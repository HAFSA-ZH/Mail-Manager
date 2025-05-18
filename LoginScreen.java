package mail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginScreen() {
        System.out.println("Initialisation de LoginScreen...");
        setTitle("Authentification");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(243, 244, 246)); // #F3F4F6 (fond gris clair)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel emailLabel = new JLabel("Email : *");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setForeground(new Color(107, 114, 128)); // #6B7280 (gris moyen pour étiquettes)
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(59, 130, 246), 1, true), // #3B82F6 (bleu clair pour bordure)
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel passwordLabel = new JLabel("Mot de passe : *");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(107, 114, 128)); // #6B7280
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(59, 130, 246), 1, true), // #3B82F6
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    emailField.requestFocusInWindow();
                }
            }
        });

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        messageLabel.setForeground(new Color(239, 68, 68)); // #EF4444 (rouge pour erreurs)

        JButton loginButton = new JButton("Connexion");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(30, 58, 138)); // #1E3A8A (bleu marine)
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 58, 138), 1, true), // #1E3A8A
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(209, 213, 219)); // #D1D5DB (gris clair pour bouton Annuler)
        cancelButton.setForeground(new Color(17, 24, 39)); // #111827 (texte foncé)
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true), // #D1D5DB
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Disposition
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(messageLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        panel.add(cancelButton, gbc);

        loginButton.addActionListener(e -> {
            System.out.println("Bouton Connexion cliqué !");
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            System.out.println("Email saisi : " + email);
            System.out.println("Mot de passe saisi : ****");

            emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 1, true), // #3B82F6
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 1, true), // #3B82F6
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            if (email.isEmpty() || password.isEmpty()) {
                System.out.println("Validation échouée : champs vides");
                messageLabel.setText("Tous les champs marqués d'un * sont obligatoires.");
                if (email.isEmpty()) emailField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(239, 68, 68), 2, true), // #EF4444
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                if (password.isEmpty()) passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(239, 68, 68), 2, true), // #EF4444
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                return;
            }

            System.out.println("Tentative d'authentification...");
            if (authenticate(email, password)) {
                System.out.println("Authentification réussie.");
                JOptionPane.showMessageDialog(this, "Connexion réussie !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                System.out.println("Ouverture de la fenêtre Email...");
                SwingUtilities.invokeLater(() -> new Email(email, email, null, null, null, password));
            } else {
                System.out.println("Authentification échouée.");
                messageLabel.setText("Email ou mot de passe incorrect.");
                passwordField.setText("");
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(239, 68, 68), 2, true), // #EF4444
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });

        cancelButton.addActionListener(e -> System.exit(0));

        add(panel);
        panel.revalidate();
        panel.repaint();

        SwingUtilities.invokeLater(() -> passwordField.requestFocusInWindow());

        System.out.println("Fenêtre LoginScreen prête, appel de setVisible(true)...");
        setVisible(true);
        System.out.println("Fenêtre LoginScreen affichée.");
    }

    private boolean authenticate(String email, String password) {
        String url = "jdbc:mysql://localhost:3306/email_manager";
        String user = "root";
        String dbPassword = "";

        String sql = "SELECT password FROM email_accounts WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, dbPassword)) {
            System.out.println("Connexion à la base de données réussie.");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                System.out.println("Mot de passe stocké dans la base : ****");
                return storedPassword.equals(password);
            } else {
                System.out.println("Aucun compte trouvé pour cet email.");
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("Erreur SQL : " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données : " + ex.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Lancement de l'application via LoginScreen...");
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}