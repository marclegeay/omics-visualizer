package cpr.loadsitespecific.internal.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class SiteSpecificTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private CyTable cyTable;
	private String[] columnNames;
	private Object[] rowKeys;

	public SiteSpecificTableModel(CyTable cyTable, String[] columnNames) {
		super();
		
		this.cyTable=cyTable;
		this.columnNames=columnNames;
		
		String colKey = this.cyTable.getPrimaryKey().getName();
		List<CyRow> rows = this.cyTable.getAllRows();
		this.rowKeys=new Object[rows.size()];
		int i=0;
		for(CyRow r : rows) {
			this.rowKeys[i++] = r.getRaw(colKey);
		}
	}

	@Override
	public int getRowCount() {
		return this.cyTable.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return this.cyTable.getColumns().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return this.cyTable.getRow(this.rowKeys[rowIndex]).getRaw(this.columnNames[columnIndex]);
	}

}
