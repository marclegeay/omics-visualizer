package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend.OVLegendPosition;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVLegend.OVLegendOrientation;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.task.DrawLegendTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.task.DeleteLegendTaskFactory;

public class OVLegendWindow extends OVWindow implements ActionListener {
	private static final long serialVersionUID = -5817928941762399915L;
	
	private OVConnection ovCon;
	
	// Viz panel
	private JCheckBox includeInnerViz;
	private JCheckBox includeOuterViz;
	
	// Legend panel
	private JTextField title;
	private JCheckBox showTitle;
	private JComboBox<String> font;
	private JTextField fontSize;
	private JComboBox<LegendPosition> position;
	private JComboBox<LegendHAlignment> alignmentH;
	private JComboBox<LegendVAlignment> alignmentV;
	private JComboBox<OVLegendOrientation> orientation;
	
	// Common panel
	private JButton closeButton;
	private JButton showButton;
	private JButton hideButton;
	
	private static final String CREATE_TEXT = "Create legend";
	private static final String RELOAD_TEXT = "Reload legend";

	public OVLegendWindow(OVManager ovManager) {
		super(ovManager, "Legend");
		
		// ---
		// Viz panel
		// ---
		this.includeInnerViz = new JCheckBox("Include Inner Visualization");
		this.includeOuterViz = new JCheckBox("Include Outer Visualization");
		
		// ---
		// Legend panel
		// ---
		this.initForm();
		
		// ---
		// Button panel
		// ---
		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this);

		this.showButton = new JButton(CREATE_TEXT);
		this.showButton.addActionListener(this);
		
		this.hideButton = new JButton("Delete legend");
		this.hideButton.addActionListener(this);

		LookAndFeelUtil.equalizeSize(this.closeButton, this.showButton, this.hideButton);
		
		this.setResizable(true);
	}
	
	private void initForm() {
		this.title = new JTextField(OVLegend.DEFAULT_TITLE);
		this.showTitle = new JCheckBox("Show title");
		this.showTitle.addActionListener(this);
		this.showTitle.setSelected(OVLegend.DEFAULT_SHOW_TITLE);
		this.font = new JComboBox<>(OVShared.getAvailableFontNames());
		this.font.setSelectedItem(OVLegend.DEFAULT_FONT.getFamily());
		this.fontSize = new JTextField(String.valueOf(OVLegend.DEFAULT_FONT_SIZE));
		this.position = new JComboBox<>(LegendPosition.values());
		this.position.addActionListener(this);
		this.alignmentH = new JComboBox<>(LegendHAlignment.values());
		this.alignmentV = new JComboBox<>(LegendVAlignment.values());
		this.orientation = new JComboBox<>(OVLegendOrientation.values());

		if(this.position.getSelectedItem().equals(LegendPosition.NORTH)
				|| this.position.getSelectedItem().equals(LegendPosition.SOUTH)) {
			this.alignmentH.setVisible(true);
			this.alignmentV.setVisible(false);
		} else {
			this.alignmentH.setVisible(false);
			this.alignmentV.setVisible(true);
		}
	}
	
	private JPanel getVizPanel() {
		JPanel vizPanel = new JPanel();
		vizPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		vizPanel.setBorder(LookAndFeelUtil.createTitledBorder("Visualizations properties"));
		
		vizPanel.setLayout(new GridBagLayout());

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setAnchor("C");
		
		vizPanel.add(this.includeInnerViz, c);
		vizPanel.add(this.includeOuterViz, c.nextRow());
		
		return vizPanel;
	}
	
	private JPanel getLegendPanel() {
		JPanel legendPanel = new JPanel();
		legendPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		legendPanel.setBorder(LookAndFeelUtil.createTitledBorder("Legend properties"));
		
		legendPanel.setLayout(new GridBagLayout());

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setAnchor("C");
		
		legendPanel.add(new JLabel("Title:"), c);
		legendPanel.add(this.title, c.nextCol());
		legendPanel.add(this.showTitle, c.nextCol());
		
		legendPanel.add(new JLabel("Font:"), c.nextRow());
		c.useNCols(2);
		legendPanel.add(this.font, c.nextCol());
		c.useNCols(1);
		
		legendPanel.add(new JLabel("Font size:"), c.nextRow());
		c.useNCols(2);
		legendPanel.add(this.fontSize, c.nextCol());
		c.useNCols(1);
		
		legendPanel.add(new JLabel("Position:"), c.nextRow());
		c.useNCols(2);
		legendPanel.add(this.position, c.nextCol());
		c.useNCols(1);
		
		legendPanel.add(new JLabel("Alignment:"), c.nextRow());
		c.useNCols(2);
		legendPanel.add(this.alignmentH, c.nextCol());
		legendPanel.add(this.alignmentV, c); // we put them at the same place
		c.useNCols(1);
		
		legendPanel.add(new JLabel("Color legend:"), c.nextRow());
		c.useNCols(2);
		legendPanel.add(this.orientation, c.nextCol());
		c.useNCols(1);
		
		return legendPanel;
	}

	public void init(OVConnection ovCon) {
		this.init(ovCon, false);
	}
	
	public void init(OVConnection ovCon, boolean reset) {
		this.ovCon = ovCon;
		
		if(ovCon == null) {
			return;
		}
		
		// ------------------------
		// Init form default values
		// ------------------------

		this.includeInnerViz.setEnabled(ovCon.getInnerVisualization()!=null);
		
		this.includeOuterViz.setEnabled(ovCon.getOuterVisualization()!=null);
		
		if(!reset && ovCon.getLegend() != null) {
			OVLegend legend = ovCon.getLegend();
			
			this.showButton.setText(RELOAD_TEXT);

			this.includeInnerViz.setSelected(this.includeInnerViz.isEnabled() && legend.isDrawInner());
			this.includeOuterViz.setSelected(this.includeOuterViz.isEnabled() && legend.isDrawOuter());
			
			this.title.setText(legend.getTitle());
			this.showTitle.setSelected(legend.isTitleShown());
			this.font.setSelectedItem(legend.getFont().getFamily());
			this.fontSize.setText(String.valueOf(legend.getFontSize()));
			
			OVLegendPosition ovPosition = legend.getPosition();
			LegendPosition pos=LegendPosition.NORTH;
			LegendHAlignment alignH=LegendHAlignment.LEFT;
			LegendVAlignment alignV=LegendVAlignment.TOP;
			switch(ovPosition) {
			case EAST:
				pos = LegendPosition.EAST;
				alignV = LegendVAlignment.MIDDLE;
				break;
			case EAST_BOTTOM:
				pos = LegendPosition.EAST;
				alignV = LegendVAlignment.BOTTOM;
				break;
			case EAST_TOP:
				pos = LegendPosition.EAST;
				alignV = LegendVAlignment.TOP;
				break;
			case NORTH:
				pos = LegendPosition.NORTH;
				alignH = LegendHAlignment.CENTER;
				break;
			case NORTH_LEFT:
				pos = LegendPosition.NORTH;
				alignH = LegendHAlignment.LEFT;
				break;
			case NORTH_RIGHT:
				pos = LegendPosition.NORTH;
				alignH = LegendHAlignment.RIGHT;
				break;
			case SOUTH:
				pos = LegendPosition.SOUTH;
				alignH = LegendHAlignment.CENTER;
				break;
			case SOUTH_LEFT:
				pos = LegendPosition.SOUTH;
				alignH = LegendHAlignment.LEFT;
				break;
			case SOUTH_RIGHT:
				pos = LegendPosition.SOUTH;
				alignH = LegendHAlignment.RIGHT;
				break;
			case WEST:
				pos = LegendPosition.WEST;
				alignV = LegendVAlignment.MIDDLE;
				break;
			case WEST_BOTTOM:
				pos = LegendPosition.WEST;
				alignV = LegendVAlignment.BOTTOM;
				break;
			case WEST_TOP:
				pos = LegendPosition.WEST;
				alignV = LegendVAlignment.TOP;
				break;
			}

			this.position.setSelectedItem(pos);
			this.alignmentH.setSelectedItem(alignH);
			this.alignmentV.setSelectedItem(alignV);
			
			this.orientation.setSelectedItem(legend.getOrientation());
		} else {
			this.initForm();
			
			this.showButton.setText(CREATE_TEXT);
			
			// We put the name of the Network as the default title
			this.title.setText(ovCon.getBaseNetwork().toString());
			this.showTitle.setSelected(OVLegend.DEFAULT_SHOW_TITLE);
			
			this.includeInnerViz.setSelected(this.includeInnerViz.isEnabled());
			this.includeOuterViz.setSelected(this.includeOuterViz.isEnabled());
		}
		
		this.hideButton.setEnabled(ovCon != null && ovCon.getLegend() != null);
		
		// -----------
		// Init panels
		// -----------
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setAnchor("C");
		
		JPanel vizPanel = this.getVizPanel();
		if(vizPanel != null) {
			mainPanel.add(vizPanel, c);
		}
		
		JPanel legendPanel = this.getLegendPanel();
		if(legendPanel != null) {
			mainPanel.add(legendPanel, c.nextRow());
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.closeButton);
		buttonPanel.add(this.hideButton);
		buttonPanel.add(this.showButton);

		this.setContentPane(new JPanel());
		this.setLayout(new BorderLayout());

		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		// ------------------
		// Check availability
		// ------------------
		
		// Everything (except Close and Hide) should be disabled if there is no Viz
		boolean enabled = this.ovCon.getInnerVisualization() != null || this.ovCon.getOuterVisualization() != null;
		this.title.setEnabled(enabled && this.showTitle.isSelected());
		this.showTitle.setEnabled(enabled);
		this.font.setEnabled(enabled);
		this.fontSize.setEnabled(enabled);
		this.position.setEnabled(enabled);
		this.alignmentH.setEnabled(enabled);
		this.alignmentV.setEnabled(enabled);
		this.orientation.setEnabled(enabled);
		
		this.showButton.setEnabled(enabled);
		
		// -----------------
		// Pack and position
		// -----------------
		
		this.pack();
		this.setLocationRelativeTo(ovManager.getService(CySwingApplication.class).getJFrame());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.showTitle) {
			this.title.setEnabled(this.showTitle.isSelected());
		} else if(e.getSource() == this.closeButton) {
			this.setVisible(false);
		} else if(e.getSource() == this.showButton) {
			int intFontSize = 0;
			try {
				intFontSize = Integer.parseInt(this.fontSize.getText());
			} catch(NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this, "Error: The font size must be an integer value.", "Formating error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			OVLegendPosition ovPosition = OVLegendPosition.NORTH_LEFT;
			if(this.position.getSelectedItem().equals(LegendPosition.NORTH)) {
				if(this.alignmentH.getSelectedItem().equals(LegendHAlignment.LEFT)) {
					ovPosition = OVLegendPosition.NORTH_LEFT;
				} else if(this.alignmentH.getSelectedItem().equals(LegendHAlignment.CENTER)) {
					ovPosition = OVLegendPosition.NORTH;
				} else if(this.alignmentH.getSelectedItem().equals(LegendHAlignment.RIGHT)) {
					ovPosition = OVLegendPosition.NORTH_RIGHT;
				}
			} else if(this.position.getSelectedItem().equals(LegendPosition.SOUTH)) {
				if(this.alignmentH.getSelectedItem().equals(LegendHAlignment.LEFT)) {
					ovPosition = OVLegendPosition.SOUTH_LEFT;
				} else if(this.alignmentH.getSelectedItem().equals(LegendHAlignment.CENTER)) {
					ovPosition = OVLegendPosition.SOUTH;
				} else if(this.alignmentH.getSelectedItem().equals(LegendHAlignment.RIGHT)) {
					ovPosition = OVLegendPosition.SOUTH_RIGHT;
				}
			} else if(this.position.getSelectedItem().equals(LegendPosition.EAST)) {
				if(this.alignmentV.getSelectedItem().equals(LegendVAlignment.TOP)) {
					ovPosition = OVLegendPosition.EAST_TOP;
				} else if(this.alignmentV.getSelectedItem().equals(LegendVAlignment.MIDDLE)) {
					ovPosition = OVLegendPosition.EAST;
				} else if(this.alignmentV.getSelectedItem().equals(LegendVAlignment.BOTTOM)) {
					ovPosition = OVLegendPosition.EAST_BOTTOM;
				}
			} else if(this.position.getSelectedItem().equals(LegendPosition.WEST)) {
				if(this.alignmentV.getSelectedItem().equals(LegendVAlignment.TOP)) {
					ovPosition = OVLegendPosition.WEST_TOP;
				} else if(this.alignmentV.getSelectedItem().equals(LegendVAlignment.MIDDLE)) {
					ovPosition = OVLegendPosition.WEST;
				} else if(this.alignmentV.getSelectedItem().equals(LegendVAlignment.BOTTOM)) {
					ovPosition = OVLegendPosition.WEST_BOTTOM;
				}
			}
			
			OVLegend legend = new OVLegend(this.includeInnerViz.isSelected() ? this.ovCon.getInnerVisualization() : null,
					this.includeOuterViz.isSelected() ? this.ovCon.getOuterVisualization() : null,
					this.title.getText(),
					this.showTitle.isSelected(),
					(String) this.font.getSelectedItem(),
					intFontSize,
					ovPosition,
					(OVLegendOrientation) this.orientation.getSelectedItem()
			);
			
			this.ovCon.setLegend(legend);
			
			// We update the network view if we create the legend, not if we reload it
			DrawLegendTaskFactory legendFactory = new DrawLegendTaskFactory(ovManager, legend, this.showButton.getText().equals(CREATE_TEXT));
			ovManager.executeTask(legendFactory.createTaskIterator());
			
			this.setVisible(false);
		} else if(e.getSource() == this.hideButton) {
			if(JOptionPane.showConfirmDialog(this, "Delete the legend applied to the network \"" + this.ovCon.getCollectionNetworkName() + "\"?", "Legend deletion confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				this.ovCon.setLegend(null);
				
				DeleteLegendTaskFactory legendFactory = new DeleteLegendTaskFactory(ovManager);
				ovManager.executeTask(legendFactory.createTaskIterator());
				
				this.setVisible(false);
			}
		} else if(e.getSource() == this.position) {
			if(this.position.getSelectedItem().equals(LegendPosition.NORTH)
					|| this.position.getSelectedItem().equals(LegendPosition.SOUTH)) {
				this.alignmentH.setVisible(true);
				this.alignmentV.setVisible(false);
			} else {
				this.alignmentH.setVisible(false);
				this.alignmentV.setVisible(true);
			}
		}
	}
	
	private enum LegendPosition {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}
	
	private enum LegendHAlignment {
		LEFT,
		CENTER,
		RIGHT
	}
	
	private enum LegendVAlignment {
		TOP,
		MIDDLE,
		BOTTOM
	}
}
