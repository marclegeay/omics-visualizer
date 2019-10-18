package dk.ku.cpr.OmicsVisualizer.external.tableimport.task;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.OmicsVisualizer.external.table.ImportOVTableDataTask;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

public class ImportNoGuiOVTableReaderFactory extends AbstractTaskFactory {
	
	private final OVManager ovManager;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportNoGuiOVTableReaderFactory(
			final OVManager ovManager
	) {
		this.ovManager = ovManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final LoadOVTableReaderTask readerTask = new LoadOVTableReaderTask(this.ovManager);
		
		return new TaskIterator(new SelectFileOVTableTask(readerTask, this.ovManager.getServiceRegistrar()),
				readerTask,
				new ImportOVTableDataTask(readerTask, ovManager),
				new AddImportedTableTask(readerTask, ovManager));
	}
}
