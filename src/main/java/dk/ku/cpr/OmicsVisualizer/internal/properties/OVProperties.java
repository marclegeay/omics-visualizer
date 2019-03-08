package dk.ku.cpr.OmicsVisualizer.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

public class OVProperties {
	private OVManager ovManager;
	private String name;

	private CyTable cyTable;
	private String tableTitle;

	public OVProperties(OVManager ovManager, String name) {
		super();
		this.ovManager=ovManager;
		this.name=name;

		this.tableTitle = OVShared.OV_PREFIX + this.name;

		CyTableManager tableManager = this.ovManager.getService(CyTableManager.class);
		this.cyTable=null;
		for(CyTable table : tableManager.getGlobalTables()) {
			if(table.getTitle().equals(this.tableTitle)) {
				this.cyTable=table;
				break;
			}
		}

		if(this.cyTable == null) {
			CyTableFactory tableFactory = this.ovManager.getService(CyTableFactory.class);

			this.cyTable = tableFactory.createTable(this.tableTitle, OVShared.OVPROPERTY_KEY, String.class, false, true);
			this.cyTable.createColumn(OVShared.OVPROPERTY_VALUE, String.class, false);

			tableManager.addTable(this.cyTable);
		}
	}

	public void setProperty(String key, String value) {
		if(this.cyTable != null) {
//			System.out.println("[OV - OVProperties::setProperty] " + this.name + " - " + key + ":" + value);
			this.cyTable.getRow(key).set(OVShared.OVPROPERTY_VALUE, value);
		}
	}

	public String getProperty(String key) {
//		System.out.print("[OV - OVProperties::getProperty(1)] " + this.name + " - " + key + " -> ");
		if(this.cyTable != null && this.cyTable.rowExists(key)) {
//			System.out.println(this.cyTable.getRow(key).get(OVShared.OVPROPERTY_VALUE, String.class));
			return this.cyTable.getRow(key).get(OVShared.OVPROPERTY_VALUE, String.class);
		}

//		System.out.println("null");
		return null;
	}

	public String getProperty(String key, String defaultValue) {
//		System.out.print("[OV - OVProperties::getProperty(2)] " + this.name + " - " + key + " -> ");
		if(this.cyTable != null && this.cyTable.rowExists(key)) {
//			System.out.println(this.cyTable.getRow(key).get(OVShared.OVPROPERTY_VALUE, String.class));
			return this.cyTable.getRow(key).get(OVShared.OVPROPERTY_VALUE, String.class);
		}

//		System.out.println(defaultValue);
		return defaultValue;
	}
	
	public List<String> getPropertyKeys() {
		List<String> keys = new ArrayList<>();
		
		if(this.cyTable != null) {
			for(CyRow row : this.cyTable.getAllRows()) {
				keys.add(row.get(OVShared.OVPROPERTY_KEY, String.class));
			}
		}
		
		return keys;
	}

	public void delete() {
		CyTableManager tableManager = this.ovManager.getService(CyTableManager.class);
		tableManager.deleteTable(this.cyTable.getSUID());

		this.cyTable=null;
	}
}
