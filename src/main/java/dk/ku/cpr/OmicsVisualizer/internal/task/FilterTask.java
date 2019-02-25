package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTask extends AbstractTask {
	private OVManager ovManager;
	private OVCytoPanel ovPanel;

	private OVTable ovTable;

	@Tunable(description="Select column",
			tooltip="Select the column you want to filter",
			gravity=1.0)
	public ListSingleSelection<String> selectColName;

	@Tunable(description="Operator",
			tooltip="Select the way the values should be filtered",
			gravity=1.0)
	public ListSingleSelection<String> selectOperator;

	@Tunable(description="Value",
			tooltip="Select the value to compare with",
			gravity=1.0)
	public String strReference;

	public FilterTask(OVManager ovManager) {
		this(ovManager, null);
	}

	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel) {
		super();
		this.ovManager = ovManager;
		this.ovPanel = ovPanel;

		this.ovTable = this.ovPanel.getDisplayedTable();

		List<String> colNames = new ArrayList<>();
		for(String colname : this.ovTable.getColNames()) {
			if(!OVShared.isOVCol(colname) && 
					this.ovTable.getColType(colname) != String.class &&
					this.ovTable.getColType(colname) != List.class) {
				colNames.add(colname);
			}
		}
		this.selectColName = new ListSingleSelection<>(colNames);

		this.selectOperator = new ListSingleSelection<String>(Arrays.asList("==",
				"!=",
				"<",
				"<=",
				">",
				">=",
				"null",
				"not null"));

		this.strReference="0";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Filtering Omics Visualizer rows");

		if(this.ovPanel == null) {
			CySwingApplication swingApplication = this.ovManager.getService(CySwingApplication.class);
			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
			this.ovPanel = (OVCytoPanel) cytoPanel.getComponentAt(cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));

			if(this.ovPanel == null) {
				return;
			}
		}

		Operator operator=null;
		if(this.selectOperator.getSelectedValue().equals("==")) {
			operator = new OperatorE();
		} else if(this.selectOperator.getSelectedValue().equals("!=")) {
			operator = new OperatorNE();
		} else if(this.selectOperator.getSelectedValue().equals("<")) {
			operator = new OperatorL();
		} else if(this.selectOperator.getSelectedValue().equals("<=")) {
			operator = new OperatorLE();
		} else if(this.selectOperator.getSelectedValue().equals(">")) {
			operator = new OperatorG();
		} else if(this.selectOperator.getSelectedValue().equals(">=")) {
			operator = new OperatorGE();
		} else if(this.selectOperator.getSelectedValue().equals("null")) {
			operator = new OperatorN();
		} else if(this.selectOperator.getSelectedValue().equals("not null")) {
			operator = new OperatorNN();
		} else {
			return;
		}

		String colName = this.selectColName.getSelectedValue();
		Class<?> colType = this.ovTable.getColType(colName);

		Number reference = null;
		try {
			if(colType == Integer.class) {
				reference=Integer.parseInt(strReference);
			} else if(colType == Long.class) {
				reference=Long.parseLong(strReference);
			} else if(colType == Double.class) {
				reference=Double.parseDouble(strReference);
			}
		} catch(NumberFormatException e) {
			reference = null;
		}

		if(reference ==null) {
			taskMonitor.setStatusMessage("Error: Impossible to parse the value \""+strReference+"\" as a number.");
			return;
		}

		List<Object> filteredRowKeys = new ArrayList<>();
		for(CyRow row : this.ovTable.getCyTable().getAllRows()) {
			try { 
				Number value = (Number) row.get(colName, colType);

				if(operator.filter(new NumberComparable(value), reference)) {
					filteredRowKeys.add(row.getRaw(this.ovTable.getCyTable().getPrimaryKey().getName()));
				}
			} catch(ClassCastException e) {
				taskMonitor.setStatusMessage("Warning: Could not cast \""+colName+"\" into double for row "+row.toString()+".");
			}
		}

		this.ovPanel.getTableModel().filter(filteredRowKeys);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Filter Omics Visualizer Table";
	}

	private class NumberComparable implements Comparable<Number> {
		private Number n;

		public NumberComparable(Number n) {
			this.n=n;
		}

		@Override
		public int compareTo(Number o) {
			if(n instanceof Integer) {
				Integer i = (Integer)n;
				return i.compareTo((Integer)o);
			} else if(n instanceof Long) {
				Long l = (Long)n;
				return l.compareTo((Long)o);
			} else if(n instanceof Double) {
				Double d = (Double)n;
				return d.compareTo((Double)o);
			}

			return 0;
		}
	}

	private abstract class Operator {
		public boolean filter(NumberComparable tableValue, Number reference) {
			if(tableValue == null) {
				return false;
			}
			if(tableValue.n == reference) {
				return true;
			}
			if(tableValue.n == null) {
				return false;
			}
			return true;
		}
	}

	private class OperatorE extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			return super.filter(tableValue, reference) && (tableValue.compareTo(reference)==0);
		}
	}

	private class OperatorNE extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			return super.filter(tableValue, reference) && (tableValue.compareTo(reference) != 0);
		}
	}

	private class OperatorL extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			return super.filter(tableValue, reference) && (tableValue.compareTo(reference) < 0);
		}
	}

	private class OperatorLE extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			return super.filter(tableValue, reference) && (tableValue.compareTo(reference) <= 0);
		}
	}

	private class OperatorG extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			return super.filter(tableValue, reference) && (tableValue.compareTo(reference) > 0);
		}
	}

	private class OperatorGE extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			return super.filter(tableValue, reference) && (tableValue.compareTo(reference) >= 0);
		}
	}

	private class OperatorN extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			// Here we do not look at reference
			return (tableValue==null || tableValue.n == null);
		}
	}

	private class OperatorNN extends Operator {
		@Override
		public boolean filter(NumberComparable tableValue, Number reference) {
			// Here we do not look at reference
			return (tableValue!=null && tableValue.n != null);
		}
	}
}
