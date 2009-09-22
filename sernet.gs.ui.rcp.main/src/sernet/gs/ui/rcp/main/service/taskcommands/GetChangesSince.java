/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadPolymorphicCnAElementById;

/**
 * Get list of changes from transaction log.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class GetChangesSince extends GenericCommand implements INoAccessControl {

	private static final String QUERY = "from ChangeLogEntry entry " + 
		"where entry.changetime > ? " + 
		"and entry.stationId != ?";
	
	private Date lastChecked;
	private String stationId;

	private List<ChangeLogEntry> entries;
	private Map<Integer, CnATreeElement> changedElements;
	
	/**
	 * Creates a changeset retrieval command for the given station id
	 * and date.
	 * 
	 * <p>The station id must not be <code>null</code>.</p>
	 * 
	 * @param lastChecked only get changes after this timestamp
	 * @param stationId filter out / remove changes for this client (self)
	 * @throws IllegalArgumentException when stationId is null.
	 */
	public GetChangesSince(Date lastChecked, String stationId) {
		if (stationId == null)
			throw new IllegalArgumentException("Station id must not be null.");
		this.stationId = stationId;
		this.lastChecked = lastChecked;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		/* save date now before query is executed, to ensure that overlapping new entries made 
		 * by another thread will definitely be included in next query:
		 * (the alternative would be to save the date after the query and possibly
		 * loose changes made between execution of the query and setting of the date)
		 */
		Date now = Calendar.getInstance().getTime();
		
		// client has never checked the log, start from now:
		if (lastChecked == null) {
			lastChecked = now;
		}
		
		IBaseDao<ChangeLogEntry, Serializable> dao = getDaoFactory().getDAO(ChangeLogEntry.class);
		entries = (List<ChangeLogEntry>) dao.findByQuery(QUERY, new Object[] {lastChecked, stationId});
		
		lastChecked = now;
	
		try { 
			hydrateChangedItems(entries);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}

	/**
	 * @param entries2
	 * @throws CommandException 
	 */
	private void hydrateChangedItems(List<ChangeLogEntry> entries2) throws CommandException {
		if (entries2.size()<1)
			return;
		
		List<Integer> ids = new ArrayList<Integer>(entries2.size());
		changedElements = new HashMap<Integer, CnATreeElement>(entries2.size());
		
		// get IDs of changed items:
		for (ChangeLogEntry logEntry : entries2) {
			if (logEntry.getElementId() != null)
				ids.add(logEntry.getElementId());
		}

		if (ids.isEmpty())
			changedElements = Collections.emptyMap();
		else
		{
			LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(ids);
			command = getCommandService().executeCommand(command);
	
			List<CnATreeElement> elements = command.getElements();
			for (CnATreeElement elmt : elements) {
				changedElements.put(elmt.getDbId(), elmt);
			}
		}

	}

	public Date getLastChecked() {
		return lastChecked;
	}

	public List<ChangeLogEntry> getEntries() {
		return entries;
	}

	public Map<Integer, CnATreeElement> getChangedElements() {
		return changedElements;
	}


}
