/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.model.Baustein;
import sernet.gs.service.PermissionException;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.CopyBausteine;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.bp.rcp.BaseProtectionView;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ElementChange;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.iso27k.rcp.CopyTreeElements;
import sernet.verinice.iso27k.rcp.CutOperation;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.rcp.IProgressRunnable;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.rcp.catalog.CatalogView;
import sernet.verinice.service.commands.CopyLinksCommand;
import sernet.verinice.service.commands.CopyLinksCommand.CopyLinksMode;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class PasteHandler extends AbstractHandler {

    private static final Logger LOG = Logger.getLogger(PasteHandler.class);

    private List<String> newCopyElements;

    /*
     * 
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            Object selection = HandlerUtil.getCurrentSelection(event);
            IViewPart part = (IViewPart) HandlerUtil.getActivePart(event);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Avtive part: " + part.getViewSite().getId());
            }
            if (selection instanceof IStructuredSelection) {
                CnATreeElement target = getTarget(part.getViewSite().getId(),
                        (IStructuredSelection) selection);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Target - type: " + target.getTypeId() + ", title:"
                            + target.getTitle());
                }
                if (CnAElementHome.getInstance().isNewChildAllowed(target)) {
                    if (!CnPItems.getCopyItems().isEmpty()) {
                        CopyLinksMode copyLinksMode = getCopyLinksMode();
                        copy(target, CnPItems.getCopyItems(), copyLinksMode);
                    } else if (!CnPItems.getCutItems().isEmpty()) {
                        cut(target, CnPItems.getCutItems());
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("User is not allowed to add elements to this group"); //$NON-NLS-1$
                }
            }
        } catch (InvocationTargetException e) {
            LOG.error("Error while pasting", e); //$NON-NLS-1$
            LOG.error("Error while pasting with target exception", e.getTargetException()); //$NON-NLS-1$

            ExceptionUtil.log(e, Messages.getString("PasteHandler.1")); //$NON-NLS-1$
        } catch (PermissionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
            handlePermissionException(e);
        } catch (Exception t) {
            if (t.getCause() instanceof PermissionException) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(t);
                }
                handlePermissionException((PermissionException) t.getCause());
            } else {
                LOG.error("Error while pasting", t); //$NON-NLS-1$
                ExceptionUtil.log(t, Messages.getString("PasteHandler.1")); //$NON-NLS-1$
            }
        }
        return null;
    }

    private CopyLinksCommand.CopyLinksMode getCopyLinksMode() {
        if (CnPItems.isCopyLinks()) {
            if (CatalogView.ID.equals(CnPItems.getCopySourcePartId())) {
                return CopyLinksCommand.CopyLinksMode.FROM_COMPENDIUM_TO_MODEL;
            } else {
                return CopyLinksCommand.CopyLinksMode.ALL;
            }
        } else {
            return CopyLinksCommand.CopyLinksMode.NONE;
        }
    }

    private void handlePermissionException(PermissionException e) {
        MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                Messages.getString("PasteHandler.2"), //$NON-NLS-1$
                e.getMessage());
    }

    /**
     * @param id
     * @param sel
     * @return
     */
    private CnATreeElement getTarget(String id, IStructuredSelection sel) {
        CnATreeElement target = null;
        if (sel.size() == 1 && sel.getFirstElement() instanceof CnATreeElement) {
            target = (CnATreeElement) sel.getFirstElement();
        } else if (ISMView.ID.equals(id)) {
            target = CnAElementFactory.getInstance().getISO27kModel();
        } else if (BaseProtectionView.ID.equals(id)) {
            target = CnAElementFactory.getInstance().getBpModel();
        } else if (BsiModelView.ID.equals(id)) {
            target = CnAElementFactory.getLoadedModel();
        }
        return target;
    }

    private void copy(final CnATreeElement target, @SuppressWarnings("rawtypes") List copyList,
            CopyLinksCommand.CopyLinksMode copyLinksMode)
            throws InvocationTargetException, InterruptedException {
        if (copyList != null && !copyList.isEmpty()) {
            IProgressRunnable operation = createOperation(target, copyList, copyLinksMode);
            if (operation != null) {
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                progressService.run(true, true, operation);
                InfoDialogWithShowToggle.openInformation(Messages.getString("PasteHandler.2"), //$NON-NLS-1$
                        NLS.bind(Messages.getString("PasteHandler.3"), //$NON-NLS-1$
                                operation.getNumberOfElements()),
                        Messages.getString("PasteHandler.0"), //$NON-NLS-1$
                        PreferenceConstants.INFO_ELEMENTS_COPIED);
                if (Activator.getDefault().getPreferenceStore()
                        .getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)) {
                    validate(target, operation);
                }
            }
        }
    }

    protected void validate(final CnATreeElement target, IProgressRunnable operation) {
        newCopyElements = ((CopyTreeElements) operation).getNewElements();
        if (newCopyElements != null) {
            validate(target);
        }
    }

    protected void validate(final CnATreeElement target) {
        final List<String> validationList = newCopyElements;
        WorkspaceJob validationCreationJob = new WorkspaceJob(
                Messages.getString("PasteHandler.5")) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();
                IStatus status = Status.OK_STATUS;
                CnATreeElement tmpTarget = target;
                try {
                    tmpTarget = Retriever.retrieveElement(tmpTarget,
                            new RetrieveInfo().setProperties(true));
                    if (!(isRootElement(tmpTarget)) || (validationList.size() == 1
                            && isSubTreeElement(validationList.get(0)))) {
                        String jobDescription = (validationList.size() == 1
                                && isSubTreeElement(validationList.get(0))
                                        ? (loadElementByUuid(validationList.get(0))).getTitle()
                                        : (tmpTarget instanceof CnATreeElement
                                                ? tmpTarget.getTitle()
                                                : Messages.getString("PasteHandler.12")));
                        monitor.beginTask(NLS.bind(Messages.getString("PasteHandler.11"),
                                new Object[] { jobDescription }), IProgressMonitor.UNKNOWN);
                        String uuid = (!isRootElement(tmpTarget) ? tmpTarget.getUuid()
                                : validationList.get(0));
                        ServiceFactory.lookupValidationService()
                                .createValidationsForSubTreeByUuid(uuid);
                        CnAElementFactory.getModel(loadElementByUuid(uuid))
                                .validationAdded(loadElementByUuid(uuid).getScopeId());
                    } else {
                        for (String uuid : validationList) {
                            monitor.beginTask(
                                    NLS.bind(Messages.getString("PasteHandler.11"),
                                            new Object[] { loadElementByUuid(uuid).getTitle() }),
                                    IProgressMonitor.UNKNOWN);
                            ServiceFactory.lookupValidationService().createValidationByUuid(uuid);
                            CnAElementFactory.getModel(loadElementByUuid(uuid))
                                    .validationAdded(loadElementByUuid(uuid).getScopeId());
                        }
                        if (validationList != null && !validationList.isEmpty()) {
                            CnAElementFactory.getModel(loadElementByUuid(validationList.get(0)))
                                    .validationAdded(
                                            loadElementByUuid(validationList.get(0)).getScopeId());
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Exception while executing createValidationsJob", e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleJob(validationCreationJob, new Mutex());
    }

    private void cut(final CnATreeElement target, @SuppressWarnings("rawtypes") List cutList)
            throws InvocationTargetException, InterruptedException {
        if (cutList.get(0) instanceof CnATreeElement && target != null) {
            @SuppressWarnings("unchecked")
            CutOperation operation = new CutOperation(target, cutList);
            operation.setInheritPermissions(inheritPermissions());
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            progressService.run(true, true, operation);
            InfoDialogWithShowToggle.openInformation(Messages.getString("PasteHandler.7"), //$NON-NLS-1$
                    NLS.bind(Messages.getString("PasteHandler.8"), operation.getNumberOfElements(), //$NON-NLS-1$
                            target.getTitle()),
                    Messages.getString("PasteHandler.9"), //$NON-NLS-1$
                    PreferenceConstants.INFO_ELEMENTS_CUT);
            if (Activator.getDefault().getPreferenceStore()
                    .getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)) {
                final List<ElementChange> changes = operation.getChanges();
                WorkspaceJob validationCreationJob = new WorkspaceJob(
                        Messages.getString("PasteHandler.5")) {
                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) {
                        Activator.inheritVeriniceContextState();
                        IStatus status = Status.OK_STATUS;
                        try {
                            String jobDescription = (changes.size() == 1
                                    && isSubTreeElement(changes.get(0).getElement())
                                            ? changes.get(0).getElement().getTitle()
                                            : (target instanceof CnATreeElement ? target.getTitle()
                                                    : Messages.getString("PasteHandler.12")));
                            if (!(isRootElement(target)) || (changes.size() == 1
                                    && isSubTreeElement(changes.get(0).getElement()))) {
                                monitor.beginTask(
                                        NLS.bind(Messages.getString("PasteHandler.11"),
                                                new Object[] { jobDescription }),
                                        IProgressMonitor.UNKNOWN);
                                CnATreeElement elmt = (!isRootElement(target) ? target
                                        : changes.get(0).getElement());
                                ServiceFactory.lookupValidationService()
                                        .createValidationsForSubTreeByUuid(elmt.getUuid());
                                CnAElementFactory.getModel(elmt).validationAdded(elmt.getScopeId());
                            } else {
                                for (ElementChange ec : changes) {
                                    monitor.beginTask(
                                            NLS.bind(Messages.getString("PasteHandler.11"),
                                                    new Object[] { ec.getElement().getTitle() }),
                                            IProgressMonitor.UNKNOWN);
                                    ServiceFactory.lookupValidationService()
                                            .createValidationByUuid(ec.getElement().getUuid());
                                }
                                if (changes != null && !changes.isEmpty()) {
                                    CnAElementFactory.getModel(changes.get(0).getElement())
                                            .validationAdded(
                                                    changes.get(0).getElement().getScopeId());
                                }
                            }
                        } catch (Exception e) {
                            LOG.error("Exception while executing createValidationsJob", e);
                        } finally {
                            monitor.done();
                        }
                        return status;
                    }
                };
                JobScheduler.scheduleJob(validationCreationJob, new Mutex());
            }
        }

    }

    /**
     * @param target
     * @param copyList
     * @param copyLinks
     * @return
     */

    @SuppressWarnings("unchecked")
    private IProgressRunnable createOperation(CnATreeElement target,
            @SuppressWarnings("rawtypes") List copyList,
            CopyLinksCommand.CopyLinksMode copyLinksMode) {
        IProgressRunnable operation = null;
        if (copyList != null && !copyList.isEmpty()) {
            if (copyList.get(0) instanceof CnATreeElement) {
                operation = new CopyTreeElements(target, copyList, copyLinksMode);
                ((CopyTreeElements) operation)
                        .setCopyAttachments(Activator.getDefault().getPreferenceStore()
                                .getBoolean(PreferenceConstants.COPY_ATTACHMENTS_WITH_OBJECTS));
            }
            if (copyList.get(0) instanceof Baustein) {
                operation = new CopyBausteine(target, copyList);
            }
        }
        return operation;
    }

    private boolean isRootElement(CnATreeElement elmt) {
        return elmt instanceof BSIModel || elmt instanceof ISO27KModel || elmt instanceof BpModel
                || elmt instanceof CatalogModel || elmt instanceof ImportBsiGroup
                || elmt instanceof ImportIsoGroup;
    }

    private boolean isSubTreeElement(Object elmt) {
        if (elmt instanceof String) {
            elmt = loadElementByUuid((String) elmt);
        }
        return elmt instanceof IISO27kGroup || elmt instanceof IBSIStrukturKategorie
                || (elmt instanceof CnATreeElement && ((CnATreeElement) elmt).isScope());
    }

    private CnATreeElement loadElementByUuid(String uuid) {
        LoadElementByUuid<CnATreeElement> elementLoader = new LoadElementByUuid<>(uuid,
                new RetrieveInfo().setProperties(true));
        try {
            elementLoader = ServiceFactory.lookupCommandService().executeCommand(elementLoader);
        } catch (CommandException e) {
            LOG.error("Error while determing element by uuid", e);
        }
        return elementLoader.getElement();
    }

    private boolean inheritPermissions() {
        return Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.CUT_INHERIT_PERMISSIONS);
    }

}
