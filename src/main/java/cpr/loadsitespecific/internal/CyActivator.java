package cpr.loadsitespecific.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import cpr.loadsitespecific.internal.loaddatatable.LoadTableFileTaskFactoryImpl;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		CyServiceRegistrar serviceRegistrar = getService(context, CyServiceRegistrar.class);
		
		{
			LoadTableFileTaskFactoryImpl factory = new LoadTableFileTaskFactoryImpl(serviceRegistrar);
			
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Import"); // File.Import.Table
			props.setProperty(MENU_GRAVITY, "5.2");
			props.setProperty(TITLE, "Site Specific Table from File...");
			registerService(context, factory, TaskFactory.class, props);
			registerService(context, factory, LoadTableFileTaskFactory.class, props);
		}
	}

}
