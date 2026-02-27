package tn.esprit.farmvision.gestionuser.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.*;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ProfileController {

    @FXML private BorderPane rootPane;
    @FXML private TextField txtNom, txtPrenom, txtEmail, txtRole;
    @FXML private TextField txtTelephone, txtMatricule, txtMatriculeAdmin;
    @FXML private TextArea txtAdresse;
    @FXML private PasswordField txtOldPassword, txtNewPassword, txtConfirmPassword;
    @FXML private VBox paneAgriculteur, paneResponsable, paneAdmin;
    @FXML private Label lblError, lblDateCreation, lblStatut;

    private final UtilisateurService service = new UtilisateurService();
    private Utilisateur currentUser;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("❌ Erreur : Session expirée");
            return;
        }

        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event ->
                    AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
        }

        loadUserData();
    }

    private void loadUserData() {
        txtNom.setText(currentUser.getNom());
        txtPrenom.setText(currentUser.getPrenom());
        txtEmail.setText(currentUser.getEmail());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
        lblDateCreation.setText("📅 Compte créé le : " + sdf.format(currentUser.getDateCreation()));
        lblStatut.setText(currentUser.isActivated() ? "✅ Statut : Activé" : "⏳ Statut : En attente");
        lblStatut.setStyle(currentUser.isActivated() ?
                "-fx-text-fill: #198754; -fx-font-weight: bold;" :
                "-fx-text-fill: #ffc107; -fx-font-weight: bold;");

        if (currentUser instanceof Administrateur admin) {
            txtRole.setText("👨‍💼 Administrateur");
            txtMatriculeAdmin.setText(admin.getMatricule());
            paneAdmin.setVisible(true);
            paneAdmin.setManaged(true);
        } else if (currentUser instanceof Agriculteur agri) {
            txtRole.setText("🌾 Agriculteur");
            txtTelephone.setText(agri.getTelephone());
            txtAdresse.setText(agri.getAdresse());
            paneAgriculteur.setVisible(true);
            paneAgriculteur.setManaged(true);
        } else if (currentUser instanceof ResponsableExploitation resp) {
            txtRole.setText("👔 Responsable d'Exploitation");
            txtMatricule.setText(resp.getMatricule());
            paneResponsable.setVisible(true);
            paneResponsable.setManaged(true);
        }
    }

    @FXML
    private void handleSave() {
        lblError.setVisible(false);

        try {
            // Validation des champs de base
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();

            if (nom.isEmpty()) throw new Exception("Le nom est obligatoire");
            if (prenom.isEmpty()) throw new Exception("Le prénom est obligatoire");
            if (email.isEmpty()) throw new Exception("L'email est obligatoire");
            if (!email.contains("@")) throw new Exception("Format email invalide");

            // Validation des champs spécifiques
            if (currentUser instanceof Agriculteur) {
                String tel = txtTelephone.getText().trim();
                String adr = txtAdresse.getText().trim();
                if (tel.isEmpty()) throw new Exception("Le téléphone est obligatoire");
                if (tel.length() != 8 || !tel.matches("\\d+"))
                    throw new Exception("Le téléphone doit contenir 8 chiffres");
                if (adr.isEmpty()) throw new Exception("L'adresse est obligatoire");

                ((Agriculteur) currentUser).setTelephone(tel);
                ((Agriculteur) currentUser).setAdresse(adr);
            }

            // Mettre à jour les informations de base
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);

            // Gestion du changement de mot de passe
            String oldPassword = txtOldPassword.getText();
            String newPassword = txtNewPassword.getText();
            String confirmPassword = txtConfirmPassword.getText();

            if (!oldPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                // Si au moins un champ de mot de passe est rempli
                if (oldPassword.isEmpty()) throw new Exception("Ancien mot de passe requis");
                if (newPassword.isEmpty()) throw new Exception("Nouveau mot de passe requis");
                if (confirmPassword.isEmpty()) throw new Exception("Confirmation requise");

                // Vérifier l'ancien mot de passe
                if (!BCrypt.checkpw(oldPassword, currentUser.getPassword())) {
                    throw new Exception("Ancien mot de passe incorrect");
                }

                if (newPassword.length() < 6) {
                    throw new Exception("Le nouveau mot de passe doit contenir au moins 6 caractères");
                }

                if (!newPassword.equals(confirmPassword)) {
                    throw new Exception("Les nouveaux mots de passe ne correspondent pas");
                }

                // Hacher le nouveau mot de passe
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                currentUser.setPassword(hashedPassword);
            }

            // Sauvegarder dans la BD
            service.update(currentUser);

            // Mettre à jour la session
            SessionManager.getInstance().setCurrentUser(currentUser);

            showSuccess("✅ Profil mis à jour avec succès !");

            // Retour automatique après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::handleRetour);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            showError("❌ " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        handleRetour();
    }

    @FXML
    private void handleRetour() {
        try {
            String fxmlPath = (currentUser instanceof Administrateur) ?
                    "/fxml/AdminDashboard.fxml" : "/fxml/UserDashboard.fxml";

            Stage stage = (Stage) txtNom.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();
            boolean full = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene newScene = new Scene(root, w > 0 ? w : 1000, h > 0 ? h : 700);
            stage.setScene(newScene);
            stage.setTitle("FarmVision - Dashboard");

            if (full) stage.setFullScreen(true);
            else if (max) stage.setMaximized(true);

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            showError("❌ Erreur navigation : " + e.getMessage());
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #dc3545; -fx-font-weight: bold;");
        lblError.setVisible(true);
    }

    private void showSuccess(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #198754; -fx-font-weight: bold;");
        lblError.setVisible(true);
    }
}