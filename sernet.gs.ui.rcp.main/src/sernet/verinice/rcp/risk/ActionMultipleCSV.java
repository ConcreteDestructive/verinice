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
 *     Alexander Koderman - initial API and implementation
 *     Daniel Murygin
 ******************************************************************************/
package sernet.verinice.rcp.risk;

import java.util.Iterator;

// Imports for handling files (not the best way..)
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// and import for internal charsets - don't know more about it
import sernet.gs.service.VeriniceCharset; // charset...

// imports to trigger the LTR stuff..
import sernet.verinice.service.csv.CsvExport;
import sernet.verinice.service.csv.ICsvExport;
//import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableService;
//import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;
import org.apache.log4j.Logger;

// add Progress Tracking
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import java.lang.reflect.InvocationTargetException;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.NonModalWizardDialog;

/**
 * This action class runs a ISO/IEC 27005 risk analysis
 * on data in one or more organizations.
 *
 * @see RiskAnalysisActionDelegate
 * @author Alexander Koderman
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ActionMultipleCSV extends RightsEnabledAction implements ISelectionListener  {

    public static final String ID = "sernet.gs.ui.rcp.main.ActionMultipleCSV"; //$NON-NLS-1$
    private ICsvExport csvExportHandler = new CsvExport(); // JS
    private static final Logger log = Logger.getLogger(RiskAnalysisIsoWizard.class); // JS
    private CnATreeElement selectedOrganization;
    public static final String TOOLTIP = "Stapelverarbeitung .vlt-files";
    private String exportDir;

    public ActionMultipleCSV(IWorkbenchWindow window) {
        super(ActionRightIDs.RISKANALYSIS, TOOLTIP); // tooltip for toolbar-button
        setId(ID);
		// setting specific charset encoding for csv-export (1250 is for german+windows)
        this.csvExportHandler.setCharset(VeriniceCharset.CHARSET_WINDOWS_1250);
        // use specific icon (red cross)
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ISO27K_RISK_MCSV));
        addLoadListener();
        window.getSelectionService().addSelectionListener(this);
        exportDir = "./EXPORTFILES/";
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        // Close editors to avoid that stale values remain in an open editor
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */);

        if (MessageDialog.openQuestion(getShell(), "CSV-Dateien erzeugen", "CSV-Report-Dateien erzeugen?")) {

            // create new trackable doings
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            IRunnableWithProgress operation = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try{
                        monitor.beginTask("CSV-Export", IProgressMonitor.UNKNOWN);

                        // hack and dirty:
                        // try to open vlt file and process it...
                        // 'filename' is here in workspace directory
                        // .. in subdirectory 'EXPORTFILES'
                        // .. every line in file contains name of .vlt-file without ending

                        String tmpExportDir = System.getProperty("MultipleCSVExportDir");
                        if (tmpExportDir != null)
                            exportDir = tmpExportDir;

                        String fileName = exportDir + "allfiles.txt";
                        List<String> list = new ArrayList<>();

                        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

                            //2. convert all content to lower case
                            //   for unix guys: Attention! output filename will be in lowercase!
                            //3. convert it into a List
                            list = stream.map(String::toLowerCase)
                                    .collect(Collectors.toList());

                        } catch (IOException e) {
                            log.error("MultipleCSV: Error, cannot read file of filenames..", e);
                            MessageDialog.openError(Display.getCurrent().getActiveShell(), "CSV-Export", "Datei '"+fileName+"' konnte nicht gelesen werden.");
                        }

                        // iterate over every file name ('entry') in list and trigger export
                        list.forEach(entry -> doExportVlt(entry));
                        /*doExportVlt("ScopeListing");
                        doExportVlt("AlleSzenarien");
                        doExportVlt("AssetZuAsset");
                        doExportVlt("ControlsZuAssetUndSzenarien");
                        doExportVlt("ProzessZuAsset");*/

                        log.info("MultipleCSV: End of processing multiple LTR to CSV exports.");

                    } catch (Exception e){
                        log.error("MultipleCSV: Error, maybe some csv still open or not found? ", e);
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "CSV-Export", "Ein Fehler trat auf.");
                        //MessageDialog.openError(getShell(), Messages.RiskAnalysisAction_ErrorDialogTitle, Messages.RiskAnalysisAction_ErrorDialogMessage);
                    } finally {
                        monitor.done();
                    }
                }
            };
            try {
                progressService.run(true, true, operation);
            } catch (InterruptedException|InvocationTargetException e) {
                log.error("MultipleCSV: Error, could not run progressService. ", e);
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.RiskAnalysisAction_ErrorDialogTitle, Messages.RiskAnalysisAction_ErrorDialogMessage);
            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.RiskAnalysisAction_ErrorDialogTitle, Messages.RiskAnalysisAction_ErrorDialogMessage);
            }

        } else {
            log.info("MultipleCSV: No Export wanted..");
        }
    }


    private void doExportVlt(String vltFileName) {
        // try to open vlt file and process it...
        String vlt = exportDir + vltFileName + ".vlt";
        String csvFilePath = exportDir + vltFileName + ".csv";
        LinkTableService linkTableService = new LinkTableService();

        // reading configuration like 'which organisation should be included'
        // my recommendation: remove every org. id from the list in .vlt-files (to get all data)
        ILinkTableConfiguration conf = VeriniceLinkTableIO.readLinkTableConfiguration(vlt);
        List<List<String>> table = linkTableService.createTable(conf);

        csvExportHandler.setFilePath(csvFilePath);
        csvExportHandler.exportToFile(csvExportHandler.convert(table));

    }

    // not used.. // JS
    private TitleAreaDialog openWizard() {
        RiskAnalysisIsoWizard wizard = new RiskAnalysisIsoWizard(selectedOrganization);
        return new NonModalWizardDialog(getShell(),wizard);

    }

    private void addLoadListener() {
        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                setEnabled(false);
            }
            @Override
            public void loaded(BSIModel model) {
                // Nothing to do, this action is for ISO/IEC 27005 risk analysis
            }
            @Override
            public void loaded(ISO27KModel model) {
                setEnabled(checkRights());
            }
            @Override
            public void loaded(BpModel model) {
                // Nothing to do, this action is for ISO/IEC 27005 risk analysis
            }
            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
        if (selection instanceof ITreeSelection) {
            selectedOrganization = null;
            ITreeSelection selectionCurrent = (ITreeSelection) selection;
            for (Iterator<?> iter = selectionCurrent.iterator(); iter.hasNext();) {
                Object selectedObject = iter.next();
                if (isOrganization(selectedObject)) {
                    selectedOrganization = (CnATreeElement) selectedObject;
                }
            }
        }
    }


    private boolean isOrganization(Object element) {
        return element instanceof Organization;
    }


    private Shell getShell() {
        return Display.getCurrent().getActiveShell();
    }

}
