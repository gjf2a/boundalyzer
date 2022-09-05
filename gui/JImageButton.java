package gui;

import javax.swing.*;

import java.awt.*;

@SuppressWarnings("serial")
public class JImageButton extends JButton {
	public JImageButton(String imgFileUp, String imgFileDown, String toolTip) {
		super();
		setIcon(makeButtonImage(imgFileUp));
        setBorderPainted(false);
        setBackground(new Color(0, 0, 0, 0));
        setPreferredSize(new Dimension(35, 35));
        setPressedIcon(makeButtonImage(imgFileDown));
        setToolTipText(toolTip);
	}
	
	private Icon makeButtonImage(String filename) {
        Image button = getToolkit().createImage(getClass().getClassLoader().getResource(filename)); 
        return new ImageIcon(button.getScaledInstance(32, 32, 0));
    }
}
