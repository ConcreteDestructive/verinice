/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command creates links for copied elements. Copies of elements are passed
 * in a map. Key of the map is the UUID of the source element, values is the
 * UUID of the copy.
 * 
 * All links from the source elements are copied the following way:
 * 
 * If the link destination element was copied together with the source a new
 * link is created from the source copy to the destination copy.
 * 
 * If the link destination element was not copied a new link is created from the
 * source copy to the original destination.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyLinksCommand extends GenericCommand {

    private static final Logger logger = Logger.getLogger(CopyLinksCommand.class);

    private static final int FLUSH_LEVEL = 20;
    private int number = 0;

    private transient Map<String, String> sourceDestMap;

    private transient Map<String, List<LinkInformation>> existingUpLinkMap;
    private transient Map<String, List<LinkInformation>> existingDownLinkMap;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    /**
     * @param sourceDestMap
     */
    public CopyLinksCommand(Map<String, String> sourceDestMap) {
        super();
        this.sourceDestMap = sourceDestMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        loadAndCacheLinks();
        copyLinks();
    }

    public void copyLinks() {
        number = 0;
        for (Entry<String, String> e : sourceDestMap.entrySet()) {
            String sourceUuid = e.getKey();
            String targetUuid = e.getValue();
            createLinks(targetUuid, existingUpLinkMap.get(sourceUuid), Direction.UP);
            createLinks(targetUuid, existingDownLinkMap.get(sourceUuid), Direction.DOWN);
        }
    }

    private void createLinks(String sourceUuid, List<LinkInformation> destinations,
            Direction direction) {
        if (destinations == null) {
            return;
        }
        for (LinkInformation destAndType : destinations) {
            String uuid = destAndType.destination;
            String copyDestUuid = sourceDestMap.get(uuid);
            if (copyDestUuid != null) {
                uuid = copyDestUuid;
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Creating link to copy of target... " + sourceUuid + " -> " + uuid);
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Creating link to same target... " + sourceUuid + " -> " + uuid);
            }
            if (direction == Direction.UP) {
                createLink(sourceUuid, uuid, destAndType.type);
            } else {
                createLink(uuid, sourceUuid, destAndType.type);
            }
            number++;
            if (number % FLUSH_LEVEL == 0) {
                flushAndClear();
            }
        }
        flushAndClear();
    }

    private void flushAndClear() {
        IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
        linkDao.flush();
        linkDao.clear();
        getDao().flush();
        getDao().clear();
    }

    private void createLink(String sourceUuid, String destUuid, String type) {
        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<>(sourceUuid,
                destUuid, type);
        try {
            getCommandService().executeCommand(createLink);
        } catch (CommandException e) {
            logger.error("Error while creating link for copy", e);
            throw new RuntimeCommandException(e);
        }
    }

    public void loadAndCacheLinks() {
        final Set<String> sourceUUIDs = sourceDestMap.keySet();
        List<Object[]> allLinkedUuids = getDao()
                .findByCallback(new FindLinksForElements(sourceUUIDs));

        existingUpLinkMap = new HashMap<>();
        existingDownLinkMap = new HashMap<>();
        for (Object[] entry : allLinkedUuids) {
            String dependantUUID = (String) entry[0];
            String dependencyUUID = (String) entry[1];
            String typeId = (String) entry[2];
            cacheLink(dependantUUID, dependencyUUID, typeId);
        }
    }

    private void cacheLink(String dependantUUID, String dependencyUUID, String typeId) {
        cacheLink(dependantUUID, dependencyUUID, typeId, existingUpLinkMap);
        cacheLink(dependencyUUID, dependantUUID, typeId, existingDownLinkMap);
    }

    public static void cacheLink(String source, String dest, String type,
            Map<String, List<LinkInformation>> map) {
        List<LinkInformation> destinations = map.get(source);
        if (destinations == null) {
            destinations = new LinkedList<>();
            map.put(source, destinations);
        }
        destinations.add(new LinkInformation(dest, type));
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    private static final class FindLinksForElements implements HibernateCallback {
        private final Set<String> sourceUUIDs;

        private FindLinksForElements(Set<String> sourceUUIDs) {
            this.sourceUUIDs = sourceUUIDs;
        }

        @Override
        public Object doInHibernate(Session session) throws SQLException {
            Query query = session.createQuery(
                    "select l.dependant.uuid,l.dependency.uuid,l.id.typeId from sernet.verinice.model.common.CnALink l where l.dependant.uuid in (:sourceUUIDs) or l.dependency.uuid in (:sourceUUIDs)");
            query.setParameterList("sourceUUIDs", sourceUUIDs);
            return query.list();
        }
    }

    private static final class LinkInformation {

        LinkInformation(String destination, String type) {
            this.destination = destination;
            this.type = type;
        }

        private final String destination;
        private final String type;
    }

    private enum Direction {
        UP, DOWN
    }
}
