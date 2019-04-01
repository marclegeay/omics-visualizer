package dk.ku.cpr.OmicsVisualizer.external.tableimport.task;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.AttributeMappingParameters;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.DefaultAttributeOVTableReader;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.ExcelAttributeOVTableSheetReader;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.SupportedFileType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.TextTableReader;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;


public class ImportAttributeOVTableReaderTask extends AbstractTask implements CyTableReader, TunableValidator {
	
	private  InputStream is;
	private final String fileType;
	protected CyNetworkView[] cyNetworkViews;
	protected VisualStyle[] visualstyles;
//	private final String inputName;

	private CyTable[] cyTables;
	private static int numImports=0;
	
	@Tunable(description="Attribute Mapping Parameters:")
	public AttributeMappingParameters amp;
	
	TextTableReader reader;
	
	private final CyServiceRegistrar serviceRegistrar;

	public ImportAttributeOVTableReaderTask(
			final InputStream is,
			final String fileType,
			final String inputName,
			final OVManager ovManager
	) {
		this.fileType = fileType;
//		this.inputName = inputName;
		this.is = is;
		this.serviceRegistrar = ovManager.getServiceRegistrar();
		
		// ML: We try to see if tables were imported before, to initialize the numImports
		for(OVTable table : ovManager.getOVTables()) {
			if(table.getTitle().startsWith(OVShared.OVTABLE_DEFAULT_NAME)) {
				int nb = Integer.parseInt(table.getTitle().substring(OVShared.OVTABLE_DEFAULT_NAME.length()));
				if(nb > numImports) {
					numImports = nb;
				}
			}
		}
		if(numImports < ovManager.getOVTables().size()) {
			numImports = ovManager.getOVTables().size();
		}
		
		try {
			File tempFile = File.createTempFile("temp", this.fileType);
			tempFile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(tempFile);
			int read = 0;
			byte[] bytes = new byte[1024];
		 
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
			os.flush();
			os.close();
			
			amp = new AttributeMappingParameters(new FileInputStream(tempFile), fileType);
			this.is = new FileInputStream(tempFile);
		} catch (IOException e) {
			try {
				is.close();
			} catch (IOException e1) {
			}
			
			this.is = null;
			throw new IllegalStateException("Could not initialize object", e);
		}
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Loading table data");
		tm.setProgress(0.0);
		tm.setStatusMessage("Loading table...");
		
		Workbook workbook = null;
		
		// Load Spreadsheet data for preview.
		if (fileType != null &&
				(fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) &&
				workbook == null) {
			try {
				workbook = WorkbookFactory.create(is);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?");
			} finally {
				if (is != null) is.close();
			}
		}
		
		if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
		    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {

			// Fixed bug# 1668, Only load data from the first sheet, ignore the rest sheets
			// UPDATE: From the user perspective it makes more sense to get the selected tab/sheet instead.
			String networkName = amp.getName();
			
			if (networkName == null)
				networkName = workbook.getSheetName(0);
			
			final Sheet sheet = workbook.getSheet(networkName);
			
			if (sheet != null) {
				reader = new ExcelAttributeOVTableSheetReader(sheet, amp, serviceRegistrar);
				loadAnnotation(tm);
			}
		} else {
			try {
				reader = new DefaultAttributeOVTableReader(null, amp, this.is, serviceRegistrar); 
				loadAnnotation(tm);
			} catch (Exception ioe) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Unable to read table: "+ioe.getMessage());
			}
		}
	}

	@Override
	public CyTable[] getTables() {
		return cyTables;
	}
	
	// Modification ML: We force the key to be the row number to be able to load several rows with the same ID
	private void loadAnnotation(TaskMonitor tm) {
		tm.setProgress(0.0);
		
//		final TextTableReader reader = this.reader;
//		final AttributeMappingParameters readerAMP = (AttributeMappingParameters) reader.getMappingParameter();
//		final String primaryKey = readerAMP.getAttributeNames()[readerAMP.getKeyIndex()];
		final String primaryKey = OVShared.OVTABLE_COLID_NAME;
//		final AttributeDataType dataType = readerAMP.getDataTypes()[readerAMP.getKeyIndex()];
//		final Class<?> keyType;
		final Class<?> keyType = Integer.class;
		
//		switch (dataType) {
//			case TYPE_INTEGER:
//				keyType = Integer.class;
//				break;
//			case TYPE_LONG:
//				keyType = Long.class;
//				break;
//			default:
//				keyType = String.class;
//		}
		
		tm.setProgress(0.1);

		final CyTable table =
				serviceRegistrar.getService(CyTableFactory.class).createTable(
						OVShared.OVTABLE_DEFAULT_NAME + Integer.toString(++numImports),
			             primaryKey, keyType, false, true);
		
		cyTables = new CyTable[] { table };
		tm.setProgress(0.3);
		
		try {
			this.reader.readTable(table);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tm.setProgress(1.0);
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
//		if (amp.getKeyIndex() == -1) {
//			try {
//				errMsg.append("The primary key column needs to be selected.");
//			} catch (IOException e) {
//				e.printStackTrace();
//				return ValidationState.INVALID;
//			}
//			
//			return ValidationState.INVALID;
//		}
		
//		final AttributeDataType keyDataType = amp.getDataTypes()[amp.getKeyIndex()];
//		
//		if (!TypeUtil.isValid(SourceColumnSemantic.KEY, keyDataType)) {
//			try {
//				errMsg.append("The primary key column must be an Integer, Long or String.");
//			} catch (IOException e) {
//				e.printStackTrace();
//				return ValidationState.INVALID;
//			}
//			
//			return ValidationState.INVALID;
//		}
		
		if (amp.getSelectedColumnCount() < 1){
			try {
				errMsg.append("Table should have more than one column. Please check the selected delimeters and columns.");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			
			return ValidationState.INVALID;
		}
		
		return ValidationState.OK;
	}
}
