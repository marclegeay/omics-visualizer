package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ColorPanel extends JPanel {
	private static final long serialVersionUID = -2950405099490317278L;
	
	private Color color;
	
	public ColorPanel(Color color, Container parent) {
		super();
		this.setColor(color);
		this.setBorder(BorderFactory.createLineBorder(parent.getBackground()));
		this.setOpaque(false); // To display alpha-colors
	}
	
	public ColorPanel(Container parent) {
		this(null, parent);
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
}
