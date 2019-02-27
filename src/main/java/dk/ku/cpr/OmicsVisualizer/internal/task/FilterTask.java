package dk.ku.cpr.OmicsVisualizer.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.ui.OVCytoPanel;

public class FilterTask extends AbstractTask {
	protected OVManager ovManager;
	protected OVCytoPanel ovPanel;

	protected OVTable ovTable;
	
	protected boolean removeFilter;

	protected String colName;
	protected String strOperator;
	protected String strReference;
	
	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, boolean removeFilter) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;
		
		this.removeFilter=removeFilter;
		
		if(this.ovPanel != null) {
			this.ovTable = this.ovPanel.getDisplayedTable();
		}
	}
	
	public FilterTask(OVManager ovManager, OVCytoPanel ovPanel, String colName, String strOperator, String strReference) {
		this.ovManager=ovManager;
		this.ovPanel=ovPanel;
		
		this.removeFilter=false;
		
		this.colName=colName;
		this.strOperator=strOperator;
		this.strReference=strReference;
		
		if(this.ovPanel != null) {
			this.ovTable = this.ovPanel.getDisplayedTable();
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Filtering Omics Visualizer rows");

		if(this.ovPanel == null) {
			CySwingApplication swingApplication = this.ovManager.getService(CySwingApplication.class);
			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
			try {
				this.ovPanel = (OVCytoPanel) cytoPanel.getComponentAt(cytoPanel.indexOfComponent(OVShared.CYTOPANEL_NAME));
			} catch(IndexOutOfBoundsException e) {
				return;
			}

			if(this.ovPanel == null) {
				return;
			}
		}
		
		this.ovTable = this.ovPanel.getDisplayedTable();

		if(this.removeFilter) {
			this.ovTable.removeFilter();
			// TODO
			this.ovTable.setTableProperty(OVShared.PROPERTY_FILTER, "");
		} else {
			Operator operator=null;
			if(this.strOperator.equals("==")) {
				operator = new OperatorE();
			} else if(this.strOperator.equals("!=")) {
				operator = new OperatorNE();
			} else if(this.strOperator.equals("<")) {
				operator = new OperatorL();
			} else if(this.strOperator.equals("<=")) {
				operator = new OperatorLE();
			} else if(this.strOperator.equals(">")) {
				operator = new OperatorG();
			} else if(this.strOperator.equals(">=")) {
				operator = new OperatorGE();
			} else if(this.strOperator.equals("null")) {
				operator = new OperatorN();
			} else if(this.strOperator.equals("not null")) {
				operator = new OperatorNN();
			} else {
				return;
			}

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

			// TODO
			String savedFilter = colName+","+this.strOperator+","+this.strReference;
			this.ovTable.setTableProperty(OVShared.PROPERTY_FILTER, savedFilter);
			
			List<Object> filteredRowKeys = new ArrayList<>();
			List<CyRow> allRows = this.ovTable.getCyTable().getAllRows();
			int i=0;
			for(CyRow row : allRows) {
				taskMonitor.setProgress((i++)/allRows.size());
				try { 
					Number value = (Number) row.get(colName, colType);

					if(operator.filter(new NumberComparable(value), reference)) {
						filteredRowKeys.add(row.getRaw(this.ovTable.getCyTable().getPrimaryKey().getName()));
					}
				} catch(ClassCastException e) {
					taskMonitor.setStatusMessage("Warning: Could not cast \""+colName+"\" into double for row "+row.toString()+".");
				}
			}

			this.ovTable.filter(filteredRowKeys);
		}

		this.ovTable.save();
		this.ovPanel.update();
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
