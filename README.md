# Mail-Manager
Mail Manager est une application Java qui permet de gérer plusieurs comptes email via une interface graphique intuitive.Mail Manager
 Elle offre des fonctionnalités pour envoyer, recevoir, et archiver des emails, ainsi que pour gérer une liste de comptes email. L'application utilise une base de données MySQL pour stocker les informations des comptes et des emails envoyés, et génère des archives XML pour les emails envoyés.
Fonctionnalités
généralement absentes dans les serveurs de messagerie classiques : Yahoo, Gmail, Hotmail, etc.  
 Gestion des « Mailings Lists »  
 Envoies groupés
 Archivage des emails  
 Recherche d’emails (multicritères)  
 Gestion de plusieurs boites emails  


Gestion des comptes email :
Ajout et suppression de comptes email.
Authentification sécurisée via un écran de connexion.


Envoi d'emails :
Envoi d'emails à un destinataire unique ou à une liste de destinataires.
Possibilité de joindre des fichiers.
Archivage des emails envoyés dans une base de données MySQL et sous forme de fichiers XML.


Réception d'emails :
Affichage des emails reçus depuis la boîte de réception (IMAP).
Consultation des détails des emails (expéditeur, objet, contenu, date).


Recherche et historique :
Recherche dans l'historique des emails envoyés par destinataire ou objet.
Visualisation des emails envoyés via une interface dédiée.


Interface utilisateur :
Interface graphique construite avec Java Swing.
Support multi-comptes via une liste déroulante.
Prévisualisation des emails dans la boîte de réception.



Prérequis

Java : JDK 8 ou supérieur.
MySQL : Serveur MySQL installé et configuré (version 5.7 ou supérieure recommandée).
Dépendances :
Bibliothèque JavaMail (javax.mail) pour la gestion des emails.
Connecteur JDBC pour MySQL (mysql-connector-java).
Bibliothèques Java standard pour XML (javax.xml).


Accès réseau : Connexion Internet pour envoyer/recevoir des emails via les protocoles SMTP et IMAP.
Compte Gmail : L'application est configurée pour Gmail (SMTP/IMAP). Assurez-vous d'activer "Less secure app access" ou d'utiliser un mot de passe d'application dans les paramètres de sécurité de votre compte Google.

Installation

Configurer la base de données MySQL :
Créez une base de données nommée email_manager :CREATE DATABASE email_manager;
USE email_manager;


Créez les tables nécessaires :CREATE TABLE email_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE sent_emails (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(255) NOT NULL,
    recipient TEXT NOT NULL,
    subject TEXT,
    body TEXT,
    attachment_name VARCHAR(255)
);




Ajouter les dépendances :
Téléchargez et ajoutez les bibliothèques suivantes au projet :
javax.mail.jar (JavaMail API).
mysql-connector-java.jar (par exemple, mysql-connector-java-8.0.27.jar).


Si vous utilisez un IDE comme Eclipse ou IntelliJ, ajoutez ces fichiers JAR au classpath de votre projet.


Configurer les paramètres de connexion :
Assurez-vous que les informations de connexion MySQL dans le code (URL, utilisateur, mot de passe) correspondent à votre configuration locale :String url = "jdbc:mysql://localhost:3306/email_manager";
String user = "root";
String password = "";




Compiler et exécuter :
Compilez les fichiers Java :javac -cp ".;javax.mail.jar;mysql-connector-java.jar" mail/*.java


Exécutez l'application :java -cp ".;javax.mail.jar;mysql-connector-java.jar" mail.LoginScreen





Utilisation

Connexion :
Lancez l'application. Une fenêtre de connexion s'affichera.
Entrez une adresse email et un mot de passe associés à un compte déjà enregistré dans la base de données.


Gestion des comptes :
Cliquez sur "Ajouter un compte" pour ajouter un nouveau compte email.
Sélectionnez un compte dans la liste déroulante pour consulter ses emails.
Utilisez "Supprimer un compte" pour retirer un compte.


Envoi d'emails :
Remplissez les champs "Destinataire", "Objet", "Message", et "Mot de passe".
Joignez un fichier si nécessaire via "Joindre un fichier".
Cliquez sur "Envoyer" pour envoyer à un destinataire unique, ou "Envoyer à une Liste" pour envoyer à plusieurs destinataires (séparés par des virgules).


Réception d'emails :
Les emails de la boîte de réception s'affichent automatiquement dans la liste.
Double-cliquez sur un email pour voir ses détails.


Recherche et historique :
Utilisez la barre de recherche pour trouver des emails envoyés par destinataire ou objet.
Cliquez sur "Voir emails envoyés" pour consulter l'historique complet.



Structure du projet

Email.java : Classe principale contenant l'interface graphique et la logique de gestion des emails.
LoginScreen.java : Écran de connexion pour authentifier les utilisateurs.
Label.java : Composant personnalisé pour les champs de texte dans l'interface.
PassField.java : Composant personnalisé pour les champs de mot de passe.
email_archives/ : Dossier où les emails envoyés sont archivés sous forme de fichiers XML.

Limitations

L'application est actuellement configurée pour Gmail uniquement (SMTP/IMAP). Une adaptation pour d'autres fournisseurs d'email nécessite des modifications dans les propriétés de connexion.
Les mots de passe sont stockés en clair dans la base de données (non sécurisé). Une amélioration future pourrait inclure le chiffrement.
La gestion des pièces jointes dans les emails reçus n'est pas implémentée.

Améliorations futures

Ajouter le support pour d'autres fournisseurs d'email (Outlook, Yahoo, etc.).
Implémenter le chiffrement des mots de passe dans la base de données.
Ajouter la possibilité de répondre ou transférer des emails.
Améliorer la gestion des pièces jointes pour les emails reçus.

Auteurs

[Votre nom ou pseudonyme]

Licence
Ce projet est sous licence MIT. Vous êtes libre de l'utiliser, le modifier et le distribuer selon les termes de cette licence.

