package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.model.ResponsableExploitation;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.service.EmailService;
import java.io.IOException;
import java.util.regex.Pattern;

public class SignupControllerFX {

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtTelephone, txtAdresse, txtMatricule;
    @FXML private PasswordField txtPassword, txtPasswordConfirm;
    @FXML private RadioButton rbAgriculteur, rbResponsable;
    @FXML private HBox paneAgriculteur;
    @FXML private VBox paneResponsable;
    @FXML private Label lblEmailError, lblPasswordError, lblTelError, lblError, lblSuccess;
    @FXML private Button btnRegister;
    @FXML private BorderPane rootPane;

    private final UtilisateurService service = new UtilisateurService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");

    @FXML
    private void initialize() {
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event -> AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }

        if (paneAgriculteur != null) {
            paneAgriculteur.setVisible(false);
            paneAgriculteur.setManaged(false);
        }
        if (paneResponsable != null) {
            paneResponsable.setVisible(false);
            paneResponsable.setManaged(false);
        }

        rbAgriculteur.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                paneAgriculteur.setVisible(true);
                paneAgriculteur.setManaged(true);
                paneResponsable.setVisible(false);
                paneResponsable.setManaged(false);
            }
        });

        rbResponsable.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                paneResponsable.setVisible(true);
                paneResponsable.setManaged(true);
                paneAgriculteur.setVisible(false);
                paneAgriculteur.setManaged(false);
            }
        });

        rbAgriculteur.setSelected(true);
        if (paneAgriculteur != null) {
            paneAgriculteur.setVisible(true);
            paneAgriculteur.setManaged(true);
        }

        // Validation email en temps réel
        txtEmail.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                lblEmailError.setVisible(false);
                return;
            }
            if (!EMAIL_PATTERN.matcher(newVal.trim()).matches()) {
                if (!newVal.contains("@")) lblEmailError.setText("❌ L'email doit contenir @");
                else if (newVal.startsWith("@")) lblEmailError.setText("❌ L'email ne peut pas commencer par @");
                else if (newVal.endsWith("@")) lblEmailError.setText("❌ Domaine manquant après @");
                else if (!newVal.substring(newVal.indexOf("@")).contains(".")) lblEmailError.setText("❌ Extension manquante (ex: .com, .tn)");
                else lblEmailError.setText("❌ Format email invalide");
                lblEmailError.setVisible(true);
            } else {
                lblEmailError.setVisible(false);
            }
        });

        txtPassword.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                lblPasswordError.setVisible(false);
                return;
            }
            if (newVal.length() < 6) {
                lblPasswordError.setText("Minimum 6 caractères");
                lblPasswordError.setVisible(true);
            } else {
                lblPasswordError.setVisible(false);
            }
        });

        txtTelephone.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                lblTelError.setVisible(false);
                return;
            }
            if (!PHONE_PATTERN.matcher(newVal.trim()).matches()) {
                lblTelError.setText("8 chiffres requis");
                lblTelError.setVisible(true);
            } else {
                lblTelError.setVisible(false);
            }
        });

        System.out.println("Signup initialize terminé.");
    }

    @FXML
    private void handleRegister() {
        lblError.setVisible(false);
        lblSuccess.setVisible(false);

        if (!validateFields()) return;

        AnimationManager.startLoadingButton(btnRegister, "S'inscrire", "Inscription...");

        try {
            Utilisateur newUser;

            if (rbAgriculteur.isSelected()) {
                newUser = new Agriculteur(
                        txtNom.getText().trim(), txtPrenom.getText().trim(),
                        txtEmail.getText().trim(), txtPassword.getText(),
                        txtTelephone.getText().trim(), txtAdresse.getText().trim());
            } else {
                newUser = new ResponsableExploitation(
                        txtNom.getText().trim(), txtPrenom.getText().trim(),
                        txtEmail.getText().trim(), txtPassword.getText(),
                        txtMatricule.getText().trim());
            }

            service.register(newUser);

            System.out.println("📧 Envoi email de bienvenue à: " + newUser.getEmail());
            boolean emailSent = EmailService.sendWelcomeEmail(newUser);

            if (emailSent) {
                System.out.println("✅ Email de bienvenue envoyé");
            } else {
                System.out.println("⚠️ Échec envoi email (compte créé quand même)");
            }

            AnimationManager.stopLoadingButton(btnRegister, "S'inscrire");

            lblSuccess.setText("✅ Inscription réussie ! Vérifiez votre email.");
            lblSuccess.setVisible(true);
            btnRegister.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(this::handleGoToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            AnimationManager.stopLoadingButton(btnRegister, "S'inscrire");
            AnimationManager.showError(lblError, "Erreur : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (txtNom.getText().trim().isEmpty()) errors.append("• Le nom est obligatoire\n");
        if (txtPrenom.getText().trim().isEmpty()) errors.append("• Le prénom est obligatoire\n");

        if (txtEmail.getText().trim().isEmpty()) {
            errors.append("• L'email est obligatoire\n");
        } else if (!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            errors.append("• Format email invalide (ex: user@example.com)\n");
        }

        if (txtPassword.getText().isEmpty()) errors.append("• Le mot de passe est obligatoire\n");
        else if (txtPassword.getText().length() < 6) errors.append("• Minimum 6 caractères\n");

        if (!txtPassword.getText().equals(txtPasswordConfirm.getText()))
            errors.append("• Les mots de passe ne correspondent pas\n");

        if (rbAgriculteur.isSelected()) {
            if (txtTelephone.getText().trim().isEmpty()) errors.append("• Le téléphone est obligatoire\n");
            else if (!PHONE_PATTERN.matcher(txtTelephone.getText().trim()).matches())
                errors.append("• Téléphone : 8 chiffres\n");
            if (txtAdresse.getText().trim().isEmpty()) errors.append("• L'adresse est obligatoire\n");
        } else if (rbResponsable.isSelected()) {
            if (txtMatricule.getText().trim().isEmpty()) errors.append("• Le matricule est obligatoire\n");
        }

        if (errors.length() > 0) {
            AnimationManager.showError(lblError, errors.toString());
            return false;
        }
        return true;
    }

    @FXML private void handleCancel() { clearForm(); }

    @FXML
    private void handleGoToLogin() {
        try {
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();
            boolean full = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, w > 0 ? w : 900, h > 0 ? h : 700);
            stage.setScene(scene);
            stage.setTitle("FarmVision - Connexion");

            if (full) stage.setFullScreen(true);
            else if (max) stage.setMaximized(true);

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();
        } catch (Exception e) {
            AnimationManager.showError(lblError, "Erreur: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtPasswordConfirm.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtMatricule.clear();
        lblError.setVisible(false);
        lblSuccess.setVisible(false);
        lblEmailError.setVisible(false);
        lblPasswordError.setVisible(false);
        lblTelError.setVisible(false);
    }

    @FXML private void onButtonHover() { AnimationManager.buttonHoverEffect(btnRegister); }
    @FXML private void onButtonExit() { AnimationManager.buttonExitEffect(btnRegister); }
}