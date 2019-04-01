package dk.ku.cpr.OmicsVisualizer.internal.ui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class OVTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private CyTable cyTable;
	// We need the column names to access the data
	private List<String> displayedColumnNames;
	private List<Object> displayedRowKeys;
	private List<Object> filteredRowKeys;
	private List<Object> selectedRowKeys;
	private String colKey;

	public OVTableModel(OVTable ovTable, List<String> columnNames) {
		super();
		
		this.cyTable=ovTable.getCyTable();
		this.displayedColumnNames=columnNames;
		
		this.colKey = this.cyTable.getPrimaryKey().getName();
		
		List<CyRow> rows = this.cyTable.getAllRows();
		this.displayedRowKeys=new ArrayList<>();
		this.filteredRowKeys=new ArrayList<>();
		this.selectedRowKeys=new ArrayList<>();
		for(CyRow r : rows) {
			this.filteredRowKeys.add(r.getRaw(colKey));
			this.displayedRowKeys.add(r.getRaw(colKey));
		}
	}
	
	public CyTable getDataTable() {
		return this.cyTable;
	}
	
	public void addColumnName(String colName) {
		this.displayedColumnNames.add(colName);
	}

	@Override
	public int getRowCount() {
//		return this.cyTable.getRowCount();
		return this.displayedRowKeys.size();
	}

	@Override
	public int getColumnCount() {
//		return this.cyTable.getColumns().size();
		return this.displayedColumnNames.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return this.getValueAt(rowIndex, this.displayedColumnNames.get(columnIndex));
	}
	
	public Object getValueAt(int rowIndex, String colName) {
		return this.cyTable.getRow(this.displayedRowKeys.get(rowIndex)).getRaw(colName);
	}
	
	public CyColumn getColumn(int colIndex) {
		return this.cyTable.getColumn(this.displayedColumnNames.get(colIndex));
	}

	@Override
	public String getColumnName(int col) {
		return this.displayedColumnNames.get(col);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return this.getColumn(columnIndex).getType();
	}
	
	public List<String> getAllColumnNames() {
		return this.displayedColumnNames;
	}
	
	public int mapColumnNameToColumnIndex(String name) {
		if(this.displayedColumnNames.contains(name))
			return this.displayedColumnNames.indexOf(name);
		
		return -1;
	}
	
	public List<Object> getDisplayedRowKeys() {
		return this.displayedRowKeys;
	}
	
	/**
	 * filter the rows according to the keys
	 * @param rowKeys : new list of row keys to display
	 */
	public void filter(List<Object> rowKeys) {
		this.filteredRowKeys = rowKeys;
		
		this.displayedRowKeys = new ArrayList<>(this.filteredRowKeys);
		if(!this.selectedRowKeys.isEmpty()) {
			this.displayedRowKeys.retainAll(this.selectedRowKeys);
		}
		
		this.fireTableDataChanged();
	}
	
	public void removeFilter() {
		// We look for all the rows from the table
		List<CyRow> rows = this.cyTable.getAllRows();
		this.displayedRowKeys=new ArrayList<>();
		for(CyRow r : rows) {
			this.displayedRowKeys.add(r.getRaw(colKey));
		}
		
		// Because the filter is removed, all rows pass the filter
		this.filteredRowKeys = new ArrayList<>(this.displayedRowKeys);
		
		// If needed we select only selected rows
		if(!this.selectedRowKeys.isEmpty()) {
			this.displayedRowKeys.retainAll(this.selectedRowKeys);
		}
		
		this.fireTableDataChanged();
	}
	
	public boolean isFiltered(CyRow row) {
		return this.filteredRowKeys.contains(row.getRaw(colKey));
	}
	
	public void setSelectedRowKeys(List<Object> selectedRowKeys) {
		this.selectedRowKeys = selectedRowKeys;
		
		this.filter(this.filteredRowKeys);
	}

	@Override
	public void fireTableStructureChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableStructureChanged();
		} else {
			final AbstractTableModel model = (AbstractTableModel) this;
			SwingUtilities.invokeLater (new Runnable () {
				public void run() {
					model.fireTableStructureChanged();
				}
			});
		}
	}

	@Override
	public void fireTableDataChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableDataChanged();
		} else {
			final AbstractTableModel model = (AbstractTableModel) this;
			SwingUtilities.invokeLater (new Runnable () {
				public void run() {
					model.fireTableDataChanged();
				}
			});
		}
	}

	@Override
	public void fireTableChanged (final TableModelEvent event) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableChanged(event);
		} else {
			final AbstractTableModel model = (AbstractTableModel) this;
			SwingUtilities.invokeLater (new Runnable () {
				public void run() {
					model.fireTableChanged(event);
				}
			});
		}
	}
}
