package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.OmicsVisualizer.external.io.read.GenericReaderManager;
import dk.ku.cpr.OmicsVisualizer.internal.task.ShowOVPanelTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

/**
 * Omics Visualizer Manager.
 * This class gives access to the Cytoscape service registrar, and stores the list of tables and connections.
 * @see CyServiceRegistrar
 * @see OVTable
 * @see OVConnection
 */
public class OVManager
implements SessionLoadedListener,
SessionAboutToBeSavedListener,
NetworkAboutToBeDestroyedListener,
NetworkDestroyedListener,
NetworkAddedListener {
	private static OVManager instance=null;

	private CyServiceRegistrar serviceRegistrar;

	private List<OVTable> ovTables;
	private List<OVConnection> ovCons;

	private ShowOVPanelTaskFactory showPanelFactory;
	private OVCytoPanel ovCytoPanel;

	private CyNetworkManager netManager;
	private CyTableManager tableManager;
	
	private GenericReaderManager<InputStreamTaskFactory, CyTableReader> readerManager;
	
	/**
	 * Create the Omics Visualizer Manager instance.
	 * @param serviceRegistrar The Cytoscape service registrar
	 * @return the created instance.
	 */
	public static OVManager createInstance(CyServiceRegistrar serviceRegistrar) {
		instance = new OVManager(serviceRegistrar);
		
		return instance;
	}
	
	/**
	 * Gets the current Omics Visualizer Manager instance.
	 * @return the instance. Can be <code>null</code>.
	 */
	public static OVManager getInstance() {
		return instance;
	}

	/**
	 * Creates a Manager.
	 * @param serviceRegistrar The Cytoscape service registrar
	 */
	private OVManager(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar=serviceRegistrar;
		this.showPanelFactory=new ShowOVPanelTaskFactory(this);
		this.ovCytoPanel=null;
		this.ovTables=new ArrayList<OVTable>();
		this.ovCons=new ArrayList<>();

		this.netManager = this.getService(CyNetworkManager.class);
		this.tableManager = this.getService(CyTableManager.class);
		
		StreamUtil streamUtil = this.getService(StreamUtil.class);
		this.readerManager = new GenericReaderManager<>(DataCategory.TABLE, streamUtil);

		initOVTables();
	}

	/**
	 * Detects the Omics Visualizer tables from the list of all Cytoscape tables.
	 * @see OVTable
	 */
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
	 * Returns the connection for a given network collection (represented by its root network).
	 * @param rootNetwork The root network of the network collection
	 * @return <code>null</code> if the network is not connected, the connection otherwise
	 */
	public OVConnection getConnection(CyRootNetwork rootNetwork) {
		if(rootNetwork == null) {
			return null;
		}

		for(OVConnection ovCon : this.ovCons) {
			if(rootNetwork.equals(ovCon.getRootNetwork())) {
				return ovCon;
			}
		}

		return null;
	}

	/**
	 * Returns the connection for a given network collection (represented by its root network).
	 * @param rootNetworkName name of the root network of the network collection
	 * @return <code>null</code> if the network is not connected, the connection otherwise
	 */
	public OVConnection getConnection(String rootNetworkName) {
		for(OVConnection ovCon : this.ovCons) {
			if(rootNetworkName.equals(ovCon.getRootNetwork().toString())) {
				return ovCon;
			}
		}
		return null;
	}

	/**
	 * Returns the connections for a given table.
	 * @param table The Omics Visualizer table
	 * @return the list of connections (can be empty)
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
	 * <b style="color:red">This method should only be used by the constructor of OVConnection !</b><br>
	 * <br>
	 * Adds a connection to the list of connections.
	 * @param ovCon The connection to add
	 */
	public void addConnection(OVConnection ovCon) {
		this.ovCons.add(ovCon);
	}

	/**
	 * <b style="color:red">This method should only be used by the disconnect() method of OVConnection !</b><br>
	 * <br>
	 * Removes a connection from the list of connections.
	 * @param ovCon The connection to remove
	 */
	public void removeConnection(OVConnection ovCon) {
		this.ovCons.remove(ovCon);
	}

	/**
	 * Returns the Cytoscape service registrar.
	 * @return The service registrar
	 */
	public CyServiceRegistrar getServiceRegistrar() {
		return this.serviceRegistrar;
	}

	/**
	 * Returns the specific queried service.
	 * @param clazz The class defining the type of service desired.
	 * @return A reference to a service of type <code>clazz</code>.
	 * 
	 * @throws RuntimeException If the requested service can't be found.
	 */
	public <T> T getService(Class<? extends T> clazz) {
		return this.serviceRegistrar.getService(clazz);
	}

	/**
	 * A method that attempts to get a service of the specified type and that passes the specified filter.
	 * If an appropriate service is not found, an exception will be thrown.
	 * @param clazz The class defining the type of service desired.
	 * @param filter The string defining the filter the service must pass. See OSGi's service filtering syntax for more detail.
	 * @return A reference to a service of type <code>serviceClass</code> that passes the specified filter.
	 * 
	 * @throws RuntimeException If the requested service can't be found.
	 */
	public <T> T getService(Class<? extends T> clazz, String filter) {
		return this.serviceRegistrar.getService(clazz, filter);
	}

	/**
	 * Registers an object as an OSGi service with the specified service interface and properties.
	 * @param service The object to be registered as a service.
	 * @param clazz The service interface the object should be registered as.
	 * @param props The service properties.
	 */
	public void registerService(Object service, Class<?> clazz, Properties props) {
		this.serviceRegistrar.registerService(service, clazz, props);
	}

	/**
	 * This method registers an object as an OSGi service for all interfaces that the object implements and with the specified properties.
	 * Note that this method will NOT register services for any packages with names that begin with "java", which is an effort to avoid registering meaningless services for core Java APIs.
	 * @param service The object to be registered as a service for all interfaces that the object implements.
	 * @param props The service properties.
	 */
	public void registerAllServices(CyProperty<Properties> service, Properties props) {
		this.serviceRegistrar.registerAllServices(service, props);
	}

	/**
	 * This method unregisters an object as an OSGi service for the specified service interface.
	 * @param service The object to be unregistered as a service.
	 * @param clazz The service interface the object should be unregistered as.
	 */
	public void unregisterService(Object service, Class<?> clazz) {
		this.serviceRegistrar.unregisterService(service, clazz);
	}

	/**
	 * This method unregisters an object as all OSGi service interfaces that the object implements.
	 * @param service The object to be unregistered for services it provides.
	 */
	public void unregisterAllServices(Object service) {
		this.serviceRegistrar.unregisterAllServices(service);
	}

	/**
	 * Registers the Omics Visualizer panel as a service.
	 * @param panel The panel to register.
	 */
	public void registerOVCytoPanel(OVCytoPanel panel) {
		this.ovCytoPanel = panel;

		this.registerService(this.ovCytoPanel, CytoPanelComponent.class, new Properties());
		this.registerService(this.ovCytoPanel, SetCurrentNetworkListener.class, new Properties());
		this.registerService(this.ovCytoPanel, SelectedNodesAndEdgesListener.class, new Properties());
	}

	/**
	 * Unregisters the Omics Visualizer panel.
	 * The panel is not stored anymore afterwards.
	 */
	public void unregisterOVCytoPanel() {
		if(this.ovCytoPanel != null) {
			this.unregisterService(this.ovCytoPanel, CytoPanelComponent.class);
			this.unregisterService(this.ovCytoPanel, SetCurrentNetworkListener.class);
			this.unregisterService(this.ovCytoPanel, SelectedNodesAndEdgesListener.class);
		}

		this.ovCytoPanel = null;
	}

	/**
	 * Returns the Cytoscape network manager.
	 * @return The network manager.
	 */
	public CyNetworkManager getNetworkManager() {
		return this.netManager;
	}

	/**
	 * Returns the Cytoscape table manager.
	 * @return The table manager.
	 */
	public CyTableManager getTableManager() {
		return this.tableManager;
	}
	
	/**
	 * Adds a reader factory with the specific properties.
	 * @param factory The reader factory to add.
	 * @param props The properties of the reader factory.
	 */
	public void addReaderFactory(InputStreamTaskFactory factory, Properties props) {
		this.readerManager.addInputStreamTaskFactory(factory, props);
	}
	
	/**
	 * Returns the appropriate reader for the given file.
	 * @param uri URI of the file to read
	 * @param inputName Name of the file to read
	 * @return The reader, <code>null</code> if the file cannot be read.
	 */
	public CyTableReader getReader(URI uri, String inputName) {
		return this.readerManager.getReader(uri, inputName);
	}

	/**
	 * Adds an Omics Visualizer table to the list of tables.
	 * @param table The table to add.
	 */
	public void addOVTable(OVTable table) {
		this.ovTables.add(table);
	}
	/**
	 * Adds an Cytoscape table to the list of Omics Visualier tables.
	 * The CyTable is transformed into an OVTable before being added.
	 * @param cyTable The Cytoscape table to add.
	 * @see OVTable
	 * @see OVManager#addOVTable(OVTable)
	 */
	public void addOVTable(CyTable cyTable) {
		this.addOVTable(new OVTable(this, cyTable));
	}
	/**
	 * Removes an Omics Visualizer table from the list of tables.
	 * @param table The table to remove.
	 */
	public void removeOVTable(OVTable table) {
		this.ovTables.remove(table);
		table.deleteProperties();
	}
	/**
	 * Returns the list of all Omics Visualizer tables.
	 * @return
	 */
	public List<OVTable> getOVTables() {
		return this.ovTables;
	}

	/**
	 * Get the active OVTable, i.e. the table displayed in the OVCytoPanel.
	 * @return the active OVTable, <code>null</code> if there is no active OVTable.
	 */
	public OVTable getActiveOVTable() {
		if(this.ovCytoPanel != null) {
			return this.ovCytoPanel.getDisplayedTable();
		}

		return null;
	}

	/**
	 * Returns the Omics Visualizer panel.
	 * @return The panel.
	 */
	public OVCytoPanel getOVCytoPanel() {
		return this.ovCytoPanel;
	}

	/**
	 * Creates a task to show the Omics Visualizer panel.
	 */
	public void showPanel() {
		this.executeSynchronousTask(this.showPanelFactory.createTaskIterator());
	}

	/**
	 * Executes a list of tasks in a synchronous way.
	 * @param ti The list of tasks to execute.
	 * @param to The class that listens to the result of the tasks.
	 */
	public void executeSynchronousTask(TaskIterator ti, TaskObserver to) {
		SynchronousTaskManager<?> taskM = this.serviceRegistrar.getService(SynchronousTaskManager.class);
		taskM.execute(ti, to);
	}

	/**
	 * Executes a list of tasks in a synchronous way.
	 * @param ti The list of tasks to execute.
	 */
	public void executeSynchronousTask(TaskIterator ti) {
		this.executeSynchronousTask(ti, null);
	}

	/**
	 * Executes a list of tasks in an asynchronous way.
	 * @param ti The list of tasks to execute.
	 * @param to The class that listens to the result of the tasks.
	 */
	public void executeTask(TaskIterator ti, TaskObserver to) {
		TaskManager<?, ?> taskM = this.serviceRegistrar.getService(TaskManager.class);
		taskM.execute(ti, to);
	}
	/**
	 * Executes a list of tasks in an asynchronous way.
	 * @param ti The list of tasks to execute.
	 */
	public void executeTask(TaskIterator ti) {
		this.executeTask(ti, null);
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// First we forget about previous state:
		this.ovTables=new ArrayList<OVTable>();
		this.unregisterOVCytoPanel();

		// Then we init the OVTables
		initOVTables();
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		for(OVTable table : this.ovTables) {
			table.save();
		}
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		// A network is about to be delete
		// If this network is connected to an OVTable, we disconnect them
		
		CyNetwork net = e.getNetwork();

		if(net!= null) {
			for(OVTable table : this.ovTables) {
				if(table.isConnected() && table.isConnectedTo(net)) {
					table.disconnect(net);
				}
			}
			if(this.ovCytoPanel != null) {
				this.ovCytoPanel.update();
			}
		}
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent e) {
		if(this.ovCytoPanel != null) {
			this.ovCytoPanel.update();
		}
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		CyRootNetworkManager rootNetManager = this.getService(CyRootNetworkManager.class);
		CyNetwork newNetwork = e.getNetwork();
		
		if(newNetwork == null) {
			return;
		}
		
		// We check if the network is a sub-network of a network collection already connected
		CyRootNetwork newRootNertork = rootNetManager.getRootNetwork(newNetwork);
		OVConnection ovCon = this.getConnection(newRootNertork);
		if(ovCon != null) {
			ovCon.connectNetwork(newNetwork);
		} else {
			// We check if the network has OV columns (in case of a clone)
			CyTable netTable = newNetwork.getDefaultNetworkTable();

			if(netTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL) != null) {
				// We found a connected table
				String link = netTable.getRow(newNetwork.getSUID()).get(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OVCOL, String.class);
				if(link != null && !link.isEmpty()) {
					String splittedLink[] = DataUtils.getCSV(link);

					if(splittedLink.length == 3) {
						// We look for the table
						OVTable connectedTable=null;
						for(OVTable ovTable : this.getOVTables()) {
							if(ovTable.getTitle().equals(splittedLink[0])) {
								connectedTable = ovTable;
							}
						}

						if(connectedTable != null) {
							ovCon = connectedTable.connect(newNetwork, splittedLink[1], splittedLink[2]);
							// We try to load the Visualization
							if(ovCon != null) {
								if(netTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL) != null) {
									String viz = netTable.getRow(newNetwork.getSUID()).get(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_INNERVIZCOL, String.class);
									if(viz != null && !viz.isEmpty()) {
										ovCon.setInnerVisualization(OVVisualization.load(viz), false);
									}
								}
								if(netTable.getColumn(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL) != null) {
									String viz = netTable.getRow(newNetwork.getSUID()).get(OVShared.OV_COLUMN_NAMESPACE, OVShared.CYNETWORKTABLE_OUTERVIZCOL, String.class);
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

		if(this.ovCytoPanel != null) {
			this.ovCytoPanel.update();
		}
	}
	

}
