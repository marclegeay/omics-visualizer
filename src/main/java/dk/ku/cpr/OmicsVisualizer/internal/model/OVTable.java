package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import dk.ku.cpr.OmicsVisualizer.internal.properties.OVProperties;
import dk.ku.cpr.OmicsVisualizer.internal.ui.table.OVBrowserTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.table.OVTableColumnModel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.table.OVTableModel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.table.OVTableHeaderRenderer;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

/**
 * An Omics Visualizer table.
 * It is basically a Cytoscape unassigned table, that can be connected to several networks.
 * @see CyTable
 */
public class OVTable {
	private OVManager ovManager;
	
	private CyTable cyTable;
	private OVBrowserTable jTable;
	private OVTableModel tableModel;
	private OVTableColumnModel tableColumnModel;
	
	private OVFilter filter;

	private OVProperties ovProps;
	
	/**
	 * Creates an Omics Visualizer table from a Cytoscape table.
	 * @param ovManager The Omics Visualizer manager.
	 * @param cyTable The Cytoscape table.
	 */
	public OVTable(OVManager ovManager, CyTable cyTable) {
		this.ovManager=ovManager;
		this.cyTable=cyTable;
		this.jTable=null;
		
		this.cyTable.setPublic(false);
		
		this.filter = null;
		
		this.ovProps = new OVProperties(this.ovManager, OVShared.OVPROPERTY_NAME+"-"+this.cyTable.getTitle());

		this.load();
		this.save();
	}
	
	/**
	 * Returns the data table, i.e. the Cytoscape table.
	 * @return The Cytoscape table.
	 */
	public CyTable getCyTable() {
		return this.cyTable;
	}
	
	/**
	 * Returns the browsing table.
	 * @return The browsing table.
	 */
	public JTable getJTable() {
		if(this.jTable == null) {
			this.createJTable();
		}
		
		return this.jTable;
	}
	
	/**
	 * Returns the list of connections.
	 * @return The list of connections.
	 */
	public List<OVConnection> getConnections() {
		return this.ovManager.getConnections(this);
	}
	
	/**
	 * Returns the connection between the table and a given network collection (represented by its root network).
	 * @param rootNetwork The root network of the network collection.
	 * @return The connection between the table and the network collection, or <code>null</code> if the network collection is not connected to the table.
	 */
	public OVConnection getConnection(CyRootNetwork rootNetwork) {
		if(rootNetwork == null) {
			return null;
		}
		
		for(OVConnection con : this.getConnections()) {
			if(con.getRootNetwork() == rootNetwork) {
				return con;
			}
		}
		return null;
	}
	
	/**
	 * Returns the connection between the table and a given network.
	 * If the network is part of a network collection that is connected to the table, then it will return the connection, <code>null</code> otherwise.
	 * @param network The network that is connected to the table.
	 * @return The connection between the table and the network, or <code>null</code> if the network is not connected to the table.
	 * 
	 * @see OVTable#getConnection(CyRootNetwork)
	 * @see OVTable#isConnectedTo(CyNetwork)
	 */
	public OVConnection getConnection(CyNetwork network) {
		if(network == null) {
			return null;
		}
		
		CyRootNetworkManager manager = this.ovManager.getService(CyRootNetworkManager.class);
		return this.getConnection(manager.getRootNetwork(network));
	}
	
	/**
	 * Is the table connected to at least one network collection?
	 * @return <code>true</code> if the table is connected to at least one network collection, <code>false</code> otherwise.
	 */
	public boolean isConnected() {
		return this.getConnections().size()>0;
	}
	
	/**
	 * Is the table connected to a specific network?
	 * @param net The network to test the connection.
	 * @return <code>true</code> if a connection between the network and the table exists, <code>false</code> otherwise.
	 * 
	 * @see OVTable#getConnection(CyNetwork)
	 */
	public boolean isConnectedTo(CyNetwork net) {
		return this.getConnection(net) != null;
	}
	
	/**
	 * Connects the table with a network collection.
	 * @param network The network to connect the table with.
	 * @param mappingColCyto The name of the column from the network's node table used for the mapping.
	 * @param mappingColOVTable The name of the column from the table used for the mapping.
	 * @return The connection between the network collection and the table.
	 */
	public OVConnection connect(CyNetwork network, String mappingColCyto, String mappingColOVTable) {
		CyRootNetworkManager manager = this.ovManager.getService(CyRootNetworkManager.class);
		CyRootNetwork linkedRootNetwork = manager.getRootNetwork(network);
		
		if(linkedRootNetwork == null) {
			return null;
		}
		
		OVConnection con = this.getConnection(linkedRootNetwork);
		
		if(con == null) { // new network collection
			con = new OVConnection(this.ovManager, this, linkedRootNetwork, mappingColCyto, mappingColOVTable);
		} else {
			con.update(mappingColCyto, mappingColOVTable);
		}
		
		return con;
	}
	
	/**
	 * Disconnect all the network collections.
	 * 
	 * @see OVConnection#disconnect()
	 */
	public void disconnectAll() {
		for(OVConnection con : this.getConnections()) {
			con.disconnect();
		}
	}
	
	/**
	 * Disconnect a network.
	 * @param network The network to disconnect.
	 * 
	 * @see OVConnection#disconnectNetwork(CyNetwork)
	 */
	public void disconnect(CyNetwork network) {
		OVConnection con = this.getConnection(network);
		
		if(con != null) {
			con.disconnectNetwork(network);
		}
	}

	/**
	 * Creates the browsing table.
	 */
	private void createJTable() {
		List<String> colNames = new ArrayList<String>();
		Collection<CyColumn> cols = this.cyTable.getColumns();
		for(@SuppressWarnings("unused") CyColumn c : cols) {
			colNames.add("");
		}
		
		Set<String> visibleCols = new HashSet<String>();
		
		if(this.getTableProperty(OVShared.OVTABLE_COLID_NAME, "") != "") {
			// We load the columns from the Properties
			// Cytoscape store CyTable with columns sorted alphabetically, so we stored the order of each columns in Properties
			Iterator<CyColumn> it = cols.iterator();
			String propValue;
			while(it.hasNext()) {
				CyColumn col = it.next();
				
				propValue = this.getTableProperty(col.getName());
				if(propValue != null) { // Some CyColumn are not stored in property (OVColumns)
					String propValues[] = propValue.split(",");
					
					int i = Integer.parseInt(propValues[0]);
					colNames.set(i, col.getName());
					
					boolean visible = Boolean.parseBoolean(propValues[1]);
					if(visible) {
						visibleCols.add(col.getName());
					}
				}
			}
		} else {
			// We do not display the Custom column ID name
			Iterator<CyColumn> it = cols.iterator();
			int i=0;
			while(it.hasNext()) {
				CyColumn col = it.next();
				colNames.set(i, col.getName());
				
				// We do not want to display our special OVTable columns
				if(!OVShared.isOVCol(col.getName())) {
					visibleCols.add(col.getName());
				}
				
				i++;
			}
		}

		OVBrowserTable jTable = new OVBrowserTable(ovManager);

		jTable.setAutoCreateRowSorter(true);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		jTable.getColumnModel().addColumnModelListener(this);
		

		tableModel = new OVTableModel(this, colNames);
		jTable.setModel(tableModel);
		
		tableColumnModel = new OVTableColumnModel(this);
		jTable.setColumnModel(tableColumnModel);
		
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn tableColumn = new TableColumn(i);
			tableColumn.setHeaderValue(tableModel.getColumnName(i));
			tableColumn.setHeaderRenderer(new OVTableHeaderRenderer(this.ovManager));
			tableColumnModel.addColumn(tableColumn);
			
			// We compute the with of the column afterward
			tableColumn.setPreferredWidth(tableColumn.getHeaderRenderer().getTableCellRendererComponent(jTable, tableColumn.getHeaderValue(), false, false, 0, i).getPreferredSize().width);
		}
		
		this.setVisibleColumns(visibleCols);
		
		this.jTable = jTable;
	}
	
	/**
	 * Returns the list of the column names.
	 * The list is ordered as the columns are.
	 * @return The list of the column names.
	 */
	public List<String> getColNames() {
		return this.tableColumnModel.getColumnNames(false);
	}
	
	/**
	 * Returns the class of the type of a given column.
	 * @param colName The name of the column.
	 * @return The class of the type of the column.
	 * If the column name does not exists in the table, <code>null</code> is returned.
	 */
	public Class<?> getColType(String colName) {
		CyColumn col = this.cyTable.getColumn(colName);
		
		if(col != null) {
			return col.getType();
		}
		
		return null;
	}
	
	/**
	 * Returns the class of the type of elements of a given list column.
	 * @param colName The name of the list column.
	 * @return The class of the type of the column.
	 * If the column name does not exists in the table, or if it is not a list column, <code>null</code> is returned.
	 */
	public Class<?> getColListType(String colName) {
		CyColumn col = this.cyTable.getColumn(colName);
		
		if(col != null) {
			return col.getListElementType();
		}
		
		return null;
	}
	
	/**
	 * Returns the list of columns of the Cytoscape table.
	 * The orders of the columns is the one of the Cytoscape table when it was created.
	 * The key column is not included.
	 * @return The list of columns.
	 */
	public Collection<CyColumn> getColumns() {
		Collection<CyColumn> cols = this.cyTable.getColumns();
		
		CyColumn keyCol = null;
		for(CyColumn col : cols) {
			if(col.isPrimaryKey()) {
				keyCol = col;
			}
		}
		
		cols.remove(keyCol);
		
		return cols;
	}
	
	/**
	 * Returns the list of columns of the Cytoscape table.
	 * The orders of the columns is the one defined by the user.
	 * The key column is not included.
	 * @return The list of columns.
	 */
	public Collection<CyColumn> getColumnsInOrder() {
		Collection<CyColumn> cols = new ArrayList<CyColumn>();
		
		for(String colName : this.tableColumnModel.getColumnNames(false)) {
			if(!colName.equals(OVShared.OVTABLE_COLID_NAME)) {
				cols.add(this.cyTable.getColumn(colName));
			}
		}
		
		return cols;
	}

	/**
	 * Defines the visible columns.
	 * If a column is in the set, it will be visible, otherwise it will be hidden.
	 * @param visibleAttributes The set of visible column names.
	 */
	public void setVisibleColumns(Set<String> visibleAttributes) {
		for (final String name : tableModel.getAllColumnNames()) {
			int col = tableModel.mapColumnNameToColumnIndex(name);
			TableColumn column = tableColumnModel.getColumnByModelIndex(col);
			tableColumnModel.setColumnVisible(column, visibleAttributes.contains(name));
		}
		
		this.save();
	}
	
	/**
	 * Returns the list of the visible column names.
	 * @return the list of the visible column names.
	 */
	public Collection<String> getVisibleColumns() {
		return this.tableColumnModel.getColumnNames(true);
	}
	
	/**
	 * Adds a column at the end of the table.
	 * @param colName The name of the column.
	 * @param type The type of the column
	 */
	public void addColumn(String colName, Class<?> type) {
		this.cyTable.createColumn(colName, type, false);
		
		this.tableModel.addColumnName(colName);
		
		int modelIndex = this.tableColumnModel.getColumnCount();
		TableColumn tableColumn = new TableColumn(modelIndex);
		tableColumn.setHeaderValue(this.tableModel.getColumnName(modelIndex));
		tableColumn.setHeaderRenderer(new OVTableHeaderRenderer(this.ovManager));
		tableColumnModel.addColumn(tableColumn);
	}
	
	/**
	 * Returns the list of the rows, filtered or not.
	 * @param filtered if <code>true</code> then only the filtered rows will be returned.
	 * @return The list of the rows.
	 */
	public List<CyRow> getAllRows(boolean filtered) {
		if(!filtered) {
			return this.cyTable.getAllRows();
		} else {
			List<CyRow> filteredRows = new ArrayList<>();
			
			for(CyRow row : this.cyTable.getAllRows()) {
				if(this.isFiltered(row)) {
					filteredRows.add(row);
				}
			}
			
			return filteredRows;
		}
	}
	
	/**
	 * Returns the list of selected rows.
	 * @return The list of selected rows.
	 */
	public List<CyRow> getSelectedRows() {
		List<CyRow> selectedRows = new ArrayList<>();
		
		List<Object> selectedKeys = ((OVTableModel)this.jTable.getModel()).getDisplayedRowKeys();
		for(int i : this.jTable.getSelectedRows()) {
			selectedRows.add(this.cyTable.getRow(selectedKeys.get( this.jTable.convertRowIndexToModel(i) )));
		}
		
		return selectedRows;
	}
	
	/**
	 * Sets the list of filtered rows.
	 * @param filteredRowKeys List of the keys of the filtered rows.
	 */
	public void filter(List<Object> filteredRowKeys) {
		this.tableModel.filter(filteredRowKeys);
	}
	
	/**
	 * Removes the filter.
	 */
	public void removeFilter() {
		this.tableModel.removeFilter();
		this.setFilter(null);
	}
	
	/**
	 * Checks if a row is filtered or not.
	 * @param row The row to test.
	 * @return <code>true</code> if the row is filtered, <code>false</code> if not.
	 */
	public boolean isFiltered(CyRow row) {
		return this.tableModel.isFiltered(row);
	}
	
	/**
	 * Get the applied filter.
	 * @return the filter or <code>null</code> if no filter is applied.
	 */
	public OVFilter getFilter() {
		return this.filter;
	}
	
	/**
	 * Sets the filter applied to the table.
	 * @param filter The filter.
	 */
	public void setFilter(OVFilter filter) {
		this.filter=filter;
		this.save();
	}
	
	/**
	 * Selects all the rows of the model so that all rows will be displayed.
	 * @see OVTableModel#setSelectedRowKeys(List)
	 */
	public void selectAllRows() {
		// By "selecting" no rows, the model will display all of them
		this.tableModel.setSelectedRowKeys(new ArrayList<>());
	}
	
	/**
	 * Selects the selected rows from a given network.
	 * @param cyNetwork The network.
	 * 
	 * @see OVTableModel#setSelectedRowKeys(List)
	 */
	public void displaySelectedRows(CyNetwork cyNetwork) {
		List<Object> selectedRowKeys = new ArrayList<>();
		OVConnection ovCon = this.getConnection(cyNetwork);
		
		if(ovCon == null) {
			// The network is not connected to the table, so we display all the rows
			this.selectAllRows();
			return;
		}
		
		for(CyRow nodeRow : cyNetwork.getDefaultNodeTable().getAllRows()) {
			if(nodeRow.get(CyNetwork.SELECTED, Boolean.class)) {
				List<CyRow> linkedRows = ovCon.getLinkedRows(nodeRow);
				for(CyRow tableRow : linkedRows) {
					selectedRowKeys.add(tableRow.getRaw(OVShared.OVTABLE_COLID_NAME));
				}
			}
		}
		
		this.tableModel.setSelectedRowKeys(selectedRowKeys);
	}
	
	/**
	 * Returns the title of the Cytoscape table.
	 * @return The title of the table.
	 */
	public String getTitle() {
		return this.cyTable.getTitle();
	}
	
	/**
	 * Save the table properties.
	 */
	public void save() {
		Collection<String> visibleCols = this.getVisibleColumns();
		Collection<String> cols = this.getColNames();
		Integer index = new Integer(0);
		
		for(String col : cols) {
			String savedValue = index.toString()+","+visibleCols.contains(col);
			this.setTableProperty(col, savedValue);
			index += 1;
		}

		if(this.filter != null) {
			this.setTableProperty(OVShared.PROPERTY_FILTER, this.filter.save());
		} else {
			this.setTableProperty(OVShared.PROPERTY_FILTER, "");
		}
	}
	
	/**
	 * Load the table.
	 * It look for a filter, and connected networks to apply the visualizations.
	 */
	public void load() {
		// We first load the filter
		String filterStr = this.getTableProperty(OVShared.PROPERTY_FILTER, "");
		if(!filterStr.isEmpty()) {
			this.filter = OVFilter.load(filterStr);
		}
		
		this.createJTable();
		
		// We look for connected networks
		for(CyNetwork net : this.ovManager.getNetworkManager().getNetworkSet()) {
			CyTable netTable = net.getDefaultNetworkTable();
			
			if(netTable.getColumn(OVShared.CYNETWORKTABLE_OVCOL) != null) {
				// We found a connected network
				String link = netTable.getRow(net.getSUID()).get(OVShared.CYNETWORKTABLE_OVCOL, String.class);
				if(link != null && !link.isEmpty()) {
					String splittedLink[] = DataUtils.getCSV(link);
					
					if(splittedLink.length == 3 && splittedLink[0].equals(this.getTitle())) {
						OVConnection ovCon = this.connect(net, splittedLink[1], splittedLink[2]);
						// We try to load the Visualization
						if(ovCon != null) {
							if(netTable.getColumn(OVShared.CYNETWORKTABLE_INNERVIZCOL) != null) {
								String viz = netTable.getRow(net.getSUID()).get(OVShared.CYNETWORKTABLE_INNERVIZCOL, String.class);
								if(viz != null && !viz.isEmpty()) {
									ovCon.setInnerVisualization(OVVisualization.load(viz), false);
								}
							}
							if(netTable.getColumn(OVShared.CYNETWORKTABLE_OUTERVIZCOL) != null) {
								String viz = netTable.getRow(net.getSUID()).get(OVShared.CYNETWORKTABLE_OUTERVIZCOL, String.class);
								if(viz != null && !viz.isEmpty()) {
									ovCon.setOuterVisualization(OVVisualization.load(viz), false);
								}
							}
							ovCon.updateVisualization();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the properties of the table.
	 * @return The table properties.
	 */
	public OVProperties getTableOVProperties() {
		return (this.ovProps == null ? new OVProperties(this.ovManager, OVShared.OVPROPERTY_NAME + this.cyTable.getTitle()) : this.ovProps);
	}
	
	/**
	 * Returns the value of a given table property.
	 * @param propName Name of the property.
	 * @return The value of the property, <code>null</code> if the property is not set.
	 */
	public String getTableProperty(String propName) {
		return this.getTableOVProperties().getProperty(propName);
	}
	/**
	 * Returns the value of a given table property.
	 * @param propName Name of the property.
	 * @param propDefaultValue Default value if the property is not set.
	 * @return The value of the property, <code>popDefaultValue</code> if the property is not set.
	 */
	public String getTableProperty(String propName, String propDefaultValue) {
		return this.getTableOVProperties().getProperty(propName, propDefaultValue);
	}
	
	/**
	 * Set the value of a table property.
	 * @param propName Name of the property.
	 * @param propValue Value of the property.
	 */
	public void setTableProperty(String propName, String propValue) {
		this.getTableOVProperties().setProperty(propName, propValue);
	}
	
	/**
	 * Delete all the table properties.
	 */
	public void deleteProperties() {
		if(this.ovProps != null) {
			this.ovProps.delete();
			this.ovProps=null;
		}
	}
}
