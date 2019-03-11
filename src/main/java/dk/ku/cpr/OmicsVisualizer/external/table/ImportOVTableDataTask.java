package dk.ku.cpr.OmicsVisualizer.external.table;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.json.JSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ImportOVTableDataTask extends AbstractTask implements TunableValidator, ObservableTask {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private CyTableReader reader;
	private final OVManager ovManager;
	
	private CyTable globalTable;
	private boolean byReader;
	private List<CyTable> mappedTables;
	
	@Tunable(
			description = "New Table Name:",
			gravity = 5.0,
			longDescription="The title of the new table",
			exampleStringValue = "Supplemental Info"
	)
	public String newTableName;
	
	@ProvidesTitle
	public String getTitle() {		return "Import Data";	}

	public ImportOVTableDataTask(final CyTableReader reader, final OVManager ovManager) {
		this.reader = reader;
		this.ovManager = ovManager;
		this.byReader = true;
		init();
	}

	public ImportOVTableDataTask(final CyTable globalTable, final OVManager ovManager) {
		this.byReader = false;
		this.ovManager = ovManager;
		this.globalTable = globalTable;

		init();
	}
	
	private final void init() {
		this.mappedTables = new ArrayList<>();
		
		if (byReader) {
			if (reader != null && reader.getTables() != null) {
				newTableName = reader.getTables()[0].getTitle();
			}
		} else {
			newTableName = globalTable.getTitle();
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		addTable();
	}
	
	private void addTable(){
		final CyTableManager tableMgr = ovManager.getService(CyTableManager.class);
		
		if (byReader) {
			if (this.reader != null && this.reader.getTables() != null) {
				for (CyTable table : reader.getTables()) {
					if (!newTableName.isEmpty())
						table.setTitle(newTableName);
					
					tableMgr.addTable(table);
					ovManager.addOVTable(table);
				}
			} else{
				if (reader == null)
					logger.warn("reader is null." );
				else
					logger.warn("No tables in reader.");
			}
		} else {
			if (tableMgr.getTable(globalTable.getSUID()) != null) {
				tableMgr.deleteTable(globalTable.getSUID());
				globalTable.setPublic(true);
			}
			
			if (!newTableName.isEmpty())
				globalTable.setTitle(newTableName);
			
			tableMgr.addTable(globalTable);
			mappedTables.add(globalTable);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {	return Arrays.asList(CyTable.class, String.class, JSONResult.class);	}
	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(CyTable.class)) 		return (R)"";
		if (type.equals(String.class)) 		return (R)"";
		if (type.equals(JSONResult.class)) {
			@SuppressWarnings("unused")
			JSONResult res = () -> {		return "{}";	};	}
		return null;
	}
	
	@Override
	public ValidationState getValidationState(Appendable errMsg) {

		final CyTableManager tableMgr = ovManager.getService(CyTableManager.class);

		for (CyTable table : tableMgr.getGlobalTables()) {
			try {
				if (table.getTitle().matches(newTableName)) {
					errMsg.append(
							"There already exists a table with name: " + newTableName
							+ ". Please select another table name.\n");
					return ValidationState.INVALID;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
		}

		return ValidationState.OK;
	}
}
