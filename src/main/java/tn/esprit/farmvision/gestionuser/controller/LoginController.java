package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.service.TwoFactorAuthService;

import java.io.IOException;

/**
 * 🔐 Contrôleur de connexion avec authentification 2FA
 * ✅ Admin se connecte DIRECTEMENT (sans 2FA)
 * ✅ Autres utilisateurs passent par 2FA
 */
public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkSignup;
    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;

    private final UtilisateurService service = new UtilisateurService();

    @FXML
    private void initialize() {
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event -> {
                AnimationManager.handleSecretCode(event.getCharacter(), rootPane);
            });
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }

        if (logoImageView != null) {
            AnimationManager.animateLogoStart(logoImageView);
        }

        System.out.println("🔐 LoginController initialisé avec 2FA (Admin bypass)");
    }

    @FXML
    private void handleLogin() {
        lblError.setVisible(false);

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            AnimationManager.showError(lblError, "Email et mot de passe obligatoires");
            return;
        }

        AnimationManager.startLoadingButton(btnLogin, "Se connecter", "Connexion...");

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(event -> {
            try {
                Utilisateur user = service.login(email, password);

                if (user == null) {
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                    AnimationManager.showError(lblError, "Email ou mot de passe incorrect");
                    return;
                }

                if (!user.isActivated() && !(user instanceof Administrateur)) {
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                    AnimationManager.showError(lblError, "Compte en attente de validation");
                    return;
                }

                // ✅ NOUVEAU : Admin se connecte DIRECTEMENT
                if (user instanceof Administrateur) {
                    System.out.println("👨‍💼 Admin détecté - Connexion directe (sans 2FA)");
                    SessionManager.getInstance().setCurrentUser(user);
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");

                    if (logoImageView != null && rootPane != null) {
                        AnimationManager.playLogoSuccessAnimation(
                                logoImageView,
                                rootPane,
                                () -> navigateToDashboard(user)
                        );
                    } else {
                        navigateToDashboard(user);
                    }
                } else {
                    // ✅ Utilisateurs normaux : 2FA obligatoire
                    System.out.println("👤 Utilisateur standard - Envoi code 2FA à: " + user.getEmail());

                    boolean codeSent = TwoFactorAuthService.sendVerificationCode(user);

                    if (!codeSent) {
                        AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                        AnimationManager.showError(lblError, "❌ Erreur envoi code 2FA. Réessayez.");
                        return;
                    }

                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");

                    if (logoImageView != null && rootPane != null) {
                        AnimationManager.playLogoSuccessAnimation(
                                logoImageView,
                                rootPane,
                                () -> navigate2FAVerification(user)
                        );
                    } else {
                        navigate2FAVerification(user);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                AnimationManager.showError(lblError, "Erreur connexion : " + e.getMessage());
            }
        });
        pause.play();
    }

    /**
     * 🏠 Navigation directe vers le dashboard (pour Admin)
     */
    private void navigateToDashboard(Utilisateur user) {
        try {
            String fxmlPath = (user instanceof Administrateur)
                    ? "/fxml/AdminDashboard.fxml"
                    : "/fxml/UserDashboard.fxml";

            System.out.println("🔄 Navigation vers : " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isFullScreen = stage.isFullScreen();

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1000,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle("FarmVision - Dashboard");

            if (isFullScreen) {
                stage.setFullScreen(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

            System.out.println("✅ Dashboard affiché avec succès");

        } catch (IOException e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur navigation : " + e.getMessage());
        }
    }

    /**
     * 🔐 Navigation vers la page de vérification 2FA (pour utilisateurs normaux)
     */
    private void navigate2FAVerification(Utilisateur user) {
        try {
            System.out.println("🔐 Navigation vers vérification 2FA...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Verify2FA.fxml"));
            Parent root = loader.load();

            Verify2FAController controller = loader.getController();
            controller.setPendingUser(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isFullScreen = stage.isFullScreen();

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 900,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle("FarmVision - Vérification 2FA");

            if (isFullScreen) {
                stage.setFullScreen(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

            System.out.println("✅ Page 2FA affichée");

        } catch (IOException e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur chargement page 2FA: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            if (linkSignup != null) {
                AnimationManager.slideToRight(linkSignup, "/fxml/Signup.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Impossible d'ouvrir l'inscription: " + e.getMessage());
        }
    }

    @FXML
    private void onButtonHover() {
        if (btnLogin != null) {
            AnimationManager.buttonHoverEffect(btnLogin);
        }
    }

    @FXML
    private void onButtonExit() {
        if (btnLogin != null) {
            AnimationManager.buttonExitEffect(btnLogin);
        }
    }
}