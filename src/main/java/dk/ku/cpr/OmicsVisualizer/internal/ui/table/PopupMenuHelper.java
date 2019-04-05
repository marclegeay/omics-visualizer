package dk.ku.cpr.OmicsVisualizer.internal.ui.table;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2013 The Cytoscape Consortium
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

/**
 * A class that encapsulates the creation of JPopupMenus based on TaskFactory
 * services.
 */
public class PopupMenuHelper {

	private final OVManager ovManager;

	public PopupMenuHelper(final OVManager ovManager) {
		this.ovManager = ovManager;
	}

	@SuppressWarnings("serial")
	public void createTableCellMenu(final CyColumn column, final Object primaryKeyValue,
			final Component invoker,
			final int x, final int y, final JTable table) {
		final JPopupMenu menu = new JPopupMenu();
		
		final Object value = column.getTable().getRow(primaryKeyValue).get(column.getName(), column.getType());
		
		if (value != null) {
			String urlString = value.toString();
			if (urlString != null && (urlString.startsWith("http:") || urlString.startsWith("https:")))
				menu.add(getOpenLinkMenu(value.toString()));
		}
		
		final String name = "Select nodes from selected rows";

		final JMenuItem mi = new JMenuItem(new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectElementsFromSelectedRows(table);
			}

			@Override
			public boolean isEnabled() {
				final CyApplicationManager applicationManager =
						ovManager.getService(CyApplicationManager.class);

				return table.getSelectedRowCount() > 0 && applicationManager.getCurrentNetwork() != null;
			}
		});
		
		// We make sure that the table is connected to the current network before enabling to select nodes
		final CyApplicationManager applicationManager = this.ovManager.getService(CyApplicationManager.class);
		final CyNetwork net = applicationManager.getCurrentNetwork();
		if(this.ovManager.getActiveOVTable().isConnectedTo(net)) {
			menu.add(mi);
		}
		
		if (menu.getSubElements().length > 0)
			menu.show(invoker, x, y);
	}
	
	// Preset menu item: open browser
	protected JMenuItem getOpenLinkMenu(final Object urlString) {
		final JMenuItem openLinkItem = new JMenuItem();
		openLinkItem.setText("Open URL in web browser...");
		
		if (urlString == null || urlString.toString().startsWith("http:") == false) {
			openLinkItem.setEnabled(false);
		} else {
			openLinkItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final OpenBrowser openBrowser = ovManager.getService(OpenBrowser.class);
					openBrowser.openURL(urlString.toString());
				}
			});
		}

		return openLinkItem;
	}
	
	private void selectElementsFromSelectedRows(final JTable table) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				final CyApplicationManager applicationManager = ovManager.getService(CyApplicationManager.class);
				final CyNetwork net = applicationManager.getCurrentNetwork();
				
				if (net != null) {
					OVConnection ovCon = ovManager.getActiveOVTable().getConnection(net);
					if(ovCon != null) {
						List<CyRow> selectedTableRows = ovCon.getOVTable().getSelectedRows();
						
						CyTable nodeTable = net.getDefaultNodeTable();
						for(CyNode node : net.getNodeList()) {
							Boolean selected = false;
							for(CyRow tableRow : ovCon.getLinkedRows(nodeTable.getRow(node.getSUID()))) {
								selected = selected | selectedTableRows.contains(tableRow);
							}
							
							nodeTable.getRow(node.getSUID()).set(CyNetwork.SELECTED, selected);
						}
						
						final CyNetworkView view = applicationManager.getCurrentNetworkView();
						
						if (view != null) {
							final CyEventHelper eventHelper = ovManager.getService(CyEventHelper.class);
							eventHelper.flushPayloadEvents();
							view.updateView();
						}
					}
				}
			}
		};
		t.start();
	}
}
