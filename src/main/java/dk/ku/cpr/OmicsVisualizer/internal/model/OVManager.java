package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;
import dk.ku.cpr.OmicsVisualizer.internal.ui.ShowOVPanelTaskFactory;

public class OVManager implements SessionLoadedListener, SessionAboutToBeSavedListener {
	
	private CyServiceRegistrar serviceRegistrar;
	private CyProperty<Properties> serviceProperties;
	
	private List<OVTable> ovTables;
	
	private ShowOVPanelTaskFactory showPanelFactory;
	private OVCytoPanel ovCytoPanel;
	
	public OVManager(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar=serviceRegistrar;
		this.serviceProperties=null;
		this.showPanelFactory=new ShowOVPanelTaskFactory(this);
		this.ovCytoPanel=null;
		this.ovTables=new ArrayList<OVTable>();
		
		initOVTables();
	}
	
	public void initOVTables() {
		CyTableManager tblManager = this.getService(CyTableManager.class);
		Set<CyTable> tables = tblManager.getAllTables(true);
		for (CyTable table: tables) {
			if (OVShared.isOVTable(table)) {
				this.addOVTable(new OVTable(this, table));
			}
		}

		if(this.ovTables.size() > 0) {
			this.showPanel();
		}
	}
	
	public CyServiceRegistrar getServiceRegistrar() {
		return this.serviceRegistrar;
	}
	
	public <T> T getService(Class<? extends T> clazz) {
		return this.serviceRegistrar.getService(clazz);
	}

	public <T> T getService(Class<? extends T> clazz, String filter) {
		return this.serviceRegistrar.getService(clazz, filter);
	}

	public void registerService(Object service, Class<?> clazz, Properties props) {
		this.serviceRegistrar.registerService(service, clazz, props);
	}

	public void registerAllServices(CyProperty<Properties> service, Properties props) {
		this.serviceRegistrar.registerAllServices(service, props);
	}

	public void unregisterService(Object service, Class<?> clazz) {
		this.serviceRegistrar.unregisterService(service, clazz);
	}
	
	public void unregisterAllServices(Object service) {
		this.serviceRegistrar.unregisterAllServices(service);
	}
	
	@SuppressWarnings("unchecked")
	public CyProperty<Properties> getServiceCyProperty() {
		if(this.serviceProperties == null) {
			try { // If the service is not registered yet, it throws an Exception
				this.serviceProperties = this.serviceRegistrar.getService(CyProperty.class, "(cyPropertyName="+OVShared.CYPROPERTY_NAME+")");
			} catch(Exception e ) {
				// Now we store those Properties into the Session File
				// We use the SimpleCyProperty class to do so
				this.serviceProperties = new SimpleCyProperty<Properties>(OVShared.CYPROPERTY_NAME, new Properties(), Properties.class, SavePolicy.SESSION_FILE);
				Properties cyPropServiceProps = new Properties(); // The SimpleCyProperty service must be registered with a name, so we have Properties for this service also
				cyPropServiceProps.setProperty("cyPropertyName", this.serviceProperties.getName());
				this.serviceRegistrar.registerAllServices(this.serviceProperties, cyPropServiceProps);
			}
		}
		
		return this.serviceProperties;
	}

	public String getProperty(String propName) {
		return this.getServiceCyProperty().getProperties().getProperty(propName);
	}
	public String getProperty(String propName, String propDefaultValue) {
		return this.getServiceCyProperty().getProperties().getProperty(propName, propDefaultValue);
	}
	public void setProperty(String propName, String propValue) {
		this.getServiceCyProperty().getProperties().setProperty(propName, propValue);
	}
	
	public void addOVTable(OVTable table) {
		this.ovTables.add(table);
	}
	public void addOVTable(CyTable cyTable) {
		this.addOVTable(new OVTable(this, cyTable));
	}
	public void removeOVTable(OVTable table) {
		this.ovTables.remove(table);
		table.deleteProperties();
	}
	public List<OVTable> getOVTables() {
		return this.ovTables;
	}
	
	public void setOVCytoPanel(OVCytoPanel panel) {
		this.ovCytoPanel=panel;
	}
	
	public void showPanel() {
		SynchronousTaskManager<?> taskM = this.serviceRegistrar.getService(SynchronousTaskManager.class);
		TaskIterator ti = this.showPanelFactory.createTaskIterator();
		taskM.execute(ti);
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		System.out.println("Omics Visualizer : Session Loadeded");
		this.serviceProperties=null;
		this.ovTables=new ArrayList<OVTable>();
		
		initOVTables();
		this.ovCytoPanel.reload();
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		for(OVTable table : this.ovTables) {
			table.save();
		}
	}

}
