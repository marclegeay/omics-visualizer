package cpr.loadsitespecific.internal.task;

import java.io.InputStream;

import org.cytoscape.service.util.CyServiceRegistrar;

import cpr.loadsitespecific.internal.tableimport.task.LoadTableReaderTask;

public class LoadSiteSpecificTableReaderTask extends LoadTableReaderTask {

	public LoadSiteSpecificTableReaderTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		// TODO Auto-generated constructor stub
		System.out.println("LSSTRT 1 param");
	}
	
	public LoadSiteSpecificTableReaderTask(final InputStream is, final String fileType,final String inputName,
			final CyServiceRegistrar serviceRegistrar) {
		super(is, fileType, inputName, serviceRegistrar);
		System.out.println("LSSTRT 4 params");
	}
}
