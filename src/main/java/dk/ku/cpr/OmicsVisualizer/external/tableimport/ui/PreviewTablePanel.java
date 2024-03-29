package dk.ku.cpr.OmicsVisualizer.external.tableimport.ui;

import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_BOOLEAN;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_FLOATING;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_INTEGER;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_LONG;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType.TYPE_STRING;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType.NETWORK_IMPORT;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType.TABLE_IMPORT;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic.ALIAS;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic.ATTR;
//import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic.KEY;
import static dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic.NONE;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
// ML
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.accessibility.AccessibleComponent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.SupportedFileType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.reader.TextDelimiter;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.FileType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.ImportType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.TypeUtil;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;

/**
 * General purpose preview table panel.
 */
@SuppressWarnings("serial")
public class PreviewTablePanel extends JPanel {

	private static final float ICON_FONT_SIZE = 14.0f;
	
	// Lines start with this char will be ignored.
	private String commentChar;
	private int startLine;

	// Tracking attribute data type.
	private String[] namespaces;
	private SourceColumnSemantic[] types;
	private AttributeDataType[] dataTypes;
	private String[] listDelimiters;
	// ML: Custom decimal format
	private Character decimalSeparator;
	
	private Set<?> keySet;

	/*
	 * GUI Components
	 */
	private JLabel sheetLabel;
	private JComboBox<Sheet> sheetComboBox;
	private JTable previewTable;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private JScrollPane tableScrollPane;
	
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private ImportType importType;
	
	private final IconManager iconManager;
	
	private EditDialog editDialog;
	private int lastDialogIndex = -1;
	private long lastDialogTime;
	private boolean updating;
	
	private final Object lock = new Object();

	private static final Logger logger = Logger.getLogger(CyUserLog.NAME);

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(final IconManager iconManager) {
		this(TABLE_IMPORT, iconManager);
	}

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel(final ImportType importType, final IconManager iconManager) {
		this.importType = importType;
		this.iconManager = iconManager;

		initComponents();
	}

	public void setKeyAttributeList(Set<?> keySet) {
		this.keySet = keySet;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (changes == null)
			return;

		changes.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createTitledBorder("Preview"));
		
		sheetLabel = new JLabel("Sheet:");
		sheetLabel.setVisible(false);
		
		final JLabel instructionLabel;
		
		if(this.importType == ImportType.OV_IMPORT_NODE_TABLE) {
			instructionLabel = new JLabel("Click on a column to edit its name.");
		} else {
			instructionLabel = new JLabel("Click on a column to edit it.");
		}
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		LookAndFeelUtil.equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		if(this.importType == ImportType.OV_IMPORT_NODE_TABLE) {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(sheetLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(RELATED)
							.addComponent(getSheetComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(UNRELATED)
							.addComponent(instructionLabel)
					)
					.addComponent(getTableScrollPane(), DEFAULT_SIZE, 320, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(sheetLabel)
							.addComponent(getSheetComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(instructionLabel)
					)
					.addPreferredGap(RELATED)
					.addComponent(getTableScrollPane(), 120, 160, Short.MAX_VALUE)
					.addPreferredGap(RELATED)
			);
		} else {
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(sheetLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(RELATED)
							.addComponent(getSheetComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(UNRELATED)
							.addComponent(instructionLabel)
							.addGap(20, 20, Short.MAX_VALUE)
							.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(RELATED)
							.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getTableScrollPane(), DEFAULT_SIZE, 320, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(sheetLabel)
							.addComponent(getSheetComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(instructionLabel)
							.addComponent(getSelectAllButton())
							.addComponent(getSelectNoneButton())
					)
					.addPreferredGap(RELATED)
					.addComponent(getTableScrollPane(), 120, 160, Short.MAX_VALUE)
					.addPreferredGap(RELATED)
			);
			
		}
		
		ColumnResizer.adjustColumnPreferredWidths(getPreviewTable());
		updatePreviewTable();
	}
	
	public JTable getPreviewTable() {
		if (previewTable == null) {
			previewTable = new JTable(new PreviewTableModel(new Vector<Vector<String>>(), new Vector<String>(), false));
			previewTable.setShowGrid(false);
			previewTable.setCellSelectionEnabled(false);
			previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			previewTable.setDefaultEditor(Object.class, null);

			if (importType == NETWORK_IMPORT) {
				final TableCellRenderer netRenderer = new PreviewTableCellRenderer();
				previewTable.setDefaultRenderer(Object.class, netRenderer);
			} else {
				previewTable.setDefaultRenderer(Object.class, new PreviewTableCellRenderer());
			}

			final JTableHeader hd = previewTable.getTableHeader();
			hd.setReorderingAllowed(false);
			hd.setDefaultRenderer(new PreviewTableHeaderRenderer());
			
			final TableColumnModelListener colModelListener = new TableColumnModelListener() {
				@Override
				public void columnMoved(TableColumnModelEvent e) {
					disposeEditDialog();
				}
				@Override
				public void columnMarginChanged(ChangeEvent e) {
					disposeEditDialog();
				}
				@Override
				public void columnSelectionChanged(ListSelectionEvent e) {
				}
				@Override
				public void columnRemoved(TableColumnModelEvent e) {
				}
				@Override
				public void columnAdded(TableColumnModelEvent e) {
				}
			};
			
			hd.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent e) {
					final TableColumnModel columnModel = previewTable.getColumnModel();
					final int newColIdx = columnModel.getColumnIndexAtX(e.getX());
					final int idx = editDialog != null ? editDialog.index : -1;
					
					disposeEditDialog();
						
					if (idx != newColIdx)
						showEditDialog(newColIdx);
					
					// Do not show editor dialog when the user is resizing the columns
					previewTable.getColumnModel().removeColumnModelListener(colModelListener);
					previewTable.getColumnModel().addColumnModelListener(colModelListener);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					previewTable.getColumnModel().removeColumnModelListener(colModelListener);
				}
			});
			
			// Also close the editor dialog when the table changes
			previewTable.getModel().addTableModelListener(evt -> disposeEditDialog());
		}
		
		return previewTable;
	}

	public String[] getAttributeNames() {
		String[] names = null;
		final PreviewTableModel model = (PreviewTableModel) getPreviewTable().getModel();
		final int columnCount = model.getColumnCount();
		names = new String[columnCount];
		
		for (int i = 0; i < columnCount; i++)
			names[i] = model.getColumnName(i);
		
		return names;
	}
	
	public String[] getNamespaces() {
		return namespaces;
	}
	
	protected void setNamespace(final int index, final String newValue) {
		if (index < 0)
			return;
		
		if (namespaces != null && namespaces.length > index)
			namespaces[index] = newValue;
	}
	
	public SourceColumnSemantic[] getTypes() {
		return types;
	}
	
	protected void setType(final int index, final SourceColumnSemantic newType) {
		if (index < 0)
			return;
		
		if (types != null && types.length > index) {
			// First replace the index that currently has this unique type by the default type
			if (newType.isUnique())
				replaceType(newType, TypeUtil.getDefaultType(importType));
			
			final SourceColumnSemantic oldType = types[index];
			types[index] = newType;
			
			if (newType != oldType)
				changes.fireIndexedPropertyChange(DataEvents.ATTR_TYPE_CHANGED, index, oldType, newType);
		}
	}
	
	protected void fillTypes(final SourceColumnSemantic newValue) {
		if (types != null)
			Arrays.fill(types, newValue);
	}
	
	protected void replaceType(final SourceColumnSemantic type1, final SourceColumnSemantic type2) {
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				if (types[i] == type1)
					setType(i, type2);
			}
		}
	}
	
	public AttributeDataType[] getDataTypes() {
		return dataTypes;
	}
	
	public AttributeDataType getDataType(final int index) {
		if (dataTypes != null && dataTypes.length > index)
			return dataTypes[index];
		
		return null;
	}
	
	protected void setDataType(final int index, final AttributeDataType newValue) {
		if (index < 0)
			return;
		
		if (dataTypes != null && dataTypes.length > index) {
			final AttributeDataType oldValue = dataTypes[index];
			dataTypes[index] = newValue;
			
			if (newValue != oldValue)
				changes.fireIndexedPropertyChange(DataEvents.ATTR_DATA_TYPE_CHANGED, index, oldValue, newValue);
		}
	}
	
	public String[] getListDelimiters() {
		return listDelimiters;
	}
	
	public void setListDelimiter(final int index, final String newValue) {
		if (index < 0)
			return;
		
		if (listDelimiters != null && listDelimiters.length > index)
			listDelimiters[index] = newValue;
	}
	
	public FileType getFileType() {
		final String name = getSourceName();

		if (name != null && name.startsWith("gene_association"))
			return FileType.GENE_ASSOCIATION_FILE;

		return FileType.ATTRIBUTE_FILE;
	}

	public String getSourceName() {
		return getPreviewTable().getName();
	}
	
	public void updatePreviewTable(CyTable cyTable) {
		// Only for OV Import node table 
		if(this.importType != ImportType.OV_IMPORT_NODE_TABLE) {
			return;
		}
		
		updating = true;

		try {
			// The cyTable should have 4 columns: ID,key,value,source
			// we want to get rid of the ID
			int i=0;
			dataTypes = new AttributeDataType[3];
			Vector<String> colnames = new Vector<>();
			for(CyColumn col : cyTable.getColumns()) {
				if(!col.getName().equals(OVShared.OVTABLE_COLID_NAME)) {
					colnames.add(col.getName());
					Class<?> colType = col.getType();
					if(colType == Integer.class) {
						dataTypes[i] = AttributeDataType.TYPE_INTEGER;
					} else if(colType == Long.class) {
						dataTypes[i] = AttributeDataType.TYPE_LONG;
					} else if(colType == Double.class) {
						dataTypes[i] = AttributeDataType.TYPE_FLOATING;
					} else if(colType == Boolean.class) {
						dataTypes[i] = AttributeDataType.TYPE_BOOLEAN;
					} else {
						dataTypes[i] = AttributeDataType.TYPE_STRING;
					}
					i++;
				}
			}
			// get rid of the ID col
			colnames.remove(OVShared.OVTABLE_COLID_NAME);


			Vector< Vector<String> > data = new Vector<>();
			for(CyRow row : cyTable.getAllRows()) {
				Vector<String> dataRow = new Vector<>();
				for(String colname : colnames) {
					Object val = row.getRaw(colname);
					if(val == null) {
						dataRow.add(null);
					} else {
						dataRow.add(val.toString());
					}
				}

				data.add(dataRow);
			}
		
			types = new SourceColumnSemantic[3];
			Arrays.fill(types, SourceColumnSemantic.ATTR);
			
			listDelimiters = new String[colnames.size()];
			
			namespaces = TypeUtil.getPreferredNamespaces(types);
			
			final PreviewTableModel newModel = new PreviewTableModel(data, colnames, false);
			updatePreviewTable(newModel, cyTable.getTitle());
		} finally {
			updating = false;
		}
	}
	
	/**
	 * Load file and show preview.
	 */
	public void updatePreviewTable(
			final Workbook workbook,
			final String fileType,
			final String fileFullName,
			final InputStream tempIs,
			final List<String> delimiters,
			final String commentLineChar,
			final int startLine,
			// ML: Custom decimal format
			final Character decimalSeparator
	) throws IOException {
		if (tempIs == null)
			return;

		if ((commentLineChar != null) && (commentLineChar.trim().length() != 0))
			this.commentChar = commentLineChar;
		else
			this.commentChar = null;

		this.startLine = startLine;

		updating = true;
		
		// ML: Custom decimal format
		this.decimalSeparator = decimalSeparator;
		
		try {
			getSheetComboBox().removeAllItems();
			getSheetComboBox().setVisible(false);
			sheetLabel.setVisible(false);
			
			PreviewTableModel newModel = null;
			
			if (SupportedFileType.EXCEL.getExtension().equalsIgnoreCase(fileType)
					|| SupportedFileType.OOXML.getExtension().equalsIgnoreCase(fileType)) {
				final int numberOfSheets = workbook.getNumberOfSheets();
				
				if (numberOfSheets == 0)
					throw new IllegalStateException("No sheet found in the workbook.");
	
				for (int i = 0; i < numberOfSheets; i++) {
					final Sheet sheet = workbook.getSheetAt(i);
					
					if (sheet.getPhysicalNumberOfRows() > 0)
						getSheetComboBox().addItem(sheet);
				}
				
				if (getSheetComboBox().getItemCount() > 0)
					getSheetComboBox().setSelectedIndex(0);
				
				if (getSheetComboBox().getItemCount() > 1) {
					sheetLabel.setVisible(true);
					getSheetComboBox().setVisible(true);
				}
				
				/*
				 * Load each sheet in the workbook.
				 */
				if (getSheetComboBox().getItemCount() > 0) {
					final Sheet sheet = workbook.getSheetAt(0);
					updatePreviewTable(sheet);
				} else {
					throw new RuntimeException("No data found in the Excel sheets.");
				}
			} else {
				newModel = parseText(tempIs, delimiters, startLine);
	
				String sourceName;
				String[] urlParts = fileFullName.split("/");
				
				if (urlParts.length > 0 && !fileFullName.isEmpty())
					sourceName = urlParts[urlParts.length - 1];
				else
					sourceName = "Source Table";
				
				// ML: Custom decimal format
				dataTypes = TypeUtil.guessDataTypes(newModel, decimalSeparator);
				types = TypeUtil.guessTypes(importType, newModel, dataTypes, null);
				listDelimiters = new String[newModel.getColumnCount()];
				namespaces = TypeUtil.getPreferredNamespaces(types);
				
				updatePreviewTable(newModel, sourceName);
			}
		} finally {
			updating = false;
		}
	}

	public void setFirstRowAsColumnNames() {
		final PreviewTableModel model = (PreviewTableModel) getPreviewTable().getModel();
		model.setFirstRowNames(true);

		types = TypeUtil.guessTypes(importType, model, dataTypes, null);
		updatePreviewTable();
		
		ColumnResizer.adjustColumnPreferredWidths(getPreviewTable());
	}

//	protected boolean isCytoscapeAttributeFile(final URL sourceURL) throws IOException {
//		InputStream is = null;
//		boolean testResult = true;
//
//		try {
//			BufferedReader bufRd = null;
//			is = URLUtil.getInputStream(sourceURL);
//			
//			try {
//				String line = null;
//				int i = 0;
//				bufRd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8").newDecoder()));
//				
//				// Test first two lines to check the file type.
//				while ((line = bufRd.readLine()) != null) {
//					if (i == 0) {
//						String[] elements = line.split(" +");
//
//						if (elements.length == 1) {
//							// True so far.
//						} else {
//							elements = line.split("[(]");
//
//							if ((elements.length == 2) && elements[1].startsWith("class=")) {
//								// true so far.
//							} else {
//								testResult = false;
//
//								break;
//							}
//						}
//					} else if (i == 1) {
//						String[] elements = line.split(" += +");
//
//						if (elements.length != 2)
//							testResult = false;
//					} else if (i >= 2) {
//						break;
//					}
//
//					i++;
//				}
//			} finally {
//				if (bufRd != null) bufRd.close();
//			}
//		} finally {
//			if (is != null) is.close();
//		}
//
//		return testResult;
//	}

	public int checkKeyMatch(final int targetColumn) {
		int matched = 0;

		if (keySet != null && !keySet.isEmpty()) {
			final TableModel curModel = getPreviewTable().getModel();
	
			try {
				curModel.getValueAt(0, targetColumn);
			} catch (ArrayIndexOutOfBoundsException e) {
				return 0;
			}
	
			final int rowCount = curModel.getRowCount();
			
			for (int i = 0; i < rowCount; i++) {
				final Object val = curModel.getValueAt(i, targetColumn);
				
				if (val != null && keySet.contains(val))
					matched++;
			}
		}
		
		return matched;
	}
	
	public int getPreviewSize() {
		return 100;
	}
	
	/**
	 * Returns the first index of the table column that has the passed type.
	 */
	protected int getColumnIndex(final SourceColumnSemantic type) {
		if (types != null)
			return Arrays.asList(types).indexOf(type);
		
		return -1;
	}
	
	protected void setAliasColumn(final int index, final boolean flag) {
		if (types != null && types.length > index) {
			types[index] = flag ? ALIAS : ATTR;
			updatePreviewTable();
		}
	}
	
	protected boolean isImported(final int index) {
		if (types != null && types.length > index)
			return types[index] != NONE;

		return false;
	}
	
	// ML
	private Class<?> getNumericClass(double val) {
		// We use BigDecimal to know if the numerical value is int, long or double
        BigDecimal bd = BigDecimal.valueOf(val);
        try {
        	bd.intValueExact();
        	return Integer.class;
        } catch(ArithmeticException eInt) {
        	try {
        		bd.longValueExact();
        		return Long.class;
        	} catch(ArithmeticException eLong) {
        		return Double.class;
        	}
        }
	}
	// END ML
	
	// ML
	private String formatCell(Cell cell, Class<?> cellClass, DataFormatter formatter, FormulaEvaluator evaluator) {
		if (cell == null) {
			return "";
		}
		
		CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            if (evaluator == null) {
                return cell.getCellFormula();
            }
            cellType = evaluator.evaluateFormulaCell(cell);
        }
        switch (cellType) {
            case NUMERIC :
            	if (DateUtil.isCellDateFormatted(cell)) {
                    return formatter.formatCellValue(cell, evaluator);
                }
                BigDecimal val = BigDecimal.valueOf(cell.getNumericCellValue());

                if(cellClass == Integer.class) {
                	return String.valueOf(val.intValue());
                }
                if(cellClass == Long.class) {
                	return String.valueOf(val.longValue());
                }
                if(cellClass == Double.class) {
                	return String.valueOf(val.doubleValue());
                }
                return val.toPlainString();

            case STRING :
                return cell.getRichStringCellValue().getString();

            case BOOLEAN :
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK :
                return "";
        }
        
        return "";
	}
	// END ML
	
	private PreviewTableModel parseExcel(final Sheet sheet, int startLine) throws IOException {
		int size = getPreviewSize();
		
		if (size == -1)
			size = Integer.MAX_VALUE;

		int maxCol = 0;
		final Vector<Vector<String>> data = new Vector<>();

		int rowCount = 0;
		int validRowCount = 0;
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		DataFormatter formatter = new DataFormatter();
		Row row;

		// ML : moved (was at the end before)
		final boolean firstRowNames = importType == NETWORK_IMPORT || importType == TABLE_IMPORT;
		// END ML
		
		// ML
		Vector<Class<?>> colTypes = new Vector<>();
		Vector<Row> previewRows = new Vector<>();
		// End ML

		while (((row = sheet.getRow(rowCount)) != null) && (validRowCount < size)) {
			if (rowCount >= startLine) {
				// ML
//				final Vector<String> rowVector = new Vector<>();
				// END ML

				if (maxCol < row.getLastCellNum())
					maxCol = row.getLastCellNum();
				
				// ML
				// We initialize colTypes
				if(colTypes.size() != maxCol) {
					for(int c=colTypes.size(); c<maxCol; ++c) {
						colTypes.add(null);
					}
				}
				// END ML

				for (short j = 0; j < maxCol; j++) {
					Cell cell = row.getCell(j);
					// ML
//					if (cell == null || cell.getCellType() == CellType.ERROR || 
//							(cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR)) {
//						rowVector.add(null);
//					} else {
//						rowVector.add(formatter.formatCellValue(cell, evaluator));
					if (cell != null && cell.getCellType() != CellType.ERROR && 
							(cell.getCellType() != CellType.FORMULA || cell.getCachedFormulaResultType() != CellType.ERROR)) {
					// END ML
						
						// ML
						// We check types only if it's not the names of columns
						if(!firstRowNames || validRowCount > 0) {
							Class<?> colType = colTypes.get(j);
							
							CellType cellType = cell.getCellType();
							if (cellType == CellType.FORMULA) {
								if (evaluator != null) {
									cellType = evaluator.evaluateFormulaCell(cell);
								}
							}
							
							if(colType == null) {
								switch (cellType) {
								case NUMERIC :
									if (DateUtil.isCellDateFormatted(cell)) {
										colType = String.class;
									} else {
										colType = getNumericClass(cell.getNumericCellValue());
									}
									break;
								case STRING :
									colType = String.class;
									break;
								case BOOLEAN :
									colType = Boolean.class;
									break;
								case FORMULA :
								case BLANK :
									colType = null; // don't know yet
									break;
								}
							} else {
								// colType is not null
								// We check if the current type fits with the one of the cell

								// Previously detected as boolean?
								if (colType == Boolean.class) {
									// Just make sure the other rows are also compatible with boolean values...
									if (cellType != CellType.BOOLEAN) {
										// This row does not contain a boolean, so the column has to be a String
										colType = String.class;
									}
								} else if (colType == Integer.class) {
									// Make sure the other rows are also integers...
									if (cellType == CellType.NUMERIC) {
										Class<?> cellNumericType = getNumericClass(cell.getNumericCellValue());
										if (cellNumericType == Long.class)
											colType = Long.class;
										else if (cellNumericType == Double.class)
											colType = Double.class;
										// else it is Integer, we don't change
									} else {
										// Previously numeric, not numeric anymore: String
										colType = String.class;
									}
								} else if (colType == Long.class) {
									// Make sure the other rows are also longs (no need to check for integers anymore)...
									if (cellType == CellType.NUMERIC) {
										Class<?> cellNumericType = getNumericClass(cell.getNumericCellValue());
										if (cellNumericType == Double.class)
											colType = Double.class;
									} else {
										// Previously numeric, not numeric anymore: String
										colType = String.class;
									}
								} else if (colType == Double.class) {
									// Make sure the other rows are also doubles (no need to check for other numeric types)...
									if (cellType != CellType.NUMERIC) {
										colType = String.class;
									}
								}
							}

							colTypes.set(j, colType);
						}
				        // END OF ML
					}
				}

				// ML
//				data.add(rowVector);
				previewRows.add(row);
				// END ML
				validRowCount++;
			}

			rowCount++;
		}
		
		// ML
		for(Row r : previewRows) {
			final Vector<String> rowVector = new Vector<>();
			
			for (short col = 0; col < maxCol; col++) {
				Cell cell = r.getCell(col);

				if (cell == null || cell.getCellType() == CellType.ERROR || 
						(cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR)) {
					rowVector.add(null);
				} else {
					rowVector.add(formatCell(cell, colTypes.get(col), formatter, evaluator));
				}
			}
			
			data.add(rowVector);
		}
		
		
//		return new PreviewTableModel(data, new Vector<String>(), firstRowNames);
		return new PreviewTableModel(data, new Vector<String>(), colTypes, firstRowNames);
		// END ML
	}
	
	private PreviewTableModel parseText(InputStream tempIs, List<String> delimiters, int startLine)
			throws IOException {
		String line;
//		String attrName = "Attr1";
		Vector<Vector<String>> data = null;
		int maxColumn;

		BufferedReader bufRd = new BufferedReader(new InputStreamReader(tempIs, Charset.forName("UTF-8").newDecoder()));
		/*
		 * Generate reg. exp. for delimiter.
		 */
		final String delimiterRegEx;

		if (delimiters != null) {
			StringBuffer delimiterBuffer = new StringBuffer();

			if (delimiters.size() != 0) {
				delimiterBuffer.append("[");

				for (String delimiter : delimiters)
					delimiterBuffer.append(delimiter);

				delimiterBuffer.append("]");
			}

			delimiterRegEx = delimiterBuffer.toString();
		} else {
			// treat as cytoscape attribute files.
			delimiterRegEx = " += +";
			// Extract first column for attr name.
			line = bufRd.readLine();
//			String[] line1 = line.split(" +");
//			attrName = line1[0];
		}

		/*
		 * Read & extract one line at a time. The line can be Tab delimited,
		 */
		final int size = getPreviewSize();
		boolean importAll = false;

		if (size == -1)
			importAll = true;

		// Distinguish between CSV files and everything else.
		// TODO: Since the CSV parser allows for other delimiters, consider exploring using it for everything.

		// The variables are modified by both the new method and the old method.
		int rowCount = 0;
		int validRowCount = 0;
		maxColumn = 0;
		data = new Vector<>();
		
		// Modification ML: We use CSVReader for every CSV type
//		if (delimiters != null && delimiters.contains(TextDelimiter.COMMA.getDelimiter()) && delimiters.size() == 1) {
		if(delimiters.size() == 1 && delimiters.get(0).length()==1) {
			// Only if there is exactly one delimiter and that delimiter is a
			// comma should you read the file using OpenCSV
			// New method... Using OpenCSV
			// Modification ML:
//			final CSVReader reader = new CSVReader(bufRd);
			final CSVReader reader = new CSVReader(bufRd, delimiters.get(0).charAt(0));
			String[] rowData; // Note that rowData is roughly equivalent to "parts" in the old code.
			
			while ((rowData = reader.readNext()) != null) {
				final List<String> list = Arrays.asList(rowData);
				line = list.isEmpty() ? "" : String.join(TextDelimiter.COMMA.getDelimiter(), list);
				
				if (!ignoreLine(line, rowCount)) {
					final Vector<String> row = new Vector<>();
					
					for (String field : rowData)
						row.add(field);
					
					if (rowData.length > maxColumn)
						maxColumn = rowData.length;
					
					data.add(row);
					validRowCount++;
				}
				
				rowCount++;

				if (importAll == false && validRowCount >= size)
					break;
			}
			
			try {
				reader.close();
			} catch (Exception e) { }
		} else {
			// Old method... Using naive splitting.
			String[] parts;
			
			while ((line = bufRd.readLine()) != null) {
				if (!ignoreLine(line, rowCount)) {
					final Vector<String> row = new Vector<>();

					if (delimiterRegEx.length() == 0) {
						parts = new String[1];
						parts[0] = line;
					} else {
						parts = line.split(delimiterRegEx);
					}

					for (String entry : parts) {
						row.add(entry);
					}

					if (parts.length > maxColumn)
						maxColumn = parts.length;

					data.add(row);
					validRowCount++;
				}

				rowCount++;

				if (importAll == false && validRowCount >= size)
					break;
			}
		}

		// If the inputStream is passed in from parameter, do not close it
		if (tempIs != null)
			tempIs.close();

		final boolean firstRowNames = importType == NETWORK_IMPORT || importType == TABLE_IMPORT;
		
//		if (delimiters == null) {
//			// Cytoscape attr file.
//			final Vector<String> columnNames = new Vector<>();
//			columnNames.add("Key");
//			columnNames.add(attrName);
//			
//			return new PreviewTableModel(data, columnNames, firstRowNames);
//		} else {
			return new PreviewTableModel(data, new Vector<String>(), firstRowNames);
//		}
	}

	private boolean ignoreLine(final String line, int index) {
		return ((commentChar != null) && line.startsWith(commentChar)) || (line.trim().length() == 0)
				|| (index < startLine);
	}
	
	private void showEditDialog(final int colIdx) {
		if (colIdx == lastDialogIndex && System.currentTimeMillis() - lastDialogTime < 100)
			return;
		
		// We don't want to edit the first column (key column)
		if(colIdx == 0 && importType == ImportType.OV_IMPORT_NODE_TABLE) {
			return;
		}
		
		lastDialogIndex = -1;
		lastDialogTime = 0;
		
		final Window parent = SwingUtilities.getWindowAncestor(PreviewTablePanel.this);
		
		final PreviewTableModel model = (PreviewTableModel) getPreviewTable().getModel();
		final String attrName = model.getColumnName(colIdx);
		final List<SourceColumnSemantic> availableTypes = TypeUtil.getAvailableTypes(importType);
		final List<String> availableNamespaces = TypeUtil.getAvailableNamespaces(importType);
		
		final AttributeEditorPanel attrEditorPanel = new AttributeEditorPanel(
				parent,
				attrName,
				availableTypes,
				availableNamespaces,
				types[colIdx],
				namespaces[colIdx],
				dataTypes[colIdx],
				listDelimiters[colIdx],
				iconManager,
				importType
		);
		
		if (LookAndFeelUtil.isWinLAF()) {
			attrEditorPanel.setBorder(
					BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("activeCaptionBorder")));
			attrEditorPanel.setBackground(UIManager.getColor("TableHeader.background"));
		}
		
		editDialog = new EditDialog(parent, ModalityType.MODELESS, colIdx);
		editDialog.setUndecorated(true);
		editDialog.add(attrEditorPanel);
		
		final ActionMap actionMap = attrEditorPanel.getActionMap();
		final InputMap inputMap = attrEditorPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "VK_ESCAPE");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "VK_ENTER");
		
		actionMap.put("VK_ESCAPE", new AbstractAction("VK_ESCAPE") {
			@Override
			public void actionPerformed(ActionEvent e) {
				disposeEditDialog();
			}
		});
		actionMap.put("VK_ENTER", new AbstractAction("VK_ENTER") {
			@Override
			public void actionPerformed(ActionEvent e) {
				disposeEditDialog();
			}
		});
		
		attrEditorPanel.addPropertyChangeListener("attributeName", evt -> {
			final String name = (String) evt.getNewValue();
			
			if (name != null && !name.trim().isEmpty()) {
				((PreviewTableModel) getPreviewTable().getModel()).setColumnName(colIdx, name);
				getPreviewTable().getColumnModel().getColumn(colIdx).setHeaderValue(name);
				updatePreviewTable();
			}
		});
		attrEditorPanel.addPropertyChangeListener("namespace", evt -> {
			setNamespace(colIdx, (String) evt.getNewValue());
			updatePreviewTable();
		});
		attrEditorPanel.addPropertyChangeListener("attributeType", evt -> {
			setType(colIdx, (SourceColumnSemantic) evt.getNewValue());
			updatePreviewTable();
		});
		attrEditorPanel.addPropertyChangeListener("attributeDataType", evt -> {
			final AttributeDataType newDataType = (AttributeDataType) evt.getNewValue();

			if (newDataType.isList())
				setListDelimiter(colIdx, attrEditorPanel.getListDelimiter());

			setDataType(colIdx, newDataType);
			updatePreviewTable();
		});
		attrEditorPanel.addPropertyChangeListener("listDelimiter", evt -> {
			setListDelimiter(colIdx, (String) evt.getNewValue());
			updatePreviewTable();
		});
		
		positionEditDialog();
		
		editDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				getPreviewTable().getTableHeader().repaint();
				attrEditorPanel.getAttributeNameTextField().requestFocusInWindow();
			}
			@Override
			public void windowClosed(WindowEvent e) {
				getPreviewTable().getTableHeader().repaint();
			}
		});
		
		editDialog.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				if (editDialog != null) {
					lastDialogIndex = editDialog.index;
					lastDialogTime = System.currentTimeMillis();
				}
				disposeEditDialog();
			}
			@Override
			public void windowGainedFocus(WindowEvent e) {
			}
		});
		
	    editDialog.pack();
		editDialog.setVisible(true);
	}
	
	private void positionEditDialog() {
		if (editDialog != null) {
			final JTableHeader hd = getPreviewTable().getTableHeader();
			
			// Get the column header location
			// (see: https://bugs.openjdk.java.net/browse/JDK-4408424)
			final AccessibleComponent ac = hd.getAccessibleContext().getAccessibleChild(editDialog.index)
					.getAccessibleContext().getAccessibleComponent();
			
			final Point screenPt = ac.getLocationOnScreen();
			final Point compPt = ac.getLocation();
			int xOffset = screenPt.x - compPt.x;
			int yOffset = screenPt.y - compPt.y + hd.getBounds().height;

		    final Point pt = ac.getBounds().getLocation();
		    pt.translate(xOffset, yOffset);
		    
		    // This prevent the dialog from being positioned completely outside the parent panel
		    pt.x = Math.max(pt.x, getTableScrollPane().getLocationOnScreen().x - editDialog.getBounds().width);
		    pt.x = Math.min(pt.x, getTableScrollPane().getLocationOnScreen().x + getTableScrollPane().getBounds().width);
			
		    // Show the dialog right below the column header
		    editDialog.setLocation(pt);
		}
	}

	protected void disposeEditDialog() {
		synchronized (lock) {
			if (editDialog != null) {
				editDialog.dispose();
				editDialog = null;
			}
		}
	}

	private void updatePreviewTable(final Sheet sheet) throws IOException {
		final PreviewTableModel newModel = parseExcel(sheet, startLine);
		
		if (newModel.getRowCount() > 0) {
			final String sheetName = sheet.getSheetName();
			
			// ML: Custom decimal format
			dataTypes = TypeUtil.guessSheetDataTypes(newModel, decimalSeparator);
			types = TypeUtil.guessTypes(importType, newModel, dataTypes, null);
			listDelimiters = new String[newModel.getColumnCount()];
			namespaces = TypeUtil.getPreferredNamespaces(types);
			
			updatePreviewTable(newModel, sheetName);
		}
		
		if (getPreviewTable() == null)
			throw new IllegalStateException("No data found in the Excel sheets.");
	}
	
	private void updatePreviewTable(final PreviewTableModel newModel, final String name) {
		getPreviewTable().setName(name);
		getPreviewTable().setModel(newModel);
		
		ColumnResizer.adjustColumnPreferredWidths(getPreviewTable());
		updatePreviewTable();
	}
	
	protected void updatePreviewTable() {
		getPreviewTable().revalidate();
		getPreviewTable().repaint();
		getPreviewTable().getTableHeader().resizeAndRepaint();
		
		getSelectAllButton().setEnabled(false);
		getSelectNoneButton().setEnabled(false);
		
		if (types != null) {
			getSelectAllButton().setEnabled(Arrays.asList(types).contains(NONE));
			
			for (SourceColumnSemantic t : types) {
				if (t != NONE) {
					getSelectNoneButton().setEnabled(true);
					break;
				}
			}
		}
	}
	
	private JComboBox<Sheet> getSheetComboBox() {
		if (sheetComboBox == null) {
			sheetComboBox = new JComboBox<>();
			sheetComboBox.setVisible(false);
			sheetComboBox.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					setText(((Sheet)value).getSheetName());
					
					return this;
				}
			});
			sheetComboBox.addActionListener(evt -> {
				if (!updating) {
					disposeEditDialog();
					final Sheet sheet = (Sheet) sheetComboBox.getSelectedItem();
					
					try {
						if (sheet != null)
							updatePreviewTable(sheet);
					} catch (IOException e) {
						logger.error("Cannot preview Excel sheet '" + sheet.getSheetName() + "'.", e);
					}
				}
			});
		}
		
		return sheetComboBox;
	}
	
	private JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				disposeEditDialog();
				
				// Replace types "NONE" with new guessed types.
				// NOTE: This must not change the current data types!
				final Set<SourceColumnSemantic> ignoredTypes = new HashSet<>(Arrays.asList(types));
				final SourceColumnSemantic[] newTypes =
						TypeUtil.guessTypes(importType, getPreviewTable().getModel(), dataTypes, ignoredTypes);
				
				for (int i = 0; i < newTypes.length; i++) {
					if (types.length > i && types[i] == NONE)
						setType(i, newTypes[i]);
				}
				
				updatePreviewTable();
			});
			
			if (isAquaLAF()) {
				selectAllButton.putClientProperty("JButton.buttonType", "gradient");
				selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			}
			
			selectAllButton.setEnabled(false);
		}
		
		return selectAllButton;
	}
	
	private JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(evt -> {
				disposeEditDialog();
				fillTypes(NONE);
				updatePreviewTable();
			});
			
			if (isAquaLAF()) {
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
				selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
			}
			
			selectNoneButton.setEnabled(false);
		}
		
		return selectNoneButton;
	}
	
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			tableScrollPane = new JScrollPane(getPreviewTable());
			tableScrollPane.getHorizontalScrollBar().addAdjustmentListener(evt -> {
				// Realign the Attribute Editor Dialog if it is open
				if (!evt.getValueIsAdjusting())
					positionEditDialog();
			});
		}
		
		return tableScrollPane;
	}
	
	// ML : public // END ML
	public class PreviewTableModel extends DefaultTableModel {
		
		private boolean firstRowNames;
		private Vector<Class<?>> predefinedClasses;

		// ML
		public PreviewTableModel(final Vector<Vector<String>> data, final Vector<String> columnNames,
				Vector<Class<?>> columnTypes, final boolean firstRowNames) {
			super(data, columnNames);
			this.firstRowNames = firstRowNames;
			this.predefinedClasses = columnTypes;
		}
		// END OF ML

		public PreviewTableModel(final Vector<Vector<String>> data, final Vector<String> columnNames,
				final boolean firstRowNames) {
			// ML : changed
			this(data,columnNames, null, firstRowNames);
			// END ML
		}
		
		// ML
		public boolean hasPredefinedTypes() {
			return predefinedClasses != null;
		}
		
		public Class<?> getPredefinedColumnClass(final int column) {
			if(predefinedClasses != null && column < predefinedClasses.size()) {
				return predefinedClasses.get(column);
			}
			
			return String.class;
		}
		// END ML

		@SuppressWarnings("unchecked")
		public void setColumnName(final int column, final String name) {
			if (columnIdentifiers.isEmpty())
				columnIdentifiers.setSize(getColumnCount());
			
			if (columnIdentifiers.size() > column) {
				columnIdentifiers.set(column, name);
				this.fireTableChanged(new TableModelEvent(this));
			}
		}

		public void setFirstRowNames(final boolean firstRowNames) {
			this.firstRowNames = firstRowNames;
			this.fireTableStructureChanged();
		}
		
		public boolean isFirstRowNames() {
			return firstRowNames;
		}
		
		@Override
		public int getRowCount() {
			return firstRowNames ? dataVector.size() - 1 : dataVector.size();
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public int getColumnCount() {
			return dataVector.size() > 0 ? ((Vector<String>) dataVector.get(0)).size() : 0;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public String getColumnName(final int column) {
			String colName = null;
			
			// First check is the name has been overwritten by the user
			if (columnIdentifiers.size() > column)
				colName = (String) columnIdentifiers.get(column);
			
			if (colName == null) {
				if (firstRowNames && dataVector.size() > 0) {
					// No overwritten name and should use the first data row as column names
					final Vector<String> firstRow = (Vector<String>) dataVector.get(0);
					
					if (firstRow != null && firstRow.size() > column) {
						colName = firstRow.get(column);
					}
				}
			}
			
			if(colName == null) {
				// Just return a default name
				colName = "Column " + (column + 1);
			}
			
			return colName;
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return false;
		}

		@Override
		public Object getValueAt(int row, final int column) {
			if (firstRowNames)
				row++;
			
			return super.getValueAt(row, column);
		}

		@Override
		public void setValueAt(final Object aValue, int row, final int column) {
			if (firstRowNames)
				row++;
			
			super.setValueAt(aValue, row, column);
		}
	}
	
	private class PreviewTableHeaderRenderer extends JPanel implements TableCellRenderer {
		
		private final JLabel typeLabel;
		private final JLabel nameLabel;
		private final JLabel editLabel;
		
		PreviewTableHeaderRenderer() {
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setBackground(UIManager.getColor("TableHeader.background"));
			
			nameLabel = new JLabel();
			nameLabel.setFont(UIManager.getFont("TableHeader.font"));
			
			typeLabel = new JLabel();
			typeLabel.setFont(iconManager.getIconFont(ICON_FONT_SIZE));
			
			editLabel = new JLabel(IconManager.ICON_CARET_LEFT);
			editLabel.setFont(iconManager.getIconFont(12.0f));
			editLabel.setHorizontalAlignment(JLabel.CENTER);
			
			// Forces the edit label to always have the same size, no matter its state
			final JLabel tempLabel = new JLabel(IconManager.ICON_CARET_DOWN);
			tempLabel.setFont(iconManager.getIconFont(12.0f));
			LookAndFeelUtil.equalizeSize(editLabel, tempLabel);
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(6)
					.addComponent(typeLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(nameLabel)
					.addGap(5, 5, Short.MAX_VALUE)
					.addComponent(editLabel)
					.addGap(6)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(4)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(typeLabel)
							.addComponent(nameLabel)
							.addComponent(editLabel)
					)
					.addGap(4)
			);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS,
		                                               boolean hasF, int row, int col) {
			nameLabel.setText(val != null ? val.toString() : "");
			
			Color fgColor = UIManager.getColor("TableHeader.foreground");
			
			// Set type icon
			if (types != null && types.length > col) {
				SourceColumnSemantic type = types[col];
				
				if (type == null)
					type = NONE;
				
				 final AttributeDataType dataType = dataTypes != null && dataTypes.length > col ?
						 dataTypes[col] : TYPE_STRING;
				
				typeLabel.setForeground(type.getForeground());
				typeLabel.setText(type.getText());
				
				setToolTipText("<html>" + type.getDescription() + " - <i>" + dataType.getDescription() + "</i></html>");
				
				if (type == NONE)
					fgColor = UIManager.getColor("TextField.inactiveForeground");
			} else {
				fgColor = UIManager.getColor("TextField.inactiveForeground");
			}
			
			if (editDialog != null && editDialog.index == col && editDialog.isVisible())
				editLabel.setText(IconManager.ICON_CARET_DOWN);
			else
				editLabel.setText(IconManager.ICON_CARET_LEFT);
			
			nameLabel.setForeground(fgColor);
			
			this.invalidate();

			return this;
		}
	}
	
	private class PreviewTableCellRenderer extends DefaultTableCellRenderer {
		
		public PreviewTableCellRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		                                               boolean hasFocus, int row, int column) {
			setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
			
			setFont(getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			// Modification ML:
			//setFont(getFont().deriveFont(getColumnIndex(KEY) == column ? Font.BOLD : Font.PLAIN));
			setFont(getFont().deriveFont(Font.PLAIN));
			
			setText(value == null ? "" : value.toString());

			if (isImported(column))
				setForeground(table.getForeground());
			else
				setForeground(UIManager.getColor("TextField.inactiveForeground"));
			
			final AttributeDataType dataType = getDataType(column);
			
			if (dataType == TYPE_INTEGER || dataType == TYPE_LONG || dataType == TYPE_FLOATING)
				setHorizontalAlignment(JLabel.RIGHT);
			else if (dataType == TYPE_BOOLEAN)
				setHorizontalAlignment(JLabel.CENTER);
			else
				setHorizontalAlignment(JLabel.LEFT);
			
			return this;
		}
	}
	
	private class EditDialog extends JDialog {
		
		final int index;
		
		EditDialog(final Window parent, final ModalityType modType, int index) {
			super(parent, modType);
			this.index = index;
		}
	}
}
