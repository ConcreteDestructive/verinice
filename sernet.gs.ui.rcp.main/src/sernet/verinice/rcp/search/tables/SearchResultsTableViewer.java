/*******************************************************************************
 * Copyright (c) 2015 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.tables;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.ColumnStore;
import sernet.verinice.rcp.search.IColumnStore;

/**
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class SearchResultsTableViewer extends TableViewer implements IStructuredContentProvider {

    private IColumnStore columnStore;

    public SearchResultsTableViewer(Composite parent, IColumnStore columnStore, VeriniceSearchResultObject veriniceSearchResultObject) {

        super(parent);

        this.columnStore = columnStore;

        Table table = getTable();

        setContentProvider(this);

        createColumns();

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        setInput(veriniceSearchResultObject);
        refresh();
    }

    private void createColumns() {

        for (final PropertyType propertyType : columnStore.getColumns()) {

            TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
            column.setLabelProvider(new ColumnLabelProvider() {

                @Override
                public String getText(Object element) {
                    VeriniceSearchResultRow row = (VeriniceSearchResultRow) element;
                    return row.getValueFromResultString(propertyType.getName());
                }
            });

        }

    }


    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof VeriniceSearchResultObject) {
            return ((VeriniceSearchResultObject) inputElement).getRows().toArray(new Object[((VeriniceSearchResultObject) inputElement).getRows().size()]);
        }

        return new Object[0];
    }

}