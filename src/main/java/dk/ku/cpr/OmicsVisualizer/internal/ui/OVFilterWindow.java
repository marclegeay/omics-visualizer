package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.RemoveFilterTaskFactory;

public class OVFilterWindow extends OVWindow implements ActionListener {
	private static final long serialVersionUID = -7306443854568361953L;

	private OVTable ovTable;

	private OVFilterRootPanel rootFilterPanel;

	private JButton okButton;
	private JButton cancelButton;
	private JButton removeButton;

	public OVFilterWindow(OVManager ovManager) {
		super(ovManager);
	}
	
	private void init() {
		this.rootFilterPanel = new OVFilterRootPanel(this, this.ovTable, this.ovManager);

		this.okButton = new JButton("Apply");
		this.okButton.addActionListener(this);

		this.cancelButton = new JButton("Close");
		this.cancelButton.addActionListener(this);

		this.removeButton = new JButton("Delete");
		this.removeButton.addActionListener(this);
		
		LookAndFeelUtil.equalizeSize(this.okButton, this.cancelButton, this.removeButton);
	}

	public void update(boolean center) {
		
		JPanel mainPanel = new JPanel();
//		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		mainPanel.setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.cancelButton);
		buttonPanel.add(this.removeButton);
		buttonPanel.add(this.okButton);
		
		JScrollPane scrollFilter = new JScrollPane(this.rootFilterPanel);
		scrollFilter.setBorder(null);
		scrollFilter.setOpaque(!LookAndFeelUtil.isAquaLAF());
		scrollFilter.getViewport().setOpaque(!LookAndFeelUtil.isAquaLAF());
		
		mainPanel.add(scrollFilter, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.setContentPane(mainPanel);

		this.setPreferredSize(null); // We want to recompute the size each time
		this.pack(); // We pack so that getWidth and getHeight are computed
		// Then we set the size limits ...
		OVCytoPanel ovPanel = this.ovManager.getOVCytoPanel();
		// at most 80% of the Cytoscape window
		int prefWidth = Math.min(this.getWidth()+30, (int) (ovPanel.getTopLevelAncestor().getWidth() * 0.8)); // +30 so that the vertical slide can fit
		int prefHeight = Math.min(this.getHeight(), (int) (ovPanel.getTopLevelAncestor().getHeight() * 0.8));
//		int prefWidth = (int) (ovPanel.getTopLevelAncestor().getWidth() * 0.8);
//		int prefHeight = (int) (ovPanel.getTopLevelAncestor().getHeight() * 0.8);
		
		int curHeight = this.getHeight();
		prefHeight = (prefHeight < curHeight ? prefHeight : curHeight);
		this.setPreferredSize(new Dimension(prefWidth, prefHeight));

		this.pack(); // We recompute the size with the new preferences

		if(ovPanel != null && center) {
			this.setLocationRelativeTo(ovPanel.getTopLevelAncestor());
		}
	}

	@Override
	public void setVisible(boolean b) {
		if(b) {
			if(this.ovManager.getActiveOVTable() != null) {
				this.ovTable = this.ovManager.getActiveOVTable();
			}

			this.setTitle("Filter " + this.ovTable.getTitle());
			
			this.init();

			OVFilter filter = this.ovTable.getFilter();
			if(filter != null) {
				this.rootFilterPanel.setFilter(filter);
			}

			this.removeButton.setEnabled(filter!=null);

			this.update(true);
		}

		super.setVisible(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.okButton) {
			if(!this.rootFilterPanel.isFilterValid()) {
				JOptionPane.showMessageDialog(this, "Error: The filter is not well formatted.", "Filter Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			OVFilter filter = this.rootFilterPanel.getFilter();
			if(filter == null) {
				// The filter should not be null, but we never know...
				JOptionPane.showMessageDialog(this, "Error: The filter is not well formatted.", "Filter Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			this.ovTable.setFilter(filter);
			
			FilterTaskFactory factory = new FilterTaskFactory(this.ovManager);
			this.ovManager.executeTask(factory.createTaskIterator(this.ovTable));

//			this.setVisible(false);
			this.setVisible(true);
		} else if(e.getSource() == this.cancelButton) {
			this.setVisible(false);
		} else if(e.getSource() == this.removeButton) {
			RemoveFilterTaskFactory factory = new RemoveFilterTaskFactory(this.ovManager);
			this.ovManager.executeTask(factory.createTaskIterator(this.ovTable));

//			this.setVisible(false);
			this.setVisible(true);
		}
	}
}
