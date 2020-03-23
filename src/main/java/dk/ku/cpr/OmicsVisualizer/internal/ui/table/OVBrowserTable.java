package dk.ku.cpr.OmicsVisualizer.internal.ui.table;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.model.CyColumn;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

@SuppressWarnings("serial")
public class OVBrowserTable extends JTable implements MouseListener {
	
	private final TableCellRenderer cellRenderer;

	// For right-click menu
	private final PopupMenuHelper popupMenuHelper;
	private JPopupMenu rightClickPopupMenu;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public OVBrowserTable(OVManager ovManager) {
		cellRenderer = new OVTableCellRenderer(ovManager);
		
		popupMenuHelper = new PopupMenuHelper(ovManager);
		
		addMouseListener(this);
		setAutoCreateColumnsFromModel(false);
		setAutoCreateRowSorter(true);
		setCellSelectionEnabled(true);
		setShowGrid(false);
		
		this.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				maybeShowHeaderPopup(e);
			}
			@Override
			public void mouseReleased(final MouseEvent e) {
				maybeShowHeaderPopup(e);
			}
		});
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public synchronized void addMouseListener(final MouseListener listener) {
		// Hack to prevent selected rows from being deselected when the user
		// CONTROL-clicks one of those rows on Mac (popup trigger).
		super.addMouseListener(new ProxyMouseListener(listener));
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cellRenderer;
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return false;
	}
	
	@Override
	public JToolTip createToolTip() {
		MultiLineToolTip tip = new MultiLineToolTip();
		tip.setMaximumSize(new Dimension(480, 320));
		tip.setComponent(this);
		
		return tip;
	}

	/**
	 * This method initializes rightClickPopupMenu
	 * 
	 * @return the inilialised pop-up menu
	 */
	public JPopupMenu getPopupMenu() {
		if (rightClickPopupMenu != null)
			return rightClickPopupMenu;

		rightClickPopupMenu = new JPopupMenu();

		return rightClickPopupMenu;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		selectFocusedCell(e);
		maybeShowPopup(e);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	// ==[ PRIVATE METHODS ]============================================================================================

	
	private void maybeShowHeaderPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			final int column = getColumnModel().getColumnIndexAtX(e.getX());
			final OVTableModel tableModel = (OVTableModel) getModel();

			// Make sure the column we're clicking on actually exists!
			if (column >= tableModel.getColumnCount() || column < 0)
				return;

			final CyColumn cyColumn = tableModel.getColumn(convertColumnIndexToModel(column));
			popupMenuHelper.createColumnHeaderMenu(cyColumn, this,
					e.getX(), e.getY());
		}
	}
	
	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (LookAndFeelUtil.isWindows())
				selectFocusedCell(e);
			
			// Show context menu
			final int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
			final int viewRow = e.getY() / getRowHeight();
			final int modelColumn = convertColumnIndexToModel(viewColumn);
			final int modelRow = convertRowIndexToModel(viewRow);
			
			final OVTableModel tableModel = (OVTableModel) this.getModel();

			// Make sure the column and row we're clicking on actually exists!
			if (modelColumn >= tableModel.getColumnCount() || modelRow >= tableModel.getRowCount())
				return;
			
			final CyColumn cyColumn = tableModel.getColumn(modelColumn);
			final Object primaryKeyValue =tableModel.getDisplayedRowKeys().get(modelRow);
			popupMenuHelper.createTableCellMenu(cyColumn, primaryKeyValue, this,
					e.getX(), e.getY(), this);
		}
	}
	
	private void selectFocusedCell(final MouseEvent e) {
		final int row = e.getY() / getRowHeight();
		final int column = getColumnModel().getColumnIndexAtX(e.getX());
		
		final int[] selectedRows = this.getSelectedRows();
		int binarySearch = Arrays.binarySearch(selectedRows, row);
		
		// Select clicked cell, if not selected yet
		if (binarySearch < 0) {
			// Clicked row not selected: Select only the right-clicked row/cell
			this.changeSelection(row, column, false, false);
		} else {
			final OVTableModel tableModel = (OVTableModel) this.getModel();
			tableModel.fireTableRowsUpdated(selectedRows[0], selectedRows[selectedRows.length-1]);
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ProxyMouseListener extends MouseAdapter {
		
		private final MouseListener listener;
		
		public ProxyMouseListener(final MouseListener listener) {
			this.listener = listener;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// In order to prevent selected rows from being deselected when the user
			// CONTROL-clicks one of those rows on Mac.
			if (listener instanceof OVBrowserTable || !e.isPopupTrigger())
				listener.mouseClicked(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			listener.mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			listener.mouseExited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// In order to prevent selected rows from being deselected when the user
			// CONTROL-clicks one of those rows on Mac.
			if (listener instanceof OVBrowserTable || !e.isPopupTrigger())
				listener.mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			listener.mouseReleased(e);
		}
	}
}
