package mail;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class Email extends JFrame {
    private Label recipientField, subjectField, bodyArea, sentArea, recipientListField;
    private PassField passwordField;
    private JLabel attachmentLabel;
    private JComboBox<String> mailboxSelector;
    private File attachmentFile;
    private DefaultListModel<String> emailListModel;
    private Message[] messages;
    private Store store;
    private Folder inbox;
    private String initialPassword;
    private java.util.Map<String, String> emailPasswords;
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    Color lightGris = new Color(240, 240, 240);
   
    public Email(String sender, String recipient, String subject, String body) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public Email(String initialEmail, String sender, String recipient, String subject, String body, String password) {
        this(sender, recipient, subject, body);
        this.initialPassword = password;
        System.out.println("Constructeur Email avec initialEmail: " + initialEmail + ", mot de passe: " + (password != null ? "****" : "null"));
        
        initializeUI(); // Initialisation de l'interface graphique
        loadEmailAccounts(); // Chargement des comptes
        
        if (initialEmail != null && mailboxSelector.getItemCount() > 0) {
            mailboxSelector.setSelectedItem(initialEmail);
            String pwd = emailPasswords.get(initialEmail);
            if (pwd != null) {
                recevoirEmails(initialEmail, pwd);
            } else {
                JOptionPane.showMessageDialog(this, "Erreur : Aucun mot de passe trouvé pour " + initialEmail + " dans emailPasswords.");
            }
        } else if (mailboxSelector.getItemCount() > 0) {
            String email = (String) mailboxSelector.getSelectedItem();
            String pwd = emailPasswords.get(email);
            if (pwd != null) {
                recevoirEmails(email, pwd);
            }
        }
        setVisible(true); // Rendre la fenêtre visible
    }

    public Email() {
        initializeUI(); // Initialisation de l'interface graphique
        loadEmailAccounts(); // Chargement des comptes
        
        // Charger les emails du premier compte, s'il existe
        if (mailboxSelector.getItemCount() > 0) {
            String email = (String) mailboxSelector.getSelectedItem();
            recevoirEmails(email, emailPasswords.get(email));
        }
        setVisible(true); // Rendre la fenêtre visible
    }

    private void initializeUI() {
        setTitle("Entreprise Mail Manager");
        setSize(800, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        emailPasswords = new java.util.HashMap<>();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (inbox != null && inbox.isOpen()) inbox.close(false);
                    if (store != null && store.isConnected()) store.close();
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
        setBackground(lightGris);
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 2));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton searchButton = new JButton("Rechercher");
        mailboxSelector = new JComboBox<>(); // Initialisation de mailboxSelector
        sentArea = new Label("Rechercher : ", 30);
        JButton addAccountButton = new JButton("Ajouter un compte");
        JButton removeAccountButton = new JButton("Supprimer un compte");

        leftPanel.add(new JLabel("Boîte :"));
        leftPanel.add(mailboxSelector);
        leftPanel.add(addAccountButton);
        leftPanel.add(removeAccountButton);
        rightPanel.add(sentArea);
        rightPanel.add(searchButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Inbox Panel
        JPanel inboxPanel = new JPanel(new BorderLayout());
        inboxPanel.setBorder(BorderFactory.createTitledBorder("Boîte de réception"));
        emailListModel = new DefaultListModel<>();
        JList<String> emailList = new JList<>(emailListModel);
        emailList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = emailList.getSelectedIndex();
                    if (index >= 0 && messages != null && index < messages.length) {
                        try {
                            Message message = messages[index];
                            String content = getEmailContent(message);
                            String contentType = getEmailContentType(message);

                            JPanel messagePanel = new JPanel(new BorderLayout());
                            JTextArea metaArea = new JTextArea(3, 50);
                            metaArea.setText("De : " + message.getFrom()[0] + "\n" +
                                    "Objet : " + message.getSubject() + "\n" +
                                    "Date : " + (message.getReceivedDate() != null ? message.getReceivedDate().toString() : "Date inconnue"));
                            metaArea.setEditable(false);
                            metaArea.setLineWrap(true);
                            metaArea.setWrapStyleWord(true);

                            JEditorPane contentPane = new JEditorPane();
                            contentPane.setEditable(false);
                            if (contentType.contains("text/html")) {
                                contentPane.setContentType("text/html");
                                contentPane.setText(content);
                            } else {
                                contentPane.setContentType("text/plain");
                                contentPane.setText(content);
                            }

                            JScrollPane contentScroll = new JScrollPane(contentPane);
                            contentScroll.setPreferredSize(new Dimension(600, 300));

                            messagePanel.add(new JScrollPane(metaArea), BorderLayout.NORTH);
                            messagePanel.add(contentScroll, BorderLayout.CENTER);

                            JOptionPane.showMessageDialog(null, messagePanel, "Détails du message", JOptionPane.PLAIN_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Erreur : " + ex.getMessage());
                        }
                    }
                }
            }
        });
        inboxPanel.add(new JScrollPane(emailList), BorderLayout.CENTER);
        JButton viewSentEmailsButton = new JButton("Voir emails envoyés");
        JPanel inboxButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inboxButtonPanel.add(viewSentEmailsButton);
        inboxPanel.add(inboxButtonPanel, BorderLayout.SOUTH);

        // Send Panel
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));
        sendPanel.setBorder(BorderFactory.createTitledBorder("Composer un message"));

        recipientField = new Label("Destinataire :", 40, 30);
        subjectField = new Label("Objet :", 40, 30);
        passwordField = new PassField("Mot de passe", 40, 30);
        bodyArea = new Label("Message :", 40, 5, 30);
        recipientListField = new Label("<html>Destinataires<br>(séparés par des virgules) :</html>", 40, 5, 50);

        JButton attachButton = new JButton("Joindre un fichier");
        attachmentLabel = new JLabel("Aucune pièce jointe");
        JButton sendButton = new JButton("Envoyer");
        JButton sendToListButton = new JButton("Envoyer à une Liste");

        sendPanel.add(recipientField);
        sendPanel.add(passwordField);
        sendPanel.add(subjectField);
        sendPanel.add(bodyArea);
        sendPanel.add(attachButton);
        sendPanel.add(attachmentLabel);
        sendPanel.add(sendButton);
        sendPanel.add(recipientListField);
       
        sendPanel.add(sendToListButton);

        centerPanel.add(inboxPanel);
        centerPanel.add(sendPanel);
      
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        // Ajouter les listeners pour les boutons (déplacés ici pour garantir que les composants sont initialisés)
        addAccountButton.addActionListener(e -> {
            JDialog addAccountDialog = new JDialog(this, "Ajouter un compte", true);
            addAccountDialog.setSize(400, 300);
            addAccountDialog.setLocationRelativeTo(this);
            addAccountDialog.setModal(true);

            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            addAccountDialog.setLayout(layout);
            addAccountDialog.getContentPane().setBackground(new Color(245, 245, 245));

            JLabel emailLabel = new JLabel("Email : *");
            emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            JTextField emailField = new JTextField(20);
            emailField.setFont(new Font("Arial", Font.PLAIN, 14));
            emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            JLabel passwordLabel = new JLabel("Mot de passe : *");
            passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            JPasswordField passwordField = new JPasswordField(20);
            passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            JLabel confirmPasswordLabel = new JLabel("Confirmer mot de passe : *");
            confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            JPasswordField confirmPasswordField = new JPasswordField(20);
            confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
            confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            JLabel messageLabel = new JLabel(" ");
            messageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            messageLabel.setForeground(Color.RED);

            JButton saveButton = new JButton("Enregistrer");
            saveButton.setFont(new Font("Arial", Font.BOLD, 14));
            saveButton.setBackground(new Color(30, 144, 255));
            saveButton.setForeground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 144, 255), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton cancelButton = new JButton("Annuler");
            cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
            cancelButton.setBackground(new Color(211, 211, 211));
            cancelButton.setForeground(Color.BLACK);
            cancelButton.setFocusPainted(false);
            cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(211, 211, 211), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.LINE_END;
            layout.setConstraints(emailLabel, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            layout.setConstraints(emailField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            layout.setConstraints(passwordLabel, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            layout.setConstraints(passwordField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            layout.setConstraints(confirmPasswordLabel, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            layout.setConstraints(confirmPasswordField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(messageLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            layout.setConstraints(saveButton, gbc);

            gbc.gridx = 1;
            layout.setConstraints(cancelButton, gbc);

            saveButton.addActionListener(e2 -> {
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

                emailField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(135, 206, 235), 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(135, 206, 235), 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(135, 206, 235), 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    messageLabel.setText("Tous les champs marqués d'un * sont obligatoires.");
                    if (email.isEmpty()) emailField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    if (password.isEmpty()) passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    if (confirmPassword.isEmpty()) confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    return;
                }

                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    messageLabel.setText("Adresse e-mail invalide.");
                    emailField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    messageLabel.setText("Les mots de passe ne correspondent pas.");
                    passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    return;
                }

                if (password.length() < 8) {
                    messageLabel.setText("Le mot de passe doit contenir au moins 8 caractères.");
                    passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    return;
                }

                if (addEmailAccount(email, password)) {
                    JOptionPane.showMessageDialog(addAccountDialog, "Compte ajouté avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    mailboxSelector.addItem(email);
                    emailPasswords.put(email, password);
                    addAccountDialog.dispose();
                } else {
                    messageLabel.setText("Erreur : compte déjà existant ou problème de base de données.");
                    emailField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 2, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                }
            });

            cancelButton.addActionListener(e2 -> addAccountDialog.dispose());

            addAccountDialog.add(emailLabel);
            addAccountDialog.add(emailField);
            addAccountDialog.add(passwordLabel);
            addAccountDialog.add(passwordField);
            addAccountDialog.add(confirmPasswordLabel);
            addAccountDialog.add(confirmPasswordField);
            addAccountDialog.add(messageLabel);
            addAccountDialog.add(saveButton);
            addAccountDialog.add(cancelButton);

            addAccountDialog.setVisible(true);
        });

        // Ajouter les autres listeners (déplacés ici)
        removeAccountButton.addActionListener(e -> {
            String selectedEmail = (String) mailboxSelector.getSelectedItem();
            if (selectedEmail == null) {
                JOptionPane.showMessageDialog(this, "Aucun compte sélectionné.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment supprimer le compte " + selectedEmail + " ?", "Confirmer la suppression", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteEmailAccount(selectedEmail)) {
                    mailboxSelector.removeItem(selectedEmail);
                    emailPasswords.remove(selectedEmail);
                    JOptionPane.showMessageDialog(this, "Compte supprimé avec succès.");
                    if (mailboxSelector.getItemCount() > 0) {
                        recevoirEmails((String) mailboxSelector.getSelectedItem(), emailPasswords.get(mailboxSelector.getSelectedItem()));
                    } else {
                        emailListModel.clear();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du compte.");
                }
            }
        });

        mailboxSelector.addActionListener(e -> {
            String selectedEmail = (String) mailboxSelector.getSelectedItem();
            if (selectedEmail != null) {
                String password = emailPasswords.get(selectedEmail);
                if (password != null) {
                    recevoirEmails(selectedEmail, password);
                }
            }
        });

        viewSentEmailsButton.addActionListener(e -> {
            String url = "jdbc:mysql://localhost:3306/email_manager";
            String user = "root";
            String password = "";

            String sql = "SELECT sender, recipient, subject, body, attachment_name FROM sent_emails ORDER BY id DESC";

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);

                while (rs.next()) {
                    textArea.append("De : " + rs.getString("sender") + "\n");
                    textArea.append("À : " + rs.getString("recipient") + "\n");
                    textArea.append("Objet : " + rs.getString("subject") + "\n");
                    textArea.append("Message : " + rs.getString("body") + "\n");
                    textArea.append("Pièce jointe : " + rs.getString("attachment_name") + "\n");
                    textArea.append("--------------------------------------------------\n");
                }

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                JOptionPane.showMessageDialog(null, scrollPane, "Historique des emails envoyés", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur JDBC : " + ex.getMessage());
            }
        });

        attachButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                attachmentFile = fileChooser.getSelectedFile();
                attachmentLabel.setText("Attaché : " + attachmentFile.getName());
            }
        });

        searchButton.addActionListener(e -> {
            String keyword = sentArea.getText().trim();

            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Veuillez entrer un mot-clé pour la recherche.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String url = "jdbc:mysql://localhost:3306/email_manager";
            String user = "root";
            String password = "";

            String sql = "SELECT sender, recipient, subject, body, attachment_name FROM sent_emails " +
                         "WHERE (REGEXP_REPLACE(recipient, '@.*', '') LIKE ? OR subject LIKE ?) " +
                         "ORDER BY id DESC";

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                System.out.println("Mot-clé de recherche : " + keyword);
                System.out.println("Requête SQL : " + sql);

                stmt.setString(1, "%" + keyword + "%");
                stmt.setString(2, "%" + keyword + "%");

                ResultSet rs = stmt.executeQuery();

                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                boolean hasResults = false;

                while (rs.next()) {
                    hasResults = true;
                    textArea.append("De : " + rs.getString("sender") + "\n");
                    textArea.append("À : " + rs.getString("recipient") + "\n");
                    textArea.append("Objet : " + rs.getString("subject") + "\n");
                    textArea.append("Message : " + rs.getString("body") + "\n");
                    textArea.append("Pièce jointe : " + rs.getString("attachment_name") + "\n");
                    textArea.append("--------------------------------------------------\n");
                }

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));

                if (hasResults) {
                    JOptionPane.showMessageDialog(null, scrollPane, "Résultats de la recherche", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Aucun résultat trouvé pour : " + keyword, "Résultats de la recherche", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur JDBC : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        sendButton.addActionListener(e -> {
            String from = (String) mailboxSelector.getSelectedItem();
            String to = recipientField.getText();
            String subject = subjectField.getText();
            String message = bodyArea.getText();
            String password = passwordField.getText(); // Get password from UI field

            if (from == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte email.");
                return;
            }
            if (to.isEmpty() || subject.isEmpty() || message.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs, y compris le mot de passe.");
                return;
            }
            if (!to.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, "Adresse e-mail invalide.");
                return;
            }

            // Verify password matches the stored one (optional, for added security)
            String storedPassword = emailPasswords.get(from);
            if (!password.equals(storedPassword)) {
                JOptionPane.showMessageDialog(this, "Mot de passe incorrect pour ce compte.");
                return;
            }

            envoyerEmail(from, password, to, subject, message, attachmentFile);
            passwordField.setText(""); // Clear password field after sending
        });
        sendToListButton.addActionListener(e -> {
            String from = (String) mailboxSelector.getSelectedItem();
            String recipients = recipientListField.getText();
            String subject = subjectField.getText();
            String message = bodyArea.getText();
            String password = passwordField.getText(); // Get password from UI field

            if (from == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte email.");
                return;
            }
            if (recipients.isEmpty() || subject.isEmpty() || message.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs, y compris le mot de passe.");
                return;
            }

            // Verify password matches the stored one (optional, for added security)
            String storedPassword = emailPasswords.get(from);
            if (!password.equals(storedPassword)) {
                JOptionPane.showMessageDialog(this, "Mot de passe incorrect pour ce compte.");
                return;
            }

            String[] recipientArray = recipients.split(",");
            for (String recipient : recipientArray) {
                recipient = recipient.trim();
                if (!recipient.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    JOptionPane.showMessageDialog(this, "Adresse e-mail invalide : " + recipient);
                    return;
                }
            }

            for (String recipient : recipientArray) {
                envoyerEmail(from, password, recipient.trim(), subject, message, attachmentFile);
            }
            passwordField.setText(""); // Clear password field after sending
        });
    }
    
    // Le reste du code (loadEmailAccounts, addEmailAccount, deleteEmailAccount, envoyerEmail, etc.) reste inchangé
    private void loadEmailAccounts() {
        String url = "jdbc:mysql://localhost:3306/email_manager";
        String user = "root";
        String password = "";

        String sql = "SELECT email, password FROM email_accounts";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            mailboxSelector.removeAllItems();
            emailPasswords.clear();

            while (rs.next()) {
                String email = rs.getString("email");
                String pwd = rs.getString("password");
                mailboxSelector.addItem(email);
                emailPasswords.put(email, pwd);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des comptes : " + ex.getMessage());
        }
    }

    private boolean addEmailAccount(String email, String password) {
        String url = "jdbc:mysql://localhost:3306/email_manager";
        String user = "root";
        String dbPassword = "";

        String sql = "INSERT INTO email_accounts (email, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;

        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23000")) {
                return false;
            }
            JOptionPane.showMessageDialog(this, "Erreur JDBC : " + ex.getMessage());
            return false;
        }
    }

    private boolean deleteEmailAccount(String email) {
        String url = "jdbc:mysql://localhost:3306/email_manager";
        String user = "root";
        String password = "";

        String sql = "DELETE FROM email_accounts WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur JDBC : " + ex.getMessage());
            return false;
        }
    }

    private void envoyerEmail(String fromEmail, String password, String toEmail, String subject, String message, File attachment) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(fromEmail));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            mimeMessage.setSubject(subject);

            Multipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(message);
            multipart.addBodyPart(textPart);

            String attachmentName = null;
            if (attachment != null) {
                MimeBodyPart filePart = new MimeBodyPart();
                filePart.attachFile(attachment);
                multipart.addBodyPart(filePart);
                attachmentName = attachment.getName();
            }

            mimeMessage.setContent(multipart);

            Transport.send(mimeMessage);

            archiveEmailToXML(fromEmail, toEmail, subject, message, attachmentName);
            saveEmailToDatabase(fromEmail, toEmail, subject, message, attachmentName);

            JOptionPane.showMessageDialog(this, "Email envoyé avec succès à : " + toEmail);
            recipientField.setText("");
            subjectField.setText("");
            passwordField.setText("");
            bodyArea.setText("");
            attachmentLabel.setText("Aucune pièce jointe");
            attachmentFile = null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }

    private void saveEmailToDatabase(String from, String to, String subject, String body, String attachmentName) {
        String url = "jdbc:mysql://localhost:3306/email_manager";
        String user = "root";
        String password = "";

        String sql = "INSERT INTO sent_emails (sender, recipient, subject, body, attachment_name) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, from);
            stmt.setString(2, to);
            stmt.setString(3, subject);
            stmt.setString(4, body);
            stmt.setString(5, attachmentName != null ? attachmentName : "None");

            stmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur JDBC : " + e.getMessage());
        }
    }

    private void archiveEmailToXML(String from, String to, String subject, String body, String attachmentName) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("Email");
            doc.appendChild(rootElement);

            Element fromElement = doc.createElement("From");
            fromElement.appendChild(doc.createTextNode(from));
            rootElement.appendChild(fromElement);

            Element toElement = doc.createElement("To");
            toElement.appendChild(doc.createTextNode(to));
            rootElement.appendChild(toElement);

            Element subjectElement = doc.createElement("Subject");
            subjectElement.appendChild(doc.createTextNode(subject != null ? subject : ""));
            rootElement.appendChild(subjectElement);

            Element bodyElement = doc.createElement("Body");
            bodyElement.appendChild(doc.createTextNode(body != null ? body : ""));
            rootElement.appendChild(bodyElement);

            Element timestampElement = doc.createElement("Timestamp");
            timestampElement.appendChild(doc.createTextNode(new java.util.Date().toString()));
            rootElement.appendChild(timestampElement);

            Element attachmentElement = doc.createElement("Attachment");
            attachmentElement.appendChild(doc.createTextNode(attachmentName != null ? attachmentName : "None"));
            rootElement.appendChild(attachmentElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            File archivesDir = new File("email_archives");
            if (!archivesDir.exists()) {
                archivesDir.mkdirs();
            }

            String fileName = "email_archives/email_" + System.currentTimeMillis() + ".xml";
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);

            System.out.println("Fichier XML généré à : " + new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'archivage XML : " + e.getMessage());
        }
    }

    private void recevoirEmails(String email, String password) {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.host", "imap.gmail.com");
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imap.ssl.enable", "true");

        try {
            if (inbox != null && inbox.isOpen()) inbox.close(false);
            if (store != null && store.isConnected()) store.close();

            Session session = Session.getInstance(props, null);
            store = session.getStore("imaps");
            store.connect("imap.gmail.com", email, password);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int messageCount = inbox.getMessageCount();
            int start = Math.max(1, messageCount - 9);
            messages = inbox.getMessages(start, messageCount);
            emailListModel.clear();
            for (Message message : messages) {
                String subject = message.getSubject() != null ? message.getSubject() : "(Sans sujet)";
                String from = message.getFrom()[0].toString();
                String date = message.getReceivedDate() != null ? message.getReceivedDate().toString() : "Date inconnue";
                String preview = getEmailContent(message).length() > 50 ? getEmailContent(message).substring(0, 50) + "..." : getEmailContent(message);
                emailListModel.addElement(from + " - " + subject + " [" + date + "] - " + preview);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la connexion au compte " + email + " : " + e.getMessage());
            emailListModel.addElement("Erreur de connexion ou aucun e-mail.");
        }
    }

    private String getEmailContent(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return bodyPart.getContent().toString();
                } else if (bodyPart.isMimeType("text/html")) {
                    return bodyPart.getContent().toString();
                }
            }
        }
        return "Contenu non disponible";
    }

    private String getEmailContentType(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return "text/plain";
        } else if (message.isMimeType("text/html")) {
            return "text/html";
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return "text/plain";
                } else if (bodyPart.isMimeType("text/html")) {
                    return "text/html";
                }
            }
        }
        return "text/plain";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Email());
    }
}