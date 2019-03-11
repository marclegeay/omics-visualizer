package dk.ku.cpr.OmicsVisualizer.external.tableimport.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class SelectFileOVTableTask extends AbstractTask {
	
	@Tunable(description="Data table file", 
	         longDescription="The path to the file that contains the table or network to be imported.",
	         exampleStringValue="myFile.csv",
	         params="fileCategory=table;input=true", 
	         required=true, 
	         gravity=0.0)
	public File file;
	
	private LoadOVTableReaderTask tableReader;
	private InputStream stream;
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME); 

	public SelectFileOVTableTask(final LoadOVTableReaderTask readerTask, final CyServiceRegistrar serviceRegistrar) {
		tableReader = readerTask;
		
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		try{
			stream = serviceRegistrar.getService(StreamUtil.class).getInputStream(file.toURI().toURL());
			
			if (!stream.markSupported()) {
				stream = new BufferedInputStream(stream);
			}
		} catch (IOException e) {
			logger.warn("Error opening stream to URI: " + file.toString(), e);
		}

		String fileFormat = file.toURI().toString().substring(file.toURI().toString().lastIndexOf('.'));
		if (tableReader != null)
			tableReader.setInputFile(stream, fileFormat, file.toURI().toString());
	}
}
