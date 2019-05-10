package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.cytoscape.util.color.Palette;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public class ColorPanel extends JPanel implements MouseListener {
	private static final long serialVersionUID = -2950405099490317278L;
	
	private Container parent;
	private Color color;
	private ColorChooser colorChooser;
	private OVManager ovManager;
	private Palette palette;
	
	public ColorPanel(Color color, Container parent, ColorChooser colorChooser, OVManager ovManager, Palette palette) {
		super();
		this.parent=parent;
		this.colorChooser=colorChooser;
		this.ovManager=ovManager;
		this.palette=palette;
		this.setColor(color);
		this.setBorder(BorderFactory.createLineBorder(parent.getBackground()));
		this.setOpaque(false); // To display alpha-colors
		
		this.addMouseListener(this);
		this.setToolTipText("Click here to change the color.");
		
		this.setPreferredSize(new Dimension(30, 30));
	}
	
	public ColorPanel(Container parent, ColorChooser colorChooser, OVManager ovManager, Palette palette) {
		this(null, parent, colorChooser, ovManager, palette);
	}
	
	public void setColor(Color color) {
		this.color=color;
		this.setBackground(this.color);
	}
	
	public Color getColor() {
		return this.color;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// We paint ourself the background
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		super.paintComponent(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(this.palette != null) {
			this.setColor(this.ovManager.getService(CyColorPaletteChooserFactory.class).getColorPaletteChooser(this.palette.getType(), false).showDialog(parent, "", this.palette,  this.color, 9));
		} else {
			this.colorChooser.show(this);
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Do nothing
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Do nothing
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing
	}
}
