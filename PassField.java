package mail;
import javax.swing.*;
import java.awt.*;

class PassField extends JPanel {
    private JPasswordField passwordField;

    public PassField(String label, int cols, int height) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(100, height));
        add(lbl);
        passwordField = new JPasswordField(cols);
        add(passwordField);
    }

    public String getText() {
        return new String(passwordField.getPassword());
    }

    public void setText(String text) {
        passwordField.setText(text);
    }
}