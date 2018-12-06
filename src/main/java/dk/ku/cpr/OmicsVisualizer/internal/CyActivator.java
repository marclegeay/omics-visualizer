package dk.ku.cpr.OmicsVisualizer.internal;

import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

import dk.ku.cpr.OmicsVisualizer.internal.api_io.read.CyTableDoubleIDReaderManager;
import dk.ku.cpr.OmicsVisualizer.internal.api_io.read.InputStreamTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.io.read.CyTableDoubleIDReaderManagerImpl;
import dk.ku.cpr.OmicsVisualizer.internal.loaddatatable.LoadDoubleIDTableFileTaskFactoryImpl;
import dk.ku.cpr.OmicsVisualizer.internal.tableimport.io.WildCardCyFileFilter;
import dk.ku.cpr.OmicsVisualizer.internal.tableimport.task.ImportAttributeDoubleIDTableReaderFactory;
import dk.ku.cpr.OmicsVisualizer.internal.tableimport.tunable.AttributeDoubleIDMappingParametersHandlerFactory;
import dk.ku.cpr.OmicsVisualizer.internal.tableimport.util.ImportType;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		final CyServiceRegistrar serviceRegistrar = getService(context, CyServiceRegistrar.class);
		final StreamUtil streamUtil = getService(context, StreamUtil.class);
		
		// Code from io-impl CyActivator
		{
			CyTableDoubleIDReaderManagerImpl tableReaderManager = new CyTableDoubleIDReaderManagerImpl(streamUtil);
			registerService(context, tableReaderManager, CyTableDoubleIDReaderManager.class);
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
			ImportAttributeDoubleIDTableReaderFactory factory = new ImportAttributeDoubleIDTableReaderFactory(filter, serviceRegistrar);
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

			ImportAttributeDoubleIDTableReaderFactory factory = new ImportAttributeDoubleIDTableReaderFactory(filter, serviceRegistrar);
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
		// Code from core-task-impl CyActivator
		{
			LoadDoubleIDTableFileTaskFactoryImpl factory = new LoadDoubleIDTableFileTaskFactoryImpl(serviceRegistrar);
			
			Properties props = new Properties();
//			props.setProperty(PREFERRED_MENU, "File.Import"); // File.Import.Table
			props.setProperty(PREFERRED_MENU, "Apps.Omics Visualizer");
			props.setProperty(MENU_GRAVITY, "5.2");
			props.setProperty(TITLE, "Load a File...");
			registerService(context, factory, TaskFactory.class, props);
			//registerService(context, factory, LoadTableFileTaskFactory.class, props);
		}
	}

}
