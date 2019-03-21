package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

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
		return this.cyTable.getRow(this.displayedRowKeys.get(rowIndex)).getRaw(this.displayedColumnNames.get(columnIndex));
	}

	@Override
	public String getColumnName(int col) {
		return this.displayedColumnNames.get(col);
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
}
