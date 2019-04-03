package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterCriteria;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterSet;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterSet.OVFilterSetType;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class OVFilterSetPanel extends OVFilterPanel implements ActionListener {
	private static final long serialVersionUID = 1589754795936897368L;
	
	private OVManager ovManager;

	private JComboBox<OVFilterSetType> selectType;
	protected List<OVFilterPanel> subFilterPanels;
	protected List<JButton> delButtons;
	private JButton addButton;
	
	private IconManager iconManager;
	
	private static String ICON_ADD = IconManager.ICON_PLUS;
	private static String ICON_DEL = IconManager.ICON_MINUS;

	public OVFilterSetPanel(OVFilterPanel parent, OVTable ovTable, OVManager ovManager) {
		super(parent, ovTable);
		this.ovManager = ovManager;
		
		this.iconManager = this.ovManager.getService(IconManager.class);
		
		this.setBorder(LookAndFeelUtil.createPanelBorder());
		
		this.selectType = new JComboBox<>(OVFilterSetType.values());

		this.subFilterPanels = new ArrayList<>();
		this.delButtons = new ArrayList<>();
		
		this.addButton = new JButton(ICON_ADD);
		Font buttonFont = this.addButton.getFont();
		this.addButton.setFont(this.iconManager.getIconFont(buttonFont.getSize()));
		this.addButton.addActionListener(this);
		this.addButton.setToolTipText("Add a new condition...");
		
		this.addSubFilterCriteria();
		
		this.init();
	}
	
	@Override
	public OVFilter getFilter() {
		if(this.subFilterPanels.isEmpty()) {
			return null;
		} else if(this.subFilterPanels.size() == 1) {
			return this.subFilterPanels.get(0).getFilter();
		} else {
			OVFilterSet filterSet = new OVFilterSet();
			
			filterSet.setType((OVFilterSetType) this.selectType.getSelectedItem());
			
			for(OVFilterPanel subFilterPanel : this.subFilterPanels) {
				filterSet.addFilter(subFilterPanel.getFilter());
			}
			
			return filterSet;
		}
	}

	@Override
	public void setFilter(OVFilter ovFilter) {
		if(ovFilter == null) {
			// Do nothing
		} else if(ovFilter instanceof OVFilterSet) {
			OVFilterSet filterSet = (OVFilterSet) ovFilter;

			// We reset the elements
			this.subFilterPanels = new ArrayList<>();
			this.delButtons = new ArrayList<>();
			
			this.selectType.setSelectedItem(filterSet.getType());
			
			int i=0;
			for(OVFilter subFilter : filterSet.getFilters()) {
				this.addSubFilterSet();
				this.subFilterPanels.get(i).setFilter(subFilter);
				++i;
			}
		} else if(ovFilter instanceof OVFilterCriteria) {
			// We reset the elements
			this.subFilterPanels = new ArrayList<>();
			this.delButtons = new ArrayList<>();

			this.addSubFilterCriteria();
			this.subFilterPanels.get(0).setFilter(ovFilter);
		} else {
			throw new ClassCastException("Cannot cast " + ovFilter.getClass().getName() + " into OVFilterCriteria.");
		}
	}
	
	private void addDelButton() {
		JButton del = new JButton(ICON_DEL);
		Font buttonFont = del.getFont();
		del.setFont(this.iconManager.getIconFont(buttonFont.getSize()));
		del.addActionListener(this);
		del.setToolTipText("Delete the condition.");
		this.delButtons.add(del);
	}
	
	protected void addSubFilterCriteria() {
		this.addDelButton();

		this.subFilterPanels.add(new OVFilterCriteriaPanel(this, this.ovTable));
	}
	
	protected void addSubFilterSet() {
		if(this.subFilterPanels.size() == 1) {
			// If there is only one sub-filter, it may be a OVFilterCriteria
			// But we want it as a OVFilterSet with one criteria
			OVFilterPanel filterPanel = this.subFilterPanels.get(0);
			if(filterPanel instanceof OVFilterCriteriaPanel) {
				this.subFilterPanels.set(0, new OVFilterSetPanel(this, this.ovTable, this.ovManager));
				this.subFilterPanels.get(0).setFilter(filterPanel.getFilter());
			}
		}
		
		this.addDelButton();
		
		this.subFilterPanels.add(new OVFilterSetPanel(this, this.ovTable, this.ovManager));
		
		// We have to put this after, so that the "checkSubFilter" does not break it
	}
	
	/**
	 * Checks if the subFilter is useful.
	 * A set S containing only one set S', containing only one filter F is useless.
	 * We modify it so that S contains only F.
	 */
	private void checkSubFilters() {
		if(this.subFilterPanels.size() == 1) {
			if(this.subFilterPanels.get(0) instanceof OVFilterSetPanel) {
				OVFilterSetPanel subFilterPanel = (OVFilterSetPanel) this.subFilterPanels.get(0);
				if(subFilterPanel.subFilterPanels.size() == 1) {
					this.subFilterPanels.set(0, subFilterPanel.subFilterPanels.get(0));
				}
			}
		}
	}
	
	protected void init() {
		if(this.subFilterPanels.size() != this.delButtons.size()) {
			throw new RuntimeException("The number of sub-filter panels (" + this.subFilterPanels.size() + ") does not match the number of del buttons (" + this.delButtons.size() + ").");
		}
		
		this.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.setInsets(0, 0, 0, 0);
		c.setAnchor("NW");
		
		if(this.subFilterPanels.size() > 1) {
			JPanel typePanel = new JPanel();
			typePanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
			typePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			typePanel.add(new JLabel("Matches:"));
			typePanel.add(this.selectType);
			
			this.add(typePanel, c.useNCols(2).expandHorizontal());
			c.useNCols(1);
			
			// We use this to align buttons
			Insets insets = this.getInsets();
			
			for(int i=0; i<this.subFilterPanels.size(); ++i) {
				// We try to align the "del" button with the first element of the subFilter
				c.setInsets(insets.top, insets.left, insets.bottom, insets.right);
				this.add(this.delButtons.get(i), c.nextRow().noExpand().setAnchor("NW"));
				c.setInsets(0, 0, 0, 0);
				
				this.add(this.subFilterPanels.get(i), c.nextCol().noExpand().setAnchor("NW"));
			}
			
			// We put the same insets for the "add" button so that is is aligne with the "dell" buttons
			c.setInsets(insets.top, insets.left, insets.bottom, insets.right);
		} else { // 0 or only 1 subFilter
			if(!this.subFilterPanels.isEmpty()) {
				this.add(this.subFilterPanels.get(0), c.noExpand().setAnchor("NW"));
			}
			c.setInsets(0, 0, 0, 0);
		}
		
		this.add(this.addButton, c.nextRow().useNCols(2).noExpand().setAnchor("NW"));
		c.useNCols(1);
		
		// TODO fix it: A dummy panel so that it the displayed components are on the top-left corner
		JPanel empty = new JPanel();
		empty.setPreferredSize(new Dimension(0, 0));
		empty.setOpaque(!LookAndFeelUtil.isAquaLAF());
		this.add(empty, c.nextRow().useNCols(2).expandBoth());
	}

	@Override
	public void update(boolean up) {
		this.removeAll();
		
		checkSubFilters();
		init();
		
		if(up && this.parent != null) {
			this.parent.update(up);
		} else if(!up) {
			for(OVFilterPanel subFilter : this.subFilterPanels) {
				subFilter.update(up);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.addButton) {
			this.addSubFilterSet();
			
			this.update(true);
		} else {
			// We look for the del button clicked
			int i=0;
			while(i<this.delButtons.size() && e.getSource() != this.delButtons.get(i)) {
				++i;
			}

			if(i<this.delButtons.size()) {
				this.delButtons.remove(i);
				this.subFilterPanels.remove(i);

				this.update(true);
			}
		}
	}
}
