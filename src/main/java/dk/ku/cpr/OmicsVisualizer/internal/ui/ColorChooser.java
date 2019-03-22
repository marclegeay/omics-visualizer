package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooser extends JDialog implements ChangeListener, ActionListener {
	private static final long serialVersionUID = -7368462609341478480L;

	private JColorChooser colorChooser;
	private JButton closeButton;
	private JButton cancelButton;

	private ColorPanel colorLabel;
	private Color previousColor;

	public ColorChooser() {
		this.setModal(true);
		
		this.colorChooser = new JColorChooser();
		this.colorChooser.getSelectionModel().addChangeListener(this);

		this.closeButton = new JButton("OK");
		this.closeButton.addActionListener(this);

		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);

		this.colorLabel=null;
	}

	public void show(ColorPanel colorLabel) {
		this.colorLabel = colorLabel;
		this.previousColor = this.colorLabel.getColor();
		
		if(this.colorLabel.getColor() != null) {
			this.colorChooser = new JColorChooser(this.colorLabel.getColor());
			this.colorChooser.getSelectionModel().addChangeListener(this);
		}
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.closeButton);
		buttonPanel.add(this.cancelButton);
		
		mainPanel.add(this.colorChooser, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		
		this.pack();

		// We try to align the JFrame with the label, and put the JFrame at the border of the Visualization Window
		this.setLocationRelativeTo(this.colorLabel); // align the element on the Y basis
		this.setLocation(this.colorLabel.getTopLevelAncestor().getX() + this.colorLabel.getTopLevelAncestor().getWidth(), this.getY());
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.closeButton) {
			this.setVisible(false);
		} else if(e.getSource() == this.cancelButton) {
			if(this.colorLabel != null) {
				this.colorLabel.setColor(this.previousColor);
			}
			this.setVisible(false);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(this.colorLabel != null) {
			this.colorLabel.setColor(this.colorChooser.getColor());
		}
	}
}
