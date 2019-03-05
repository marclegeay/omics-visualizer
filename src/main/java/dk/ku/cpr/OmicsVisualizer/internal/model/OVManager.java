package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
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

public class OVManager
implements SessionLoadedListener,
SessionAboutToBeSavedListener,
NetworkAboutToBeDestroyedListener {

	private CyServiceRegistrar serviceRegistrar;

	private List<OVTable> ovTables;
	private List<OVConnection> ovCons;

	private ShowOVPanelTaskFactory showPanelFactory;
	private OVCytoPanel ovCytoPanel;

	private CyNetworkManager netManager;
	private CyTableManager tableManager;
	
	private GenericReaderManager<InputStreamTaskFactory, CyTableReader> readerManager;

	public OVManager(CyServiceRegistrar serviceRegistrar) {
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

	public void registerOVCytoPanel(OVCytoPanel panel) {
		this.ovCytoPanel = panel;

		this.registerService(this.ovCytoPanel, CytoPanelComponent.class, new Properties());
		this.registerService(this.ovCytoPanel, RowsSetListener.class, new Properties());
		this.registerService(this.ovCytoPanel, SelectedNodesAndEdgesListener.class, new Properties());
	}

	public void unregisterOVCytoPanel() {
		if(this.ovCytoPanel != null) {
			this.unregisterService(this.ovCytoPanel, CytoPanelComponent.class);
			this.unregisterService(this.ovCytoPanel, RowsSetListener.class);
			this.unregisterService(this.ovCytoPanel, SelectedNodesAndEdgesListener.class);
		}

		this.ovCytoPanel = null;
	}

	public CyNetworkManager getNetworkManager() {
		return this.netManager;
	}

	public CyTableManager getTableManager() {
		return this.tableManager;
	}
	
	public void addReaderFactory(InputStreamTaskFactory factory, Properties props) {
		this.readerManager.addInputStreamTaskFactory(factory, props);
	}
	
	public CyTableReader getReader(URI uri, String inputName) {
		return this.readerManager.getReader(uri, inputName);
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

	public OVCytoPanel getOVCytoPanel() {
		return this.ovCytoPanel;
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

	public void executeTask(TaskIterator ti, TaskObserver to) {
		TaskManager<?, ?> taskM = this.serviceRegistrar.getService(TaskManager.class);
		taskM.execute(ti, to);
	}
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
