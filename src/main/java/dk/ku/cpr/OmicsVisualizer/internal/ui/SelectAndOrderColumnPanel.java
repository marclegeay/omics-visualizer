package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class SelectAndOrderColumnPanel extends JPanel implements ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 2600809394923740301L;
	
	private static final float ICON_FONT_SIZE = 14.0f;
	/** Name of the default "no namespace" */
	private static String DefaultNamespace;
	
	private CopyNodeTableWindow parent;
	
	private CyColumnPresentationManager presentationManager;
	private IconManager iconManager;
	
	private NamespaceComparator nsComp;
	
	private Collection<CyColumn> availableColumns;
	
	private NamespaceListModel namespaceModel;
	private SortedColumnListModel unselectedModel;
	private ColumnListModel selectedModel;
	
	private JList<String> availableNamespaces;
	private JList<CyColumn> unselectedCols;
	private JList<CyColumn> selectedCols;

	private JScrollPane unselectedSP;
	private JScrollPane selectedSP;

	private JButton upButton;
	private JButton downButton;
	private JButton leftButton;
	private JButton rightButton;
	private JButton allLeftButton;
	private JButton allRightButton;
	
	public SelectAndOrderColumnPanel(CopyNodeTableWindow parent) {
		super();
		this.parent = parent;
		
		OVManager ovManager = OVManager.getInstance();
		
		this.presentationManager = ovManager.getService(CyColumnPresentationManager.class);
		this.iconManager = ovManager.getService(IconManager.class);
		
		SelectAndOrderColumnPanel.DefaultNamespace = this.presentationManager.getColumnPresentation(null).getNamespaceDescription();
		
		this.nsComp = new NamespaceComparator();

		this.namespaceModel = new NamespaceListModel();
		this.unselectedModel = new SortedColumnListModel();
		this.selectedModel = new ColumnListModel();
		
		this.availableNamespaces = new JList<>(this.namespaceModel);
		this.availableNamespaces.setCellRenderer(new NamespaceCellRenderer());
		this.availableNamespaces.addListSelectionListener(this);
		this.availableNamespaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// The prototype Column is used to give a minimum size to lists
		CyNetwork protoNet = ovManager.getService(CyNetworkFactory.class).createNetworkWithPrivateTables();
		protoNet.getDefaultNodeTable().createColumn("very long column name prototype", String.class, true);
		CyColumn protoCol = protoNet.getDefaultNodeTable().getColumn("very long column name prototype");
		
		this.unselectedCols = new JList<>(this.unselectedModel);
		this.unselectedCols.setCellRenderer(new ColumnCellRenderer(ovManager));
		this.unselectedCols.setPrototypeCellValue(protoCol);
		
		this.selectedCols = new JList<>(this.selectedModel);
		this.selectedCols.setCellRenderer(new ColumnCellRenderer(ovManager));
		this.selectedCols.addListSelectionListener(this);
		this.selectedCols.setPrototypeCellValue(protoCol);
		
		this.unselectedSP = new JScrollPane(this.unselectedCols);
		// We always show the bar, so that the GUI does not "jump" when switching to a namespace with few and a lot of columns
		this.unselectedSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.selectedSP = new JScrollPane(this.selectedCols);
		
		this.upButton = new JButton(IconManager.ICON_ANGLE_UP);
		this.upButton.setFont(this.iconManager.getIconFont(ICON_FONT_SIZE));
		this.upButton.setToolTipText("Move the selected column up");
		this.upButton.addActionListener(this);
		
		this.downButton = new JButton(IconManager.ICON_ANGLE_DOWN);
		this.downButton.setFont(this.iconManager.getIconFont(ICON_FONT_SIZE));
		this.downButton.setToolTipText("Move the selected column down");
		this.downButton.addActionListener(this);
		
		this.leftButton = new JButton(IconManager.ICON_ANGLE_LEFT);
		this.leftButton.setFont(this.iconManager.getIconFont(ICON_FONT_SIZE));
		this.leftButton.setToolTipText("Deselect the highlited columns");
		this.leftButton.addActionListener(this);
		
		this.rightButton = new JButton(IconManager.ICON_ANGLE_RIGHT);
		this.rightButton.setFont(this.iconManager.getIconFont(ICON_FONT_SIZE));
		this.rightButton.setToolTipText("Select the highligted columns");
		this.rightButton.addActionListener(this);
		
		this.allLeftButton = new JButton(IconManager.ICON_ANGLE_LEFT + IconManager.ICON_ANGLE_LEFT);
		this.allLeftButton.setFont(this.iconManager.getIconFont(ICON_FONT_SIZE));
		this.allLeftButton.setToolTipText("Deselect all columns");
		this.allLeftButton.addActionListener(this);
		
		this.allRightButton = new JButton(IconManager.ICON_ANGLE_RIGHT + IconManager.ICON_ANGLE_RIGHT);
		this.allRightButton.setFont(this.iconManager.getIconFont(ICON_FONT_SIZE));
		this.allRightButton.setToolTipText("Select all the namespace columns");
		this.allRightButton.addActionListener(this);
		
		this.draw();
	}
	
	private void draw() {
		this.removeAll();
		
		this.setOpaque(!LookAndFeelUtil.isAquaLAF());
		this.setBorder(LookAndFeelUtil.createTitledBorder("Columns to import"));
		
		JPanel buttonLeftRightPanel = new JPanel();
		buttonLeftRightPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		buttonLeftRightPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints cButtonLR = new MyGridBagConstraints();
		cButtonLR.setInsets(0, 0, 0, 0);
		cButtonLR.expandHorizontal();
		
		buttonLeftRightPanel.add(this.allRightButton, cButtonLR);
		buttonLeftRightPanel.add(this.rightButton, cButtonLR.nextRow());
		buttonLeftRightPanel.add(this.leftButton, cButtonLR.nextRow());
		buttonLeftRightPanel.add(this.allLeftButton, cButtonLR.nextRow());
		
		JPanel buttonUpDownPanel = new JPanel();
		buttonUpDownPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		buttonUpDownPanel.setLayout(new GridBagLayout());
		MyGridBagConstraints cButtonUD = new MyGridBagConstraints();
		cButtonUD.setInsets(0, 0, 0, 0);
		cButtonUD.noExpand();
		
		buttonUpDownPanel.add(this.upButton, cButtonUD);
		buttonUpDownPanel.add(this.downButton, cButtonUD.nextCol());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandBoth();

		// We get rid of the bottom padding
		c.setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, 0, MyGridBagConstraints.DEFAULT_INSET);
		mainPanel.add(new JLabel("Namespaces:"), c);
		mainPanel.add(new JLabel("Available columns:"), c.nextCol());
		mainPanel.add(new JLabel("Selected columns:"), c.nextCol().nextCol());
		mainPanel.add(buttonUpDownPanel, c.nextCol().noExpand());
		c.expandBoth();
		
		// We get rid of the top padding
		c.setInsets(0, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET);
		mainPanel.add(new JScrollPane(this.availableNamespaces), c.nextRow());
		mainPanel.add(this.unselectedSP, c.nextCol());
		mainPanel.add(buttonLeftRightPanel, c.nextCol().noExpand());
		mainPanel.add(this.selectedSP, c.nextCol().expandBoth().useNCols(2));
		c.useNCols(1).nextCol().setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET);
		
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	public void update(Collection<CyColumn> cyCols) {
		this.availableColumns = new ArrayList<>();
		
		// We clear the JLists
		this.namespaceModel.removeAllElements();
		this.unselectedModel.removeAllElements();
		this.selectedModel.removeAllElements();
		
		// selected list is empty
		this.leftButton.setEnabled(false);
		this.allLeftButton.setEnabled(false);
		this.upButton.setEnabled(false);
		this.downButton.setEnabled(false);
		
		// We identify the namespaces
		for(CyColumn col : cyCols) {
			// We only add non-list columns
			if(col.getType() != List.class) {
				this.availableColumns.add(col);
				
				String namespace = presentationManager.getColumnPresentation(col.getNamespace()).getNamespaceDescription();
				this.namespaceModel.add(namespace);
			}
		}
		
		// We select the first namespace
		if(this.availableNamespaces.getSelectedIndex() == 0) {
			// first one already selected, but we force the reload of namespace columns
			// this happens if we change the network
			namespaceChanged();
		} else {
			this.availableNamespaces.setSelectedIndex(0);
		}
	}
	
	public List<CyColumn> getSelectedColumns() {
		return this.selectedModel.getColumnList();
	}
	
	private void namespaceChanged() {
		this.unselectedModel.removeAllElements();
		// We put the scroll back to top
		this.unselectedSP.getVerticalScrollBar().setValue(0);
		// We deselect everything in the column list
		this.unselectedCols.clearSelection();

		String selectedNamespace = this.namespaceModel.getElementAt(this.availableNamespaces.getSelectedIndex());
		this.unselectedModel.setNamespace(selectedNamespace);

		for(CyColumn col : this.availableColumns) {
			// the Model will only add columns from the selected Namespace
			this.unselectedModel.add(col);
		}

		if(this.unselectedModel.getSize()>0) {
			this.rightButton.setEnabled(true);
			this.allRightButton.setEnabled(true);
			this.unselectedCols.setSelectedIndex(0);
		} else {
			this.rightButton.setEnabled(false);
			this.allRightButton.setEnabled(false);
		}

		this.parent.draw();
	}
	
	// selectedIndices: indices of selected rows in 'unselectedCols'
	// -> The indices should be sort ascending
	private void selectColumns(int selectedIndices[]) {
		// We take the columns from the unselected and we put them in the selected

		// getSelectedIndices gives the indices sorting ascending
		// remove will change the indices
		// so if indices 0,2,3 are selected
		// we want to remove 0, then 1, then 1.
		int nbInserted=0;
		// We store the lower index to put the ScrollBar at the level of the item
		int lowerIndex = -1;
		for(int i=0; i<selectedIndices.length; ++i) {
			int trueIndice = selectedIndices[i] - nbInserted;
			if(trueIndice < this.unselectedModel.getSize()) {
				selectedIndices[i] = this.selectedModel.add(this.unselectedModel.removeElement(trueIndice));
				nbInserted++;
				
				if((lowerIndex == -1) || (selectedIndices[i] < lowerIndex)) {
					lowerIndex = selectedIndices[i];
				}
			} else {
				selectedIndices[i] = -1;
			}
		}
		
		// We select the added elements
		this.selectedCols.setSelectedIndices(selectedIndices);
		// We put the ScrollBar at the level of the first selected item
		if(lowerIndex != -1) {
			// We force the ScrollPane to resize
			this.selectedSP.setViewportView(this.selectedCols);
			int nbUnselected = this.selectedModel.getSize();
			int scrollBarMax = this.selectedSP.getVerticalScrollBar().getMaximum();

			int itemHeight = scrollBarMax / nbUnselected;
			int newPosition = itemHeight * lowerIndex;
			
			int currentPosition = this.selectedSP.getVerticalScrollBar().getValue();
			int viewSize = this.selectedSP.getVerticalScrollBar().getVisibleAmount();

			// We move only if the newPosition is not already shown
			if((newPosition < currentPosition) || (newPosition+itemHeight > currentPosition+viewSize)) {
				this.selectedSP.getVerticalScrollBar().setValue(newPosition);
			}
		}

		// We change the selection (of the reduced list) to be sure that at least one item is selected
		int newSelectedIndex = this.unselectedCols.getSelectedIndex();
		if(newSelectedIndex < 0) {
			newSelectedIndex = 0;
		} else if(newSelectedIndex >= this.unselectedModel.getSize()) {
			newSelectedIndex = this.unselectedModel.getSize()-1;
		}
		this.unselectedCols.setSelectedIndex(newSelectedIndex);

		this.leftButton.setEnabled(this.selectedModel.getSize()>0);
		this.allLeftButton.setEnabled(this.selectedModel.getSize()>0);
		this.rightButton.setEnabled(this.unselectedModel.getSize()>0);
		this.allRightButton.setEnabled(this.unselectedModel.getSize()>0);
	}

	// selectedIndices: indices of selected rows in 'selectedCols'
	// -> The indices should be sort ascending
	private void deselectColumns(int selectedIndices[]) {
		// We take the columns from the selected and we put them back in the unselected

		// getSelectedIndices gives the indices sorting ascending
		// remove will change the indices
		// so if indices 0,2,3 are selected
		// we want to remove 0, then 1, then 1.
		int nbInserted=0;
		// We store the lower index to put the ScrollBar at the level of the item
		int lowerIndex = -1;
		for(int i=0; i<selectedIndices.length; ++i) {
			int trueIndice = selectedIndices[i] - nbInserted;
			if(trueIndice < this.selectedModel.getSize()) {
				selectedIndices[i] = this.unselectedModel.add(this.selectedModel.removeElement(trueIndice));
				nbInserted++;
				
				if((lowerIndex == -1) || (selectedIndices[i] < lowerIndex)) {
					lowerIndex = selectedIndices[i];
				}
			} else {
				selectedIndices[i] = -1;
			}
		}
		
		// We select the added elements
		this.unselectedCols.setSelectedIndices(selectedIndices);
		// We put the ScrollBar at the level of the first selected item
		if(lowerIndex != -1) {
			// We force the ScrollPane to resize
			this.unselectedSP.setViewportView(this.unselectedCols);
			int nbUnselected = this.unselectedModel.getSize();
			int scrollBarMax = this.unselectedSP.getVerticalScrollBar().getMaximum();

			int itemHeight = scrollBarMax / nbUnselected;
			int newPosition = itemHeight * lowerIndex;
			
			int currentPosition = this.unselectedSP.getVerticalScrollBar().getValue();
			int viewSize = this.unselectedSP.getVerticalScrollBar().getVisibleAmount();

			// We move only if the newPosition is not already shown
			if((newPosition < currentPosition) || (newPosition+itemHeight > currentPosition+viewSize)) {
				this.unselectedSP.getVerticalScrollBar().setValue(newPosition);
			}
		}

		// We change the selection (of the reduced list) to be sure that at least one item is selected
		int newSelectedIndex = this.selectedCols.getSelectedIndex();
		if(newSelectedIndex < 0) {
			newSelectedIndex = 0;
		} else if(newSelectedIndex >= this.selectedModel.getSize()) {
			newSelectedIndex = this.selectedModel.getSize()-1;
		}
		this.selectedCols.setSelectedIndex(newSelectedIndex);

		this.leftButton.setEnabled(this.selectedModel.getSize()>0);
		this.allLeftButton.setEnabled(this.selectedModel.getSize()>0);
		this.rightButton.setEnabled(this.unselectedModel.getSize()>0);
		this.allRightButton.setEnabled(this.unselectedModel.getSize()>0);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource() == this.availableNamespaces) {
			namespaceChanged();
		} else if(e.getSource() == this.selectedCols) {
			// selectedIndices is sorted ascending
			int selectedIndices[] = this.selectedCols.getSelectedIndices();
			
			if(selectedIndices.length == 0) {
				this.upButton.setEnabled(false);
				this.downButton.setEnabled(false);
				
				return;
			}
			
			int minIndex = selectedIndices[0];
			int maxIndex = selectedIndices[selectedIndices.length-1];
			
			this.upButton.setEnabled(minIndex > 0);
			this.downButton.setEnabled(maxIndex < this.selectedModel.getSize()-1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.allLeftButton) {
			int allIndices[] = new int[this.selectedModel.getSize()];
			for(int i=0; i<allIndices.length; ++i) {
				allIndices[i] = i;
			}
			deselectColumns(allIndices);
		} else if(e.getSource() == this.allRightButton) {
			int allIndices[] = new int[this.unselectedModel.getSize()];
			for(int i=0; i<allIndices.length; ++i) {
				allIndices[i] = i;
			}
			selectColumns(allIndices);
		} else if(e.getSource() == this.rightButton) {
			selectColumns(this.unselectedCols.getSelectedIndices());
		} else if(e.getSource() == this.leftButton) {
			deselectColumns(this.selectedCols.getSelectedIndices());
		} else if(e.getSource() == this.upButton) {
			// the button should be disabled if the first item is selected
			// we assume here it is not in the list
			
			// We will try to keep the selection after the move
			int selectedIndices[] = this.selectedCols.getSelectedIndices();
			// We store the lower index to put the ScrollBar at the level of the item
			int lowerIndex = -1;
			for(int i=0; i<selectedIndices.length; ++i) {
				this.selectedModel.swap(selectedIndices[i]-1, selectedIndices[i]);
				selectedIndices[i]--;
				
				if((lowerIndex == -1) || (selectedIndices[i] < lowerIndex)) {
					lowerIndex = selectedIndices[i];
				}
			}
			
			this.selectedCols.setSelectedIndices(selectedIndices);
			
			// We put the ScrollBar at the level of the first selected item
			if(lowerIndex != -1) {
				// We force the ScrollPane to resize
				this.selectedSP.setViewportView(this.selectedCols);
				int nbUnselected = this.selectedModel.getSize();
				int scrollBarMax = this.selectedSP.getVerticalScrollBar().getMaximum();

				int itemHeight = scrollBarMax / nbUnselected;
				int newPosition = itemHeight * lowerIndex;
				
				int currentPosition = this.selectedSP.getVerticalScrollBar().getValue();
				int viewSize = this.selectedSP.getVerticalScrollBar().getVisibleAmount();

				// We move only if the newPosition is not already shown
				if((newPosition < currentPosition) || (newPosition+itemHeight > currentPosition+viewSize)) {
					this.selectedSP.getVerticalScrollBar().setValue(newPosition);
				}
			}
		} else if(e.getSource() == this.downButton) {
			// the button should be disabled if the last item is selected
			// we assume here it is not in the list
			
			// selectedIndices is sorted ascending, so we have to swap from the last to the first selected index
			int selectedIndices[] = this.selectedCols.getSelectedIndices();
			// We store the lower index to put the ScrollBar at the level of the item
			int lowerIndex = -1;
			for(int i = selectedIndices.length-1; i>=0; --i) {
				this.selectedModel.swap(selectedIndices[i], selectedIndices[i]+1);
				selectedIndices[i]++;
				
				if((lowerIndex == -1) || (selectedIndices[i] < lowerIndex)) {
					lowerIndex = selectedIndices[i];
				}
			}
			
			this.selectedCols.setSelectedIndices(selectedIndices);
			
			// We put the ScrollBar at the level of the first selected item
			if(lowerIndex != -1) {
				// We force the ScrollPane to resize
				this.selectedSP.setViewportView(this.selectedCols);
				int nbUnselected = this.selectedModel.getSize();
				int scrollBarMax = this.selectedSP.getVerticalScrollBar().getMaximum();

				int itemHeight = scrollBarMax / nbUnselected;
				int newPosition = itemHeight * lowerIndex;
				
				int currentPosition = this.selectedSP.getVerticalScrollBar().getValue();
				int viewSize = this.selectedSP.getVerticalScrollBar().getVisibleAmount();

				// We move only if the newPosition is not already shown
				if((newPosition < currentPosition) || (newPosition+itemHeight > currentPosition+viewSize)) {
					this.selectedSP.getVerticalScrollBar().setValue(newPosition);
				}
			}
		}
	}

	private class NamespaceListModel extends AbstractListModel<String> {
		
		private static final long serialVersionUID = -6410717281534966688L;
		
		private List<String> model;

		public NamespaceListModel() {
			model = new ArrayList<>();
		}

		@Override
		public int getSize() {
			return model.size();
		}

		@Override
		public String getElementAt(int index) {
			return model.get(index);
		}

		public void add(String namespace) {
			// no duplicate
			if(!model.contains(namespace)) {
				model.add(namespace);
				model.sort(nsComp);
				fireContentsChanged(this, 0, getSize());
			}
		}
		
		public void removeAllElements() {
			model.clear();
			fireContentsChanged(this, 0, 0);
		}
	}
	
	private class NamespaceCellRenderer implements ListCellRenderer<String> {

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel label = new JLabel(value);
			// we add a padding right
			label.setBorder(new EmptyBorder(0,0,0,5));
			
			// The "Cytoscape" namespace should be set as "null" again
			if(value.equals(SelectAndOrderColumnPanel.DefaultNamespace)) {
				value = null;
			}
			label.setIcon(presentationManager.getColumnPresentation(value).getNamespaceIcon());
			
			if(isSelected) {
				label.setBackground(list.getSelectionBackground());
				label.setForeground(list.getSelectionForeground());
			} else {
				label.setBackground(list.getBackground());
				label.setForeground(list.getForeground());
			}
			
			label.setEnabled(list.isEnabled());
			label.setFont(list.getFont());
			label.setOpaque(list.isOpaque());
			
			return label;
		}
		
	}
	
	/**
	 * Comparator that puts the default namespace before every other one, and then sort alphabetically.
	 */
	private class NamespaceComparator implements Comparator<String> {
		
		public NamespaceComparator() {
			super();
		}

		@Override
		public int compare(String o1, String o2) {
			if(o1 == o2 || o1.equals(o2)) {
				return 0;
			}
			
			// We put the default namespace first
			if(o1.equals(SelectAndOrderColumnPanel.DefaultNamespace)) {
				return -1;
			}
			
			if(o2.equals(SelectAndOrderColumnPanel.DefaultNamespace)) {
				return 1;
			}
			
			// We sort namespaces case-insensitive
			return o1.toLowerCase().compareTo(o2.toLowerCase());
		}
		
	}

	private class SortedColumnListModel extends AbstractListModel<CyColumn> {
		
		private static final long serialVersionUID = -833974734508561782L;
		
		private TreeMap<String, CyColumn> model;
		private String namespace;

		public SortedColumnListModel() {
			model = new TreeMap<>();
			this.namespace = "";
		}

		@Override
		public int getSize() {
			return model.size();
		}

		@Override
		public CyColumn getElementAt(int index) {
			return (CyColumn) model.values().toArray()[index];
		}
		
		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		public int add(CyColumn column) {
			// Selected columns should not be added
			if(selectedModel.getColumnList().contains(column)) {
				return -1;
			}
			
			// We only add column from the same namespace
			if(this.namespace.equals(presentationManager.getColumnPresentation(column.getNamespace()).getNamespaceDescription())) {
				// We don't want a case-sensitive sorting, so we use the uppercase String as key
				model.put(column.getName().toUpperCase(), column);
				fireContentsChanged(this, 0, getSize());
				
				return new ArrayList<>(model.values()).indexOf(column);
			}
			
			return -1;
		}

		public CyColumn removeElement(int index) {
			// the key is the uppercase full name
			CyColumn removed = model.remove(getElementAt(index).getName().toUpperCase());
			if (removed != null) {
				fireContentsChanged(this, 0, getSize());
			}
			return removed;
		}
		
		public void removeAllElements() {
			model.clear();
			fireContentsChanged(this, 0, 0);
		}
	}

	private class ColumnListModel extends AbstractListModel<CyColumn> {
		
		private static final long serialVersionUID = -6410717281534966688L;
		
		private Vector<CyColumn> model;

		public ColumnListModel() {
			model = new Vector<CyColumn>();
		}

		@Override
		public int getSize() {
			return model.size();
		}

		@Override
		public CyColumn getElementAt(int index) {
			return model.get(index);
		}

		public int add(CyColumn column) {
			model.add(column);
			fireContentsChanged(this, 0, getSize());
			
			// We return the indice of the new element (which is the last element)
			return this.getSize()-1;
		}

		public void swap(int i, int j) {
			Collections.swap(model, i, j);
			fireContentsChanged(this, 0, getSize());
		}

		public CyColumn removeElement(int index) {
			CyColumn removed = model.remove(index);
			if (removed != null) {
				fireContentsChanged(this, 0, getSize());
			}
			return removed;
		}
		
		public void removeAllElements() {
			model.clear();
		}

		public List<CyColumn> getColumnList() {
			return model;
		}
	}
}
