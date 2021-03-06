package dk.ku.cpr.OmicsVisualizer.internal.ui.table;

import java.awt.Component;
import java.awt.Font;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

/** Cell renderer for attribute browser table. */
class OVTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -4364566217397320318L;

	// Define fonts & colors for the cells
	private static final int H_PAD = 8;
	private static final int V_PAD = 2;
	private final Font defaultFont;
	private final IconManager iconManager;
	private final CyProperty<Properties> propManager;

	@SuppressWarnings("unchecked")
	public OVTableCellRenderer(OVManager ovManager) {
		this.iconManager = ovManager.getService(IconManager.class);
		this.propManager = ovManager.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		defaultFont = getFont().deriveFont(LookAndFeelUtil.getSmallFontSize());
		setOpaque(true);

		// Add padding:
		Border border = getBorder();

		if (border == null)
			border = BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD);
		else
			border = BorderFactory.createCompoundBorder(border,
					BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD));

		setBorder(border);
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {

		if (value instanceof Boolean)
			setFont(iconManager.getIconFont(12.0f));
		else
			setFont(defaultFont);

		setBackground(UIManager.getColor("Table.background"));
		setVerticalTextPosition(JLabel.CENTER);
		setHorizontalTextPosition(JLabel.CENTER);

		if (value instanceof Boolean)
			setHorizontalAlignment(JLabel.CENTER);
		else
			setHorizontalAlignment(value instanceof Number ? JLabel.RIGHT : JLabel.LEFT);

		// First, set values
		final String displayText;

		if (value instanceof Boolean)
			displayText = value == Boolean.TRUE ? IconManager.ICON_CHECK_SQUARE : IconManager.ICON_SQUARE_O;
		else if (value instanceof Double) {
			final OVTableColumnModel model = (OVTableColumnModel) table.getColumnModel();
			final String colName = table.getColumnName(column);
			String formatStr = model.getColumnFormat(colName);

			if (formatStr == null)
				formatStr = propManager.getProperties().getProperty("floatingPointColumnFormat");
			
			if (formatStr == null)
				displayText = value.toString();
			else
				displayText = String.format(formatStr, value);
		} else if (value != null) {
			displayText = value.toString();
		} else {
			displayText = "";
		}

		setText(displayText);
		String tooltipText = value instanceof Boolean ? value.toString() : displayText;

		setToolTipText(tooltipText);

		// If selected, return
		if (isSelected) {
			if (table.getSelectedColumn() == column && table.getSelectedRow() == row) { // Selected
																						// cell
				setBackground(UIManager.getColor("Table.focusCellBackground"));
				setForeground(UIManager.getColor("Table.focusCellForeground"));
			} else {
				setForeground(UIManager.getColor("Table.selectionForeground"));
				setBackground(UIManager.getColor("Table.selectionBackground"));
			}
		} else {
//			// If non-editable, grey it out.
//			if (table.getModel() instanceof OVTableModel && !table.isCellEditable(0, column))
//				setForeground(UIManager.getColor("TextField.inactiveForeground"));
//			else
				setForeground(UIManager.getColor("Table.foreground"));
		}

		return this;
	}
}
