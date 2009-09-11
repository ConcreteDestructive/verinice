/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccessControlEditDialog;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;

/**
 * {@link Action} that creates a dialog to modify the access
 * rights of a {@link CnATreeElement}.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public class ShowAccessControlEditAction extends Action implements ISelectionListener {

	public static final String ID = "sernet.gs.ui.rcp.main.actions.showaccesscontroleditaction";
	private final IWorkbenchWindow window;

	public ShowAccessControlEditAction(IWorkbenchWindow window, String label) {
		this.window = window;
		setText(label);
		setId(ID);
		setActionDefinitionId(ID);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(
				ImageCache.CASCADE));
		setToolTipText("Zugriffsrechte editieren.");

		window.getSelectionService().addSelectionListener(BsiModelView.ID, this);
	}

	public void run() {
		Activator.inheritVeriniceContextState();

		IStructuredSelection selection = (IStructuredSelection) window
				.getSelectionService().getSelection();
		if (selection == null)
			return;
		
		CnATreeElement cte = (CnATreeElement) selection.getFirstElement();
		if (cte == null)
			return;
		
		final AccessControlEditDialog dialog = new AccessControlEditDialog(window.getShell(), cte);

		if (dialog.open() != InputDialog.OK)
			return;

	}
	
	public void dispose()
	{
		window.getSelectionService().removeSelectionListener(BsiModelView.ID, this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// Conditions for availability of this action:
		// - Database connection must be open (Implicitly assumes that login credentials have
		//   been transferred and that the server can be queried. This is neccessary since this
		//   method will be called before the server connection is enabled.)
		// - permission handling is needed by IAuthService implementation
		// - user has administrator privileges
		boolean b = 
			CnAElementHome.getInstance().isOpen()
			&& ServiceFactory.lookupAuthService().isPermissionHandlingNeeded()
			&& AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN });

		setEnabled(b);
	}

}
