package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

import dk.ku.cpr.OmicsVisualizer.internal.tableimport.task.ShowOVPanelTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class OVManager
	implements SessionLoadedListener,
		SessionAboutToBeSavedListener,
		NetworkAboutToBeDestroyedListener {
	
	private CyServiceRegistrar serviceRegistrar;
	private CyProperty<Properties> serviceProperties;
	
	private List<OVTable> ovTables;
	private List<OVConnection> ovCons;
	
	private ShowOVPanelTaskFactory showPanelFactory;
	private OVCytoPanel ovCytoPanel;
	
	private CyNetworkManager netManager;
	private CyTableManager tableManager;
	
	public OVManager(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar=serviceRegistrar;
		this.serviceProperties=null;
		this.showPanelFactory=new ShowOVPanelTaskFactory(this);
		this.ovCytoPanel=null;
		this.ovTables=new ArrayList<OVTable>();
		this.ovCons=new ArrayList<>();
		
		this.netManager = this.getService(CyNetworkManager.class);
		this.tableManager = this.getService(CyTableManager.class);
		
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
	
	/**
	 * Get the connection for a given CyNetwork
	 * @param network
	 * @return <code>null</code> if the network is connected to no OVTable, the OVConnection otherwise
	 */
	public OVConnection getConnection(CyNetwork network) {
		if(network == null) {
			return null;
		}
		
		for(OVConnection ovCon : this.ovCons) {
			if(network.equals(ovCon.getNetwork())) {
				return ovCon;
			}
		}
		
		return null;
	}
	
	/**
	 * Get the connection for a given CyNetwork
	 * @param network
	 * @return <code>null</code> if the network is connected to no OVTable, the OVConnection otherwise
	 */
	public OVConnection getConnection(String networkName) {
		for(OVConnection ovCon : this.ovCons) {
			if(networkName.equals(ovCon.getNetwork().toString())) {
				return ovCon;
			}
		}
		return null;
	}
	
	/**
	 * Get the connections for a given OVTable
	 * @param table
	 * @return the list of OVConnection (can be empty)
	 */
	public List<OVConnection> getConnections(OVTable table) {
		List<OVConnection> list = new ArrayList<>();
		for(OVConnection ovCon : this.ovCons) {
			if(table.equals(ovCon.getOVTable())) {
				list.add(ovCon);
			}
		}
		return list;
	}
	
	/**
	 * This method should only be used by the constructor of OVConnection !
	 * @param ovCon
	 */
	public void addConnection(OVConnection ovCon) {
		this.ovCons.add(ovCon);
	}
	
	/**
	 * This method should only be used by the disconnect() method of OVConnection !
	 * @param ovCon
	 */
	public void removeConnection(OVConnection ovCon) {
		this.ovCons.remove(ovCon);
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
				this.serviceProperties = new SimpleCyProperty<Properties>(OVShared.CYPROPERTY_NAME, new Properties(), Properties.class, SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
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
	
	public CyNetworkManager getNetworkManager() {
		return this.netManager;
	}
	
	public CyTableManager getTableManager() {
		return this.tableManager;
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
//		SynchronousTaskManager<?> taskM = this.serviceRegistrar.getService(SynchronousTaskManager.class);
//		TaskIterator ti = this.showPanelFactory.createTaskIterator();
//		taskM.execute(ti);
		this.executeSynchronousTask(this.showPanelFactory.createTaskIterator());
	}
	
	public void executeSynchronousTask(TaskIterator ti) {
		SynchronousTaskManager<?> taskM = this.serviceRegistrar.getService(SynchronousTaskManager.class);
		taskM.execute(ti);
	}
	
	public void executeTask(TaskIterator ti) {
		TaskManager<?, ?> taskM = this.serviceRegistrar.getService(TaskManager.class);
		taskM.execute(ti);
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		this.serviceProperties=null;
		this.ovTables=new ArrayList<OVTable>();
		
		initOVTables();
		if(this.ovTables.size() > 0) {
			this.ovCytoPanel.reload();
		} else {
			this.unregisterService(this.ovCytoPanel, CytoPanelComponent.class);
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		for(OVTable table : this.ovTables) {
			table.save();
		}
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		CyNetwork net = e.getNetwork();
		
		if(e!= null) {
			for(OVTable table : this.ovTables) {
				if(table.isConnected() && table.isConnectedTo(net)) {
					table.disconnect(net);
				}
			}
			this.ovCytoPanel.update();
		}
	}

}
