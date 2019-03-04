package dk.ku.cpr.OmicsVisualizer.internal;

import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

import dk.ku.cpr.OmicsVisualizer.external.io.read.OVTableReaderManager;
import dk.ku.cpr.OmicsVisualizer.external.io.read.OVTableReaderManagerImpl;
import dk.ku.cpr.OmicsVisualizer.external.loaddatatable.LoadOVTableFileTaskFactoryImpl;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.io.WildCardCyFileFilter;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.task.ImportAttributeOVTableReaderFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.tunable.AttributeDoubleIDMappingParametersHandlerFactory;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveFilterTaskFactory;

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
			// Code from io-impl CyActivator
			{
				OVTableReaderManagerImpl tableReaderManager = new OVTableReaderManagerImpl(streamUtil);
				registerService(context, tableReaderManager, OVTableReaderManager.class);
				registerServiceListener(context, tableReaderManager::addInputStreamTaskFactory, tableReaderManager::removeInputStreamTaskFactory, InputStreamTaskFactory.class);
			}
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
				props.setProperty("readerId", "attributeTableReader");
				registerService(context, factory, InputStreamTaskFactory.class, props);
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
				props.setProperty("readerId", "attributeTableReader_txt");
				registerService(context, factory, InputStreamTaskFactory.class, props);
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
				LoadOVTableFileTaskFactoryImpl factory = new LoadOVTableFileTaskFactoryImpl(ovManager);
	
				Properties props = new Properties();
				
//				props.setProperty(PREFERRED_MENU, "File.Import"); // File.Import.Table
//				props.setProperty(MENU_GRAVITY, "5.2");
//				props.setProperty(TITLE, "OVTable from File...");
				
				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(MENU_GRAVITY, "1");
				props.setProperty(TITLE, "Load a File...");
				props.setProperty(COMMAND_NAMESPACE, "ov");
				props.setProperty(COMMAND, "load");
				props.setProperty(COMMAND_DESCRIPTION, "Load an Omics Visualizer table");
				
				registerService(context, factory, TaskFactory.class, props);
				//registerService(context, factory, LoadTableFileTaskFactory.class, props);
			}
			
			// Access filter
			{
				FilterTaskFactory factory = new FilterTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(TITLE, "Filter...");
				props.setProperty(MENU_GRAVITY, "2");
				props.setProperty(COMMAND_NAMESPACE, "ov");
				props.setProperty(COMMAND, "filter");
				props.setProperty(COMMAND_DESCRIPTION, "Filters the row of the active Omics Visualizer table");
				
				registerService(context, factory, TaskFactory.class, props);
			}
			
			// Remove filter
			{
				RemoveFilterTaskFactory factory = new RemoveFilterTaskFactory(ovManager);
				Properties props = new Properties();
				props.setProperty(PREFERRED_MENU, OVShared.OV_PREFERRED_MENU);
				props.setProperty(TITLE, "Remove filter");
				props.setProperty(MENU_GRAVITY, "3");
				
				registerService(context, factory, TaskFactory.class, props);
			}
		}
	}
}
