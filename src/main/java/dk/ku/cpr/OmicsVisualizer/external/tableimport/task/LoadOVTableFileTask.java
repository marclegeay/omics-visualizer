package dk.ku.cpr.OmicsVisualizer.external.tableimport.task;

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


import java.io.File;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;


public class LoadOVTableFileTask extends AbstractLoadOVTableTask {
	
	@Tunable(description = "Data Table file:", params = "fileCategory=table;input=true")
	public File file;

	public LoadOVTableFileTask(final OVManager ovManager) {
		super(ovManager);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Loading Omics Visualizer Table");
		loadTable(file.getName(), file.toURI(), true, taskMonitor);
	}
}
