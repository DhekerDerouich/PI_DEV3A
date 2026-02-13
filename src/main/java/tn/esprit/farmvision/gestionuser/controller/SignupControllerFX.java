package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import javafx.fxml.FXML;
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

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 🌾 Contrôleur de la page d'inscription FarmVision
 * ✅ VERSION CORRIGÉE - Fonctionne avec HBox!
 * 🎮 EASTER EGG: Tapez "FARM" rapidement pour une surprise!
 */
public class SignupControllerFX {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPasswordConfirm;

    @FXML private RadioButton rbAgriculteur;
    @FXML private RadioButton rbResponsable;
    @FXML private ToggleGroup roleGroup;

    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private HBox paneAgriculteur;  // ✅ HBox

    @FXML private TextField txtMatricule;
    @FXML private VBox paneResponsable;  // ✅ VBox

    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblTelError;
    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private Button btnRegister;
    @FXML private Button btnCancel;
    @FXML private Hyperlink linkLogin;

    @FXML private BorderPane rootPane;

    private final UtilisateurService service = new UtilisateurService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");

    @FXML
    private void initialize() {
        System.out.println("📋 Initialisation SignupControllerFX");

        // Animation d'entrée
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);

            // 🎮 ACTIVER L'EASTER EGG
            rootPane.setOnKeyTyped(event -> {
                AnimationManager.handleSecretCode(event.getCharacter(), rootPane);
            });
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }

        // ✅ CORRECTION: Masquer les panneaux au départ avec cast en Node
        if (paneAgriculteur != null) {
            paneAgriculteur.setVisible(false);
            paneAgriculteur.setManaged(false);
        }
        if (paneResponsable != null) {
            paneResponsable.setVisible(false);
            paneResponsable.setManaged(false);
        }

        // Listeners sur les radio buttons
        rbAgriculteur.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                System.out.println("✅ Agriculteur sélectionné");
                // Afficher paneAgriculteur, masquer paneResponsable
                paneAgriculteur.setVisible(true);
                paneAgriculteur.setManaged(true);
                paneResponsable.setVisible(false);
                paneResponsable.setManaged(false);
            }
        });

        rbResponsable.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                System.out.println("✅ Responsable sélectionné");
                // Afficher paneResponsable, masquer paneAgriculteur
                paneResponsable.setVisible(true);
                paneResponsable.setManaged(true);
                paneAgriculteur.setVisible(false);
                paneAgriculteur.setManaged(false);
            }
        });

        // Sélectionner Agriculteur par défaut
        rbAgriculteur.setSelected(true);
        if (paneAgriculteur != null) {
            paneAgriculteur.setVisible(true);
            paneAgriculteur.setManaged(true);
        }

        // Validation email en temps réel
        txtEmail.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !EMAIL_PATTERN.matcher(newVal).matches()) {
                lblEmailError.setText("Format email invalide");
                lblEmailError.setVisible(true);
            } else {
                lblEmailError.setVisible(false);
            }
        });

        // Validation mot de passe
        txtPassword.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && newVal.length() < 6) {
                lblPasswordError.setText("Le mot de passe doit contenir au moins 6 caractères");
                lblPasswordError.setVisible(true);
            } else {
                lblPasswordError.setVisible(false);
            }
        });

        // Validation téléphone
        txtTelephone.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !PHONE_PATTERN.matcher(newVal).matches()) {
                lblTelError.setText("Le téléphone doit contenir exactement 8 chiffres");
                lblTelError.setVisible(true);
            } else {
                lblTelError.setVisible(false);
            }
        });

        System.out.println("✅ SignupControllerFX initialisé");
        System.out.println("🎮 Easter Egg activé! Tapez 'FARM' rapidement!");
    }

    @FXML
    private void handleRegister() {
        lblError.setVisible(false);
        lblSuccess.setVisible(false);

        if (!validateFields()) {
            return;
        }

        AnimationManager.startLoadingButton(btnRegister, "S'inscrire", "Inscription...");

        try {
            Utilisateur newUser;

            if (rbAgriculteur.isSelected()) {
                newUser = new Agriculteur(
                        txtNom.getText().trim(),
                        txtPrenom.getText().trim(),
                        txtEmail.getText().trim(),
                        txtPassword.getText(),
                        txtTelephone.getText().trim(),
                        txtAdresse.getText().trim()
                );
            } else {
                newUser = new ResponsableExploitation(
                        txtNom.getText().trim(),
                        txtPrenom.getText().trim(),
                        txtEmail.getText().trim(),
                        txtPassword.getText(),
                        txtMatricule.getText().trim()
                );
            }

            service.register(newUser);

            AnimationManager.stopLoadingButton(btnRegister, "S'inscrire");

            lblSuccess.setText("✅ Inscription réussie ! Votre compte est en attente de validation par un administrateur.");
            lblSuccess.setVisible(true);
            lblSuccess.setManaged(true);

            btnRegister.setDisable(true);

            // Rediriger vers login après 3 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(this::handleGoToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            AnimationManager.stopLoadingButton(btnRegister, "S'inscrire");
            AnimationManager.showError(lblError, "❌ Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (txtNom.getText().trim().isEmpty()) {
            errors.append("• Le nom est obligatoire\n");
        }
        if (txtPrenom.getText().trim().isEmpty()) {
            errors.append("• Le prénom est obligatoire\n");
        }
        if (txtEmail.getText().trim().isEmpty()) {
            errors.append("• L'email est obligatoire\n");
        } else if (!EMAIL_PATTERN.matcher(txtEmail.getText()).matches()) {
            errors.append("• Format email invalide\n");
        }
        if (txtPassword.getText().isEmpty()) {
            errors.append("• Le mot de passe est obligatoire\n");
        } else if (txtPassword.getText().length() < 6) {
            errors.append("• Le mot de passe doit contenir au moins 6 caractères\n");
        }
        if (!txtPassword.getText().equals(txtPasswordConfirm.getText())) {
            errors.append("• Les mots de passe ne correspondent pas\n");
        }

        if (rbAgriculteur.isSelected()) {
            if (txtTelephone.getText().trim().isEmpty()) {
                errors.append("• Le téléphone est obligatoire\n");
            } else if (!PHONE_PATTERN.matcher(txtTelephone.getText()).matches()) {
                errors.append("• Le téléphone doit contenir exactement 8 chiffres\n");
            }
            if (txtAdresse.getText().trim().isEmpty()) {
                errors.append("• L'adresse est obligatoire\n");
            }
        } else if (rbResponsable.isSelected()) {
            if (txtMatricule.getText().trim().isEmpty()) {
                errors.append("• Le matricule est obligatoire\n");
            }
        }

        if (errors.length() > 0) {
            AnimationManager.showError(lblError, errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    /**
     * ✅ Retour au login avec maintien du plein écran
     */
    @FXML
    private void handleGoToLogin() {
        try {
            System.out.println("🔄 Retour vers Login...");

            Stage stage = (Stage) btnRegister.getScene().getWindow();

            // Sauvegarder l'état
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            // Charger Login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // Créer nouvelle scène
            Scene scene = new Scene(root, width > 0 ? width : 900, height > 0 ? height : 700);
            stage.setScene(scene);
            stage.setTitle("FarmVision - Connexion");

            // Restaurer l'état
            if (isFullScreen) {
                stage.setFullScreen(true);
            } else if (isMaximized) {
                stage.setMaximized(true);
            }

            // Animation
            root.setOpacity(0);
            AnimationManager.fadeInPage(root);

            stage.show();

            System.out.println("✅ Navigation vers Login réussie!");

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation: " + e.getMessage());
            e.printStackTrace();
            AnimationManager.showError(lblError, "Impossible de retourner au login: " + e.getMessage());
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

    @FXML
    private void onButtonHover() {
        if (btnRegister != null) {
            AnimationManager.buttonHoverEffect(btnRegister);
        }
    }

    @FXML
    private void onButtonExit() {
        if (btnRegister != null) {
            AnimationManager.buttonExitEffect(btnRegister);
        }
    }
}