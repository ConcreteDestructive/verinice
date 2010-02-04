/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.bsi.dnd.CnPItems;
import sernet.verinice.iso27k.model.IISO27kElement;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class CopyHandler extends AbstractHandler {

	private static final Logger LOG = Logger.getLogger(CopyHandler.class);
	
	List<IISO27kElement> selectedElementList = new ArrayList<IISO27kElement>();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		changeSelection(HandlerUtil.getCurrentSelection(event));
		CnPItems.clear();
		CnPItems.setItems(selectedElementList);	
		return null;
	}
	
	public void changeSelection(ISelection selection) {
		try {
			selectedElementList.clear();
			if(selection instanceof IStructuredSelection) {
				for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
					Object sel = iterator.next();
					if(sel instanceof IISO27kElement) {
						selectedElementList.add((IISO27kElement) sel);
					}
				}			
			}		
		} catch (Exception e) {
			LOG.error("Could not execute selectionChanged", e);
		}
	}

}
