package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;

public class OVTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private CyTable cyTable;
	// We need the column names to access the data
	private List<String> columnNames;
	private Object[] rowKeys;

	public OVTableModel(OVTable ovTable, List<String> columnNames) {
		super();
		
		this.cyTable=ovTable.getCyTable();
		this.columnNames=columnNames;
		
		String colKey = this.cyTable.getPrimaryKey().getName();
		List<CyRow> rows = this.cyTable.getAllRows();
		this.rowKeys=new Object[rows.size()];
		int i=0;
		for(CyRow r : rows) {
			this.rowKeys[i++] = r.getRaw(colKey);
		}
	}
	
	public void addColumnName(String colName) {
		this.columnNames.add(colName);
	}

	@Override
	public int getRowCount() {
//		return this.cyTable.getRowCount();
		return this.rowKeys.length;
	}

	@Override
	public int getColumnCount() {
//		return this.cyTable.getColumns().size();
		return this.columnNames.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return this.cyTable.getRow(this.rowKeys[rowIndex]).getRaw(this.columnNames.get(columnIndex));
	}

	@Override
	public String getColumnName(int col) {
		return this.columnNames.get(col);
	}
	
	public List<String> getAllColumnNames() {
		return this.columnNames;
	}
	
	public int mapColumnNameToColumnIndex(String name) {
		if(this.columnNames.contains(name))
			return this.columnNames.indexOf(name);
		
		return -1;
	}
	
	/**
	 * filter the rows according to the keys
	 * @param rowKeys : new list of row keys to display
	 */
	public void filter(List<Object> rowKeys) {
		this.rowKeys = rowKeys.toArray();
		
		this.fireTableDataChanged();
	}
}
