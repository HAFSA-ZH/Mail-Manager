package mail;

import javax.swing.*;
import java.awt.*;
public class Label extends JPanel {

	public Label(String label,int cols) {
		  setLayout(new FlowLayout(FlowLayout.LEFT)); // Correctement défini
	       add(new JLabel(label));
	       add(new JTextField(cols));
	}
	public Label(String label,int cols,int height) {
		  setLayout(new FlowLayout(FlowLayout.LEFT)); // Correctement défini
		  JLabel lbl = new JLabel(label);
		  lbl.setPreferredSize(new Dimension(100, height));
	       add(lbl);
	       add(new JTextField(cols));
	}
	public Label(String label,int cols,int lig,int height) {
		  setLayout(new FlowLayout(FlowLayout.LEFT)); // Correctement défini
		  JLabel lbl = new JLabel(label);
		  lbl.setPreferredSize(new Dimension(100, height));
	       add(lbl);
	       JTextArea  bodyArea = new JTextArea(lig,cols);
	       bodyArea.setLineWrap(true);
	       add(bodyArea);
	}
	public String getText() {
	    if (getComponent(1) instanceof JTextField) {
	        return ((JTextField) getComponent(1)).getText();
	    } else if (getComponent(1) instanceof JTextArea) {
	        return ((JTextArea) getComponent(1)).getText();
	    }
	    return "";
	}
	
	public void setText(String text) {
	    Component comp = getComponent(1);
	    if (comp instanceof JTextField) {
	        ((JTextField) comp).setText(text);
	    } else if (comp instanceof JTextArea) {
	        ((JTextArea) comp).setText(text);
	    }
	}

}
