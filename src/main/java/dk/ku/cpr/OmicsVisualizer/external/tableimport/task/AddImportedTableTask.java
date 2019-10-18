package dk.ku.cpr.OmicsVisualizer.external.tableimport.task;

import org.cytoscape.application.CyUserLog;

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

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

class AddImportedTableTask extends AbstractTask implements ObservableTask {

	private static Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private final CyTableReader reader;
	private final OVManager ovManager;

	AddImportedTableTask(final CyTableReader reader, final OVManager ovManager) {
		this.reader = reader;
		this.ovManager = ovManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (reader != null && reader.getTables() != null) {
			final CyTableManager tableMgr = ovManager.getService(CyTableManager.class);
			
			for (CyTable table : reader.getTables())
				tableMgr.addTable(table);
			
			ovManager.showPanel();
		} else {
			if (reader == null)
				logger.warn("reader is null.");
			else
				logger.warn("No tables in reader.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return (R) this.reader.getTables()[0].getTitle();
	}
}
