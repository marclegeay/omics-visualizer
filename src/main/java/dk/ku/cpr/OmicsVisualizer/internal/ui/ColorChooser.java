package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooser extends JFrame implements ChangeListener, ActionListener {
	private static final long serialVersionUID = -7368462609341478480L;

	private JColorChooser colorChooser;
	private JButton closeButton;

	private ColorPanel colorLabel;

	public ColorChooser() {
		this.colorChooser = new JColorChooser();
		this.colorChooser.getSelectionModel().addChangeListener(this);

		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(this.colorChooser, BorderLayout.CENTER);
		mainPanel.add(this.closeButton, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		
		this.pack();

		this.colorLabel=null;
	}

	public void show(ColorPanel colorLabel) {
		this.colorLabel = colorLabel;
		
		if(this.colorLabel.getColor() != null) {
			this.colorChooser.setColor(this.colorLabel.getColor());
		}

		this.setLocationRelativeTo(this.colorLabel);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.closeButton) {
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
