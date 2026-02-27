package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ResetPasswordController {

    @FXML private VBox rootPane;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;
    @FXML private Button btnReset;
    @FXML private Button btnCancel;

    private String email;
    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    private void initialize() {
        AnimationManager.fadeInPage(rootPane);
        txtNewPassword.requestFocus();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleReset() {
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        // Validation
        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (newPass.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        btnReset.setDisable(true);
        btnReset.setText("⏳ Mise à jour...");

        try {
            // Récupérer l'utilisateur
            Utilisateur user = userService.findByEmail(email);

            if (user == null) {
                showError("Utilisateur introuvable");
                btnReset.setDisable(false);
                btnReset.setText("🔑 Réinitialiser");
                return;
            }

            // ✅ Méthode 1: Utiliser resetPassword (plus simple)
            boolean success = userService.resetPassword(user.getId(), newPass);

            if (success) {
                showSuccess("✅ Mot de passe mis à jour avec succès!");

                // Retour au login après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(this::navigateToLogin);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showError("❌ Erreur lors de la mise à jour");
                btnReset.setDisable(false);
                btnReset.setText("🔑 Réinitialiser");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Erreur: " + e.getMessage());
            btnReset.setDisable(false);
            btnReset.setText("🔑 Réinitialiser");
        }
    }

    @FXML
    private void handleCancel() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblMessage.setText("❌ " + message);
        lblMessage.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }

    private void showSuccess(String message) {
        lblMessage.setText("✅ " + message);
        lblMessage.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }
}