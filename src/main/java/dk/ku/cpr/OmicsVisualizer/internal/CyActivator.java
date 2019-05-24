package dk.ku.cpr.OmicsVisualizer.internal;

import static org.cytoscape.io.DataCategory.TABLE;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.io.WildCardCyFileFilter;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.task.ImportAttributeOVTableReaderFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.task.ImportNoGuiOVTableReaderFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.task.LoadOVTableFileTaskFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.tunable.AttributeDoubleIDMappingParametersHandlerFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVVisualization.ChartType;
import dk.ku.cpr.OmicsVisualizer.internal.task.ConnectTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.DisconnectTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.ListPaletteTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.OperatorListTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveFilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveVisualizationTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RetrieveStringNetworkTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.ShowConnectWindowTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.ShowFilterWindowTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.ShowRetrieveWindowTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.ShowVisualizationWindowTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.TableDeleteTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.TableListTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.TableSetCurrentTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.VersionTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.VisualizationTaskFactory;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("\n\n");
		System.out.println("[OV] Starting Omics Visualizer App !");

		final CyServiceRegistrar serviceRegistrar = getService(context, CyServiceRegistrar.class);
		final StreamUtil streamUtil = getService(context, StreamUtil.class);

		OVManager ovManager = new OVManager(serviceRegistrar);
		registerService(context, ovManager, SessionLoadedListener.class);
		registerService(context, ovManager, SessionAboutToBeSavedListener.class);
		registerService(context, ovManager, NetworkAboutToBeDestroyedListener.class);
		registerService(context, ovManager, NetworkDestroyedListener.class);
		registerService(context, ovManager, NetworkAddedListener.class);
		
		// Version (Command only)
		{
			VersionTaskFactory factory = new VersionTaskFactory(context.getBundle().getVersion().toString());
			Properties props = new Properties();
			props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
			props.setProperty(ServiceProperties.COMMAND, "version");
			props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Returns the current version of the app");
			
			registerService(context, factory, TaskFactory.class, props);
		}

		// Register services to load a file
		{
			// Code from table-import-impl CyActivator
			{
				// ".xls"
				WildCardCyFileFilter filter = new WildCardCyFileFilter(
						new String[] { "xls", "xlsx" },
						new String[] { "application/excel" },
						"Excel",
						TABLE,
						streamUtil
						);
				ImportAttributeOVTableReaderFactory factory = new ImportAttributeOVTableReaderFactory(filter, ovManager);
				Properties props = new Properties();
				props.setProperty("readerDescription", "Attribute Table file reader");
				props.setProperty("readerId", "attributeOVTableReader");
				//				registerService(context, factory, InputStreamTaskFactory.class, props);
				ovManager.addReaderFactory(factory, props);
			}
			// Code from table-import-impl CyActivator
			{
				// ".txt"
				WildCardCyFileFilter filter = new WildCardCyFileFilter(
						new String[] { "csv", "tsv", "txt", "tab", "net", "" },
						new String[] { "text/csv", "text/tab-separated-values", "text/plain", "" },
						"Comma or Tab Separated Value",
						TABLE,
						streamUtil
						);
				filter.setBlacklist("xml", "xgmml", "rdf", "owl", "zip", "rar", "jar", "doc", "docx", "ppt", "pptx",
						"pdf", "jpg", "jpeg", "gif", "png", "svg", "tiff", "ttf", "mp3", "mp4", "mpg", "mpeg",
						"exe", "dmg", "iso", "cys");

				ImportAttributeOVTableReaderFactory factory = new ImportAttributeOVTableReaderFactory(filter, ovManager);
				Properties props = new Properties();
				props.setProperty("readerDescription", "Attribute Table file reader");
				props.setProperty("readerId", "attributeOVTableReader_txt");
				//				registerService(context, factory, InputStreamTaskFactory.class, props);
				ovManager.addReaderFactory(factory, props);
			}
			// Code from table-import-impl CyActivator
			{
				AttributeDoubleIDMappingParametersHandlerFactory factory =
						new AttributeDoubleIDMappingParametersHandlerFactory(ImportType.TABLE_IMPORT, serviceRegistrar);
				registerService(context, factory, GUITunableHandlerFactory.class);
			}
		}

		// Register the available actions
		{
			Integer menuGravity=0;
			
			// Loading a table: (Apps menu)
			// Code from core-task-impl CyActivator
			{
				LoadOVTableFileTaskFactory factory = new LoadOVTableFileTaskFactory(ovManager);

				Properties appsProps = new Properties();
				appsProps.setProperty(ServiceProperties.PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				appsProps.setProperty(ServiceProperties.MENU_GRAVITY, (++menuGravity).toString());
				appsProps.setProperty(ServiceProperties.TITLE, "Import Table from File...");

				registerService(context, factory, TaskFactory.class, appsProps);
				//registerService(context, factory, LoadTableFileTaskFactory.class, props);
			}
			
			// Loading a table: (File > Import menu)
			{
				LoadOVTableFileTaskFactory factory = new LoadOVTableFileTaskFactory(ovManager);
				
				Properties importProps = new Properties();
				importProps.setProperty(ServiceProperties.PREFERRED_MENU, "File.Import");
				importProps.setProperty(ServiceProperties.INSERT_SEPARATOR_BEFORE, "true");
				importProps.setProperty(ServiceProperties.MENU_GRAVITY, "8");
				importProps.setProperty(ServiceProperties.TITLE, "Omics Visualizer table from File...");

				registerService(context, factory, TaskFactory.class, importProps);
			}
			
			// Loading a table (Command only)
			{
				ImportNoGuiOVTableReaderFactory factory = new ImportNoGuiOVTableReaderFactory(ovManager);
				
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "load");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Load an Omics Visualizer table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Get table list (Command-only)
			{
				TableListTaskFactory factory = new TableListTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "table list");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Get the list of Omics Visualizer tables");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Set current table (Command-only)
			{
				TableSetCurrentTaskFactory factory = new TableSetCurrentTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "table set current");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Set the current Omics Visualizer table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Delete current table (Command-only)
			{
				TableDeleteTaskFactory factory = new TableDeleteTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "table delete");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Delete the current Omics Visualizer table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Get operator list (Command only)
			{
				OperatorListTaskFactory factory = new OperatorListTaskFactory();
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "filter list operators");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "List the available operators");
				
				registerService(context, factory, TaskFactory.class, props);
			}

			// Access filter
			{
				ShowFilterWindowTaskFactory factory = new ShowFilterWindowTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(ServiceProperties.TITLE, "Filter table rows...");
				props.setProperty(ServiceProperties.MENU_GRAVITY, (++menuGravity).toString());
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "filter show");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Show the filter window of the current table");

				registerService(context, factory, TaskFactory.class, props);
			}

			// Modify filter (Command only)
			{
				FilterTaskFactory factory = new FilterTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "filter");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Filters the row of an Omics Visualizer table");

				registerService(context, factory, TaskFactory.class, props);
			}

			// Remove filter (Command only)
			{
				RemoveFilterTaskFactory factory = new RemoveFilterTaskFactory(ovManager);
				Properties props = new Properties();
//				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
//				props.setProperty(TITLE, "Remove filter");
//				props.setProperty(MENU_GRAVITY, (++menuGravity).toString());
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "filter remove");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Removes the filter of the active table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Access retrieve
			{
				ShowRetrieveWindowTaskFactory factory = new ShowRetrieveWindowTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(ServiceProperties.TITLE, "Retrieve and connect the table to a String Network...");
				props.setProperty(ServiceProperties.MENU_GRAVITY, (++menuGravity).toString());
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "retrieve show");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Show the retrieve window of the current table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Retrieve (Command only)
			{
				RetrieveStringNetworkTaskFactory factory = new RetrieveStringNetworkTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "retrieve");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Retrieve a STRING network and connects it to the current table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Access connect
			{
				ShowConnectWindowTaskFactory factory = new ShowConnectWindowTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(ServiceProperties.TITLE, "Manage table connections...");
				props.setProperty(ServiceProperties.MENU_GRAVITY, (++menuGravity).toString());
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "connect show");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Show the connect window of the current table");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Connect (Command only)
			{
				ConnectTaskFactory factory = new ConnectTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "connect");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Connect the current table with the current network");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Disconnect (Command only)
			{
				DisconnectTaskFactory factory = new DisconnectTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "disconnect");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Disconnect the current table and the current network if they are already connected");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Access inner visualization
			{
				ShowVisualizationWindowTaskFactory factory = new ShowVisualizationWindowTaskFactory(ovManager, ChartType.PIE);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(ServiceProperties.TITLE, "Apply a Pie Chart visualization...");
				props.setProperty(ServiceProperties.MENU_GRAVITY, (++menuGravity).toString());
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz show inner");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Show the inner visualization (pie charts) window of the current table");

				registerService(context, factory, TaskFactory.class, props);
			}

			// Apply inner visualization - Continuous (Command only)
			{
				VisualizationTaskFactory factory = new VisualizationTaskFactory(ovManager, true, ChartType.PIE);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz apply inner continuous");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Apply an inner visualization (pie charts) with a continuous mapping.");

				registerService(context, factory, TaskFactory.class, props);
			}
			// Apply inner visualization - Discrete (Command only)
			{
				VisualizationTaskFactory factory = new VisualizationTaskFactory(ovManager, false, ChartType.PIE);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz apply inner discrete");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Apply an inner visualization (pie charts) with a discrete mapping.");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Access outer visualization
			{
				ShowVisualizationWindowTaskFactory factory = new ShowVisualizationWindowTaskFactory(ovManager, ChartType.CIRCOS);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(ServiceProperties.TITLE, "Apply a Donut Chart visualization...");
				props.setProperty(ServiceProperties.MENU_GRAVITY, (++menuGravity).toString());
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz show outer");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Show the outer visualization (donuts charts) window of the current table");

				registerService(context, factory, TaskFactory.class, props);
			}

			// Apply outer visualization - Continuous (Command only)
			{
				VisualizationTaskFactory factory = new VisualizationTaskFactory(ovManager, true, ChartType.CIRCOS);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz apply outer continuous");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Apply an outer visualization (donuts charts) with a continuous mapping.");

				registerService(context, factory, TaskFactory.class, props);
			}
			// Apply outer visualization - Discrete (Command only)
			{
				VisualizationTaskFactory factory = new VisualizationTaskFactory(ovManager, false, ChartType.CIRCOS);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz apply outer discrete");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Apply an outer visualization (donuts charts) with a discrete mapping.");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Remove inner visualization (Command-only)
			{
				RemoveVisualizationTaskFactory factory = new RemoveVisualizationTaskFactory(ovManager, "inner");
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz remove inner");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Remove the inner Visualization (pie charts) of the current network");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Remove outer visualization (Command-only)
			{
				RemoveVisualizationTaskFactory factory = new RemoveVisualizationTaskFactory(ovManager, "outer");
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "viz remove outer");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "Remove the outer Visualization (donuts charts) of the current network");

				registerService(context, factory, TaskFactory.class, props);
			}
			
			// List palettes (Command-only)
			{
				ListPaletteTaskFactory factory = new ListPaletteTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(ServiceProperties.COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(ServiceProperties.COMMAND, "palette list");
				props.setProperty(ServiceProperties.COMMAND_DESCRIPTION, "List available palettes with their provider");

				registerService(context, factory, TaskFactory.class, props);
			}
		}
	}
}
