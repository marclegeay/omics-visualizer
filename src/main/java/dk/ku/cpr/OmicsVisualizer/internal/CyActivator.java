package dk.ku.cpr.OmicsVisualizer.internal;

import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.io.WildCardCyFileFilter;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.task.ImportAttributeOVTableReaderFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.task.LoadOVTableFileTaskFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.tunable.AttributeDoubleIDMappingParametersHandlerFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.OperatorListTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveFilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.ShowFilterWindowTaskFactory;

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
				ImportAttributeOVTableReaderFactory factory = new ImportAttributeOVTableReaderFactory(filter, serviceRegistrar);
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

				ImportAttributeOVTableReaderFactory factory = new ImportAttributeOVTableReaderFactory(filter, serviceRegistrar);
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
			// Loading a table:
			// Code from core-task-impl CyActivator
			{
				LoadOVTableFileTaskFactory factory = new LoadOVTableFileTaskFactory(ovManager);

				Properties props = new Properties();

				//				props.setProperty(PREFERRED_MENU, "File.Import"); // File.Import.Table
				//				props.setProperty(MENU_GRAVITY, "5.2");
				//				props.setProperty(TITLE, "OVTable from File...");

				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(MENU_GRAVITY, "1");
				props.setProperty(TITLE, "Load a File...");
				props.setProperty(COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(COMMAND, "load");
				props.setProperty(COMMAND_DESCRIPTION, "Load an Omics Visualizer table");

				registerService(context, factory, TaskFactory.class, props);
				//registerService(context, factory, LoadTableFileTaskFactory.class, props);
			}
			
			// Get operator list (Command only)
			{
				OperatorListTaskFactory factory = new OperatorListTaskFactory();
				Properties props = new Properties();
				props.setProperty(COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(COMMAND, "operators");
				props.setProperty(COMMAND_DESCRIPTION, "List the available operators");
				
				registerService(context, factory, TaskFactory.class, props);
			}

			// Access filter
			{
				ShowFilterWindowTaskFactory factory = new ShowFilterWindowTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(TITLE, "Filter...");
				props.setProperty(MENU_GRAVITY, "2");
				props.setProperty(COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(COMMAND, "filter show");
				props.setProperty(COMMAND_DESCRIPTION, "Show the filter window of the current table");

				registerService(context, factory, TaskFactory.class, props);
			}

			// Modify filter (Command only)
			{
				FilterTaskFactory factory = new FilterTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(COMMAND, "filter");
				props.setProperty(COMMAND_DESCRIPTION, "Filters the row of an Omics Visualizer table");

				registerService(context, factory, TaskFactory.class, props);
			}

			// Remove filter
			{
				RemoveFilterTaskFactory factory = new RemoveFilterTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(TITLE, "Remove filter");
				props.setProperty(MENU_GRAVITY, "3");
				props.setProperty(COMMAND_NAMESPACE, OVShared.OV_COMMAND_NAMESPACE);
				props.setProperty(COMMAND, "filter remove");
				props.setProperty(COMMAND_DESCRIPTION, "Removes the filter of the active table");

				registerService(context, factory, TaskFactory.class, props);
			}
		}
	}
}
