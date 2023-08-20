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
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.apache.log4j.Logger;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ImportOVTableDataTask extends AbstractTask implements TunableValidator {

	private static final Logger logger = Logger.getLogger(CyUserLog.NAME);
	
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

	public ImportOVTableDataTask(final CyTableReader reader, String tableName, final OVManager ovManager) {
		this.reader = reader;
		this.ovManager = ovManager;
		this.byReader = true;
		this.newTableName=tableName;
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
			if (newTableName == null && reader != null && reader.getTables() != null) {
				newTableName = reader.getTables()[0].getTitle();
			}
		} else {
			newTableName = globalTable.getTitle();
		}
		
		if(newTableName == null) {
			newTableName = this.ovManager.getNextTableName();
		}
		
		// We check if the tableName is unique
		checkName();
	}
	
	private void checkName() {
		if(newTableName != null) {
			checkName(0);
		}
	}
	
	private void checkName(int num) {
		final CyTableManager tableMgr = ovManager.getService(CyTableManager.class);
		
		String tableName = newTableName;
		if(num > 0) {
			tableName += " (" + num + ")";
		}

		for (CyTable table : tableMgr.getGlobalTables()) {
			if (table.getTitle().equals(tableName)) {
				checkName(num+1);
				return;
			}
		}
		
		newTableName = tableName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		init();
		addTable(taskMonitor);
	}
	
	private void addTable(TaskMonitor taskMonitor){
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
	public ValidationState getValidationState(Appendable errMsg) {

		final CyTableManager tableMgr = ovManager.getService(CyTableManager.class);

		for (CyTable table : tableMgr.getGlobalTables()) {
			try {
				if (table.getTitle().equals(newTableName)) {
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
