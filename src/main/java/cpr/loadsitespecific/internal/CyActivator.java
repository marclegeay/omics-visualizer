package cpr.loadsitespecific.internal;

import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import cpr.loadsitespecific.internal.tableimport.tunable.AttributeMappingParametersHandlerFactory;
import cpr.loadsitespecific.internal.tableimport.util.ImportType;

import cpr.loadsitespecific.internal.tableimport.io.WildCardCyFileFilter;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

import cpr.loadsitespecific.internal.api_io.read.CyTableDoubleIDReaderManager;
import cpr.loadsitespecific.internal.api_io.read.InputStreamTaskFactory;
import cpr.loadsitespecific.internal.io.read.CyTableDoubleIDReaderManagerImpl;
import cpr.loadsitespecific.internal.loaddatatable.LoadTableFileTaskFactoryImpl;
import cpr.loadsitespecific.internal.tableimport.task.ImportAttributeTableReaderFactory;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("=== DEBUG MESSAGES ===");
		System.out.println("LAUNCHING LoadSiteSpecific App");
		System.out.println("=== CyActivator.java ===");
		
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
			ImportAttributeTableReaderFactory factory = new ImportAttributeTableReaderFactory(filter, serviceRegistrar);
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

			ImportAttributeTableReaderFactory factory = new ImportAttributeTableReaderFactory(filter, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty("readerDescription", "Attribute Table file reader");
			props.setProperty("readerId", "attributeTableReader_txt");
			registerService(context, factory, InputStreamTaskFactory.class, props);
		}
		// Code from table-import-impl CyActivator
		{
			AttributeMappingParametersHandlerFactory factory =
					new AttributeMappingParametersHandlerFactory(ImportType.TABLE_IMPORT, serviceRegistrar);
			registerService(context, factory, GUITunableHandlerFactory.class);
		}
		// Code from core-task-impl CyActivator
		{
			LoadTableFileTaskFactoryImpl factory = new LoadTableFileTaskFactoryImpl(serviceRegistrar);
			
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Import"); // File.Import.Table
			props.setProperty(MENU_GRAVITY, "5.2");
			props.setProperty(TITLE, "Site Specific Table from File...");
			registerService(context, factory, TaskFactory.class, props);
			//registerService(context, factory, LoadTableFileTaskFactory.class, props);
		}
	}

}
