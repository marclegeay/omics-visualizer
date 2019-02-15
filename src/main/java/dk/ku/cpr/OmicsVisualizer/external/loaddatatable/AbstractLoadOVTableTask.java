package dk.ku.cpr.OmicsVisualizer.external.loaddatatable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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



import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import dk.ku.cpr.OmicsVisualizer.external.io.read.OVTableReaderManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;


abstract class AbstractLoadOVTableTask extends AbstractTask {

	private final OVManager ovManager;
	
	public AbstractLoadOVTableTask(final OVManager ovManager) {
		this.ovManager = ovManager;
	}

	void loadTable(final String name, final URI uri, boolean combine, final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Finding Table Data Reader...");

		final OVTableReaderManager tableReaderMgr = ovManager.getServiceRegistrar().getService(OVTableReaderManager.class);
		final CyTableReader reader = tableReaderMgr.getReader(uri, uri.toString());
		
		if (reader == null)
			throw new NullPointerException("Failed to find reader for specified file.");
		
		if (combine) {
			taskMonitor.setStatusMessage("Importing Data Table...");
			insertTasksAfterCurrentTask(new CombineReaderAndMappingTask(reader, ovManager));
		} else {
			taskMonitor.setStatusMessage("Loading Data Table...");
			insertTasksAfterCurrentTask(
					new ReaderTableTask(reader, ovManager.getServiceRegistrar()),
					new AddImportedTableTask(reader, ovManager.getServiceRegistrar())
			);
		}
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(CyTable.class, String.class, JSONResult.class);	}
	public Object getResults(Class<?> requestedType) {
		if (requestedType.equals(CyTable.class)) 		return "";
		if (requestedType.equals(String.class)) 		return "";
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		return "{}";	};	}
		return null;
	}

}

