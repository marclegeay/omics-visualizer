package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ColumnCellRenderer implements ListCellRenderer<CyColumn> {
	
	private OVManager ovManager;
	
	public ColumnCellRenderer(OVManager ovManager) {
		this.ovManager = ovManager;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends CyColumn> list, CyColumn value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		JLabel label = new JLabel(value.getNameOnly());
		label.setIcon(this.ovManager.getService(CyColumnPresentationManager.class).getColumnPresentation(value.getNamespace()).getNamespaceIcon());
		
		JPanel mainPanel = new JPanel();
		// we add a padding right
		mainPanel.setBorder(new EmptyBorder(0,0,0,5));
		mainPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.setInsets(0, 0, 0, 0);

		mainPanel.add(label, c.expandHorizontal());

		// We try do display the same as Cytoscape display types
		String type = value.getType().getSimpleName(); // If there is a new type at least we display the class name
		if(value.getType() == Integer.class) {
			type = "1";
		} else if(value.getType() == Long.class) {
			type = "123";
		} else if(value.getType() == Double.class) {
			type = "1.0";
		} else if(value.getType() == String.class) {
			type = "ab";
		} else if(value.getType() == Boolean.class) {
			type = "y/n";
		} else if(value.getType() == List.class) {
			if(value.getListElementType() == Integer.class) {
				type = "[ 1 ]";
			} else if(value.getListElementType() == Long.class) {
				type = "[ 123 ]";
			} else if(value.getListElementType() == Double.class) {
				type = "[ 1.0 ]";
			} else if(value.getListElementType() == String.class) {
				type = "[ ab ]";
			} else if(value.getListElementType() == Boolean.class) {
				type = "[ y/n ]";
			}
		}
		JLabel labelType = new JLabel(type);
		labelType.setFont(new Font("Serif", Font.BOLD, 11)); // See dk.ku.cpr.OmicsVisualizer.internal.tableimport.ui.AttributeEditor :: createDataTypeButton(AttributeDataType)
		labelType.setVerticalAlignment(SwingConstants.BOTTOM);
		// we put a left padding to separate the name from its type
		labelType.setBorder(new EmptyBorder(0,10,0,0));


		mainPanel.add(labelType, c.nextCol().noExpand());
		
		if(isSelected) {
			mainPanel.setBackground(list.getSelectionBackground());
			mainPanel.setForeground(list.getSelectionForeground());

			label.setBackground(list.getSelectionBackground());
			label.setForeground(list.getSelectionForeground());

			labelType.setBackground(list.getSelectionBackground());
			labelType.setForeground(list.getSelectionForeground());
		} else {
			mainPanel.setBackground(list.getBackground());
			mainPanel.setForeground(list.getForeground());

			label.setBackground(list.getBackground());
			label.setForeground(list.getForeground());

			labelType.setBackground(list.getBackground());
			labelType.setForeground(list.getForeground());
		}
		
		mainPanel.setEnabled(list.isEnabled());
		mainPanel.setFont(list.getFont());
		
		label.setEnabled(list.isEnabled());
		label.setFont(list.getFont());
		
		labelType.setEnabled(list.isEnabled());
		

		return mainPanel;
	}
	
}
