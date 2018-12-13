package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class OVTableModel extends AbstractTableModel implements ColumnCreatedListener, ColumnDeletedListener,
ColumnNameChangedListener {

	private static final long serialVersionUID = 1L;
	
	private CyTable cyTable;
	private List<String> columnNames;
	private Map<String, Boolean> visibleColumn;
	private Object[] rowKeys;

	public OVTableModel(CyTable cyTable, List<String> columnNames) {
		super();
		
		this.cyTable=cyTable;
		this.columnNames=columnNames;
		
		this.visibleColumn = new HashMap<String, Boolean>();
		for(String col : columnNames) {
			this.visibleColumn.put(col, true);
		}
		
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
		return this.cyTable.getRow(this.rowKeys[rowIndex]).getRaw(this.columnNames.get(columnIndex));
	}

	@Override
	public String getColumnName(int col) {
		return this.columnNames.get(col);
	}
	
	public boolean isColumnVisible(String colName) {
		Boolean val =  this.visibleColumn.get(colName);
		
		return ((val==null) ? false : val.booleanValue());
	}
	
	public List<String> getAllColumnNames() {
		return this.columnNames;
	}
	
	public int mapColumnNameToColumnIndex(String name) {
		if(this.columnNames.contains(name))
			return this.columnNames.indexOf(name);
		
		return -1;
	}
	
	public Set<String> getVisibleColumnNames() {
		Set<String> list = new HashSet<String>();
		
		for(String col : this.columnNames) {
			if(!col.equals(OVShared.OVTABLE_COLID_NAME) && this.isColumnVisible(col)) {
				list.add(col);
			}
		}
		
		return list;
	}
	
	public void setVisibleColumnNames(Set<String> visibleCols) {
		for(String col : this.columnNames) {
			Boolean visible = visibleCols.contains(col);
			this.visibleColumn.put(col, visible);
		}
	}

	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		// TODO Auto-generated method stub
		System.out.println("CNCE");
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		// TODO Auto-generated method stub
		System.out.println("CDE");
		
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		// TODO Auto-generated method stub
		System.out.println("CCE");
		
	}
}
