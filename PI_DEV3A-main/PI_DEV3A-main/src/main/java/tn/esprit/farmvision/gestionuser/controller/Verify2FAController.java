package tn.esprit.farmvision.gestionuser.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.TwoFactorAuthService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;

import java.io.IOException;

/**
 * 🔐 Contrôleur pour la vérification du code 2FA
 */
public class Verify2FAController {

    @FXML private VBox rootPane;
    @FXML private TextField txtCode1;
    @FXML private TextField txtCode2;
    @FXML private TextField txtCode3;
    @FXML private TextField txtCode4;
    @FXML private TextField txtCode5;
    @FXML private TextField txtCode6;
    @FXML private Label lblMessage;
    @FXML private Label lblEmail;
    @FXML private Button btnVerify;
    @FXML private Button btnResend;

    private Utilisateur pendingUser; // L'utilisateur en attente de vérification

    @FXML
    private void initialize() {
        System.out.println("🔐 Initialisation Verify2FAController");

        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
        }

        // Auto-focus sur le champ suivant après saisie d'un chiffre
        setupAutoAdvance();
    }

    /**
     * Configurer l'utilisateur en attente de vérification
     */
    public void setPendingUser(Utilisateur user) {
        this.pendingUser = user;

        if (lblEmail != null && user != null) {
            // Masquer partiellement l'email pour la sécurité
            String email = user.getEmail();
            String maskedEmail = maskEmail(email);
            lblEmail.setText("📧 Code envoyé à: " + maskedEmail);
        }
    }

    /**
     * Configuration de la navigation automatique entre les champs
     */
    private void setupAutoAdvance() {
        TextField[] fields = {txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6};

        for (int i = 0; i < fields.length; i++) {
            final int index = i;

            fields[i].textProperty().addListener((obs, oldVal, newVal) -> {
                // Autoriser seulement les chiffres
                if (!newVal.matches("\\d*")) {
                    fields[index].setText(oldVal);
                    return;
                }

                // Limiter à 1 chiffre
                if (newVal.length() > 1) {
                    fields[index].setText(newVal.substring(0, 1));
                    return;
                }

                // Passer au champ suivant si un chiffre est saisi
                if (newVal.length() == 1 && index < fields.length - 1) {
                    fields[index + 1].requestFocus();
                }

                // Auto-vérifier si tous les champs sont remplis
                if (index == fields.length - 1 && newVal.length() == 1) {
                    checkAllFieldsFilled();
                }
            });

            // Retour au champ précédent avec Backspace
            fields[i].setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("BACK_SPACE")) {
                    if (fields[index].getText().isEmpty() && index > 0) {
                        fields[index - 1].requestFocus();
                    }
                }
            });
        }

        // Focus sur le premier champ
        txtCode1.requestFocus();
    }

    /**
     * Vérifier si tous les champs sont remplis
     */
    private void checkAllFieldsFilled() {
        String code = getEnteredCode();
        if (code.length() == 6) {
            // Petite pause pour l'UX
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(this::handleVerify);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Récupérer le code complet saisi
     */
    private String getEnteredCode() {
        return txtCode1.getText() + txtCode2.getText() + txtCode3.getText() +
                txtCode4.getText() + txtCode5.getText() + txtCode6.getText();
    }

    /**
     * Vérifier le code 2FA
     */
    @FXML
    private void handleVerify() {
        if (pendingUser == null) {
            showError("❌ Erreur: Utilisateur non défini");
            return;
        }

        String code = getEnteredCode();

        if (code.length() != 6) {
            showError("⚠️ Veuillez saisir les 6 chiffres");
            return;
        }

        btnVerify.setDisable(true);
        lblMessage.setText("🔄 Vérification en cours...");
        lblMessage.setStyle("-fx-text-fill: #3498db;");

        // Vérifier le code
        boolean isValid = TwoFactorAuthService.verifyCode(pendingUser.getEmail(), code);

        if (isValid) {
            lblMessage.setText("✅ Code valide! Connexion...");
            lblMessage.setStyle("-fx-text-fill: #27ae60;");

            // Sauvegarder l'utilisateur dans la session
            SessionManager.getInstance().setCurrentUser(pendingUser);

            // Rediriger vers le dashboard
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::navigateToDashboard);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            showError("❌ Code invalide ou expiré");
            btnVerify.setDisable(false);
            clearFields();
            txtCode1.requestFocus();
        }
    }

    /**
     * Renvoyer un nouveau code
     */
    @FXML
    private void handleResend() {
        if (pendingUser == null) {
            showError("❌ Erreur: Utilisateur non défini");
            return;
        }

        btnResend.setDisable(true);
        lblMessage.setText("📧 Envoi d'un nouveau code...");
        lblMessage.setStyle("-fx-text-fill: #3498db;");

        boolean sent = TwoFactorAuthService.resendCode(pendingUser);

        if (sent) {
            lblMessage.setText("✅ Nouveau code envoyé! Vérifiez votre email.");
            lblMessage.setStyle("-fx-text-fill: #27ae60;");
            clearFields();
            txtCode1.requestFocus();
        } else {
            showError("❌ Erreur lors de l'envoi du code");
        }

        // Réactiver après 30 secondes
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                javafx.application.Platform.runLater(() -> btnResend.setDisable(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Annuler et retourner au login
     */
    @FXML
    private void handleCancel() {
        try {
            Stage stage = (Stage) txtCode1.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("FarmVision - Connexion");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("❌ Erreur retour login");
        }
    }

    /**
     * Navigation vers le dashboard approprié
     */
    private void navigateToDashboard() {
        try {
            String fxmlPath = (pendingUser instanceof Administrateur) ?
                    "/fxml/AdminDashboard.fxml" : "/fxml/UserDashboard.fxml";

            Stage stage = (Stage) txtCode1.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isFullScreen = stage.isFullScreen();

            stage.setScene(new Scene(root, width, height));
            stage.setTitle("FarmVision - Dashboard");

            if (isFullScreen) {
                stage.setFullScreen(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("❌ Erreur navigation dashboard");
        }
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    /**
     * Effacer tous les champs
     */
    private void clearFields() {
        txtCode1.clear();
        txtCode2.clear();
        txtCode3.clear();
        txtCode4.clear();
        txtCode5.clear();
        txtCode6.clear();
    }

    /**
     * Masquer partiellement l'email
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        // Masquer tout sauf les 2 premiers caractères
        if (username.length() <= 2) {
            return email;
        }

        String masked = username.substring(0, 2) + "***@" + domain;
        return masked;
    }
}