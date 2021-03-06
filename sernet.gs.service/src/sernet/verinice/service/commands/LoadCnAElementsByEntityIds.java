/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

@SuppressWarnings("serial")
public class LoadCnAElementsByEntityIds<T extends CnATreeElement> extends GenericCommand {

	private Collection<Integer> ids;
	
	private Class<T> klass;

	private List<T> list;

    private IBaseDao<CnATreeElement, Serializable> elementDao;

    private RetrieveInfo ri;
	
	public LoadCnAElementsByEntityIds(Class<T> klass, Collection<Integer> ids) {
		this.klass = klass;
		this.ids = ids;
		
		if (ids.isEmpty()){
			throw new IllegalArgumentException("There must be at least one id available.");
		}
	}

	   public LoadCnAElementsByEntityIds(Class<T> klass, Collection<Integer> ids, RetrieveInfo ri) {
	        this.klass = klass;
	        this.ids = ids;
            this.ri = ri;

	        if (ids.isEmpty()){
	            throw new IllegalArgumentException("There must be at least one id available.");
	        }
	    }

	@SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<T, Serializable> dao = getDaoFactory().getDAO(klass);
		
		list = (List<T>) dao.findByCallback(new Callback(ids));

		if (ri != null){
		    List<T> hydratedElements =  new ArrayList<T>();
		    for(T element : list){
		        hydratedElements.add((T) getElementDao().retrieve(element.getDbId(), ri));
		    }

		    list = hydratedElements;
		}
	}

	public List<T> getElements() {
		return list;
	}

	private static class Callback implements HibernateCallback
	{
		private Collection<Integer> ids;
		
		Callback(Collection<Integer> ids)
		{
			this.ids = ids;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			Query query = session.createQuery(
					"from CnATreeElement elmt "
					+ "where elmt.entity.dbId in (:ids)")
					.setParameterList("ids", ids);
			
			return query.list();
		}
		
	}


    public IBaseDao<CnATreeElement, Serializable> getElementDao() {
        if (elementDao == null) {
            elementDao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);
        }
        return elementDao;
    }

}
