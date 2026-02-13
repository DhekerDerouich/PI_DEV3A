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

import java.io.IOException;

/**
 * 🌾 Contrôleur de la page de connexion FarmVision
 * ✅ Corrections: Navigation plein écran + Animation logo sans rotation
 * 🎮 EASTER EGG: Tapez "FARM" rapidement pour une surprise!
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
        // Animation d'entrée élégante de la page
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);

            // 🎮 ACTIVER L'EASTER EGG - Tapez "FARM" rapidement!
            rootPane.setOnKeyTyped(event -> {
                AnimationManager.handleSecretCode(event.getCharacter(), rootPane);
            });

            // S'assurer que le rootPane peut recevoir les événements clavier
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }

        // Animation du logo au démarrage (pulse léger)
        if (logoImageView != null) {
            AnimationManager.animateLogoStart(logoImageView);
        }

        System.out.println("🎮 Easter Egg activé! Tapez 'FARM' rapidement pour une surprise!");
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

                SessionManager.getInstance().setCurrentUser(user);

                // ✅ Animation du logo SANS ROTATION
                if (logoImageView != null && rootPane != null) {
                    AnimationManager.playLogoSuccessAnimation(
                            logoImageView,
                            rootPane,
                            () -> navigateToDashboard(user)
                    );
                } else {
                    navigateToDashboard(user);
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
     * ✅ Navigation vers le dashboard avec maintien du plein écran
     */
    private void navigateToDashboard(Utilisateur user) {
        try {
            String fxmlPath = (user instanceof Administrateur) ?
                    "/fxml/AdminDashboard.fxml" : "/fxml/UserDashboard.fxml";

            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();

            // ✅ SAUVEGARDER L'ÉTAT ACTUEL DE LA FENÊTRE
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            System.out.println("📐 État actuel: Width=" + currentWidth +
                    ", Height=" + currentHeight +
                    ", Maximized=" + isMaximized +
                    ", FullScreen=" + isFullScreen);

            // Créer la nouvelle scène avec les bonnes dimensions
            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1000,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle("FarmVision - " + user.getClass().getSimpleName());

            // ✅ RESTAURER L'ÉTAT DE LA FENÊTRE
            if (isFullScreen) {
                stage.setFullScreen(true);
                System.out.println("✅ Plein écran restauré");
            } else if (isMaximized) {
                stage.setMaximized(true);
                System.out.println("✅ Maximisation restaurée");
            }

            // Animation d'entrée
            root.setOpacity(0);
            AnimationManager.fadeInPage(root);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur chargement dashboard : " + e.getMessage());
        }
    }

    /**
     * ✅ CORRIGÉ: Navigation vers Signup avec maintien du plein écran
     */
    @FXML
    private void handleSignUp() {
        System.out.println("🔥 CLIC sur Créer un compte!");

        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            // Sauvegarder l'état
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();
            boolean full = stage.isFullScreen();

            System.out.println("📐 Taille: " + w + "x" + h);

            // Charger Signup.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Signup.fxml"));
            Parent root = loader.load();

            System.out.println("✅ Signup.fxml chargé!");

            // Créer nouvelle scène
            Scene scene = new Scene(root, w > 0 ? w : 900, h > 0 ? h : 700);
            stage.setScene(scene);
            stage.setTitle("FarmVision - Inscription");

            // Restaurer l'état
            if (full) {
                stage.setFullScreen(true);
            } else if (max) {
                stage.setMaximized(true);
            }

            // Animation
            root.setOpacity(0);
            AnimationManager.fadeInPage(root);

            stage.show();

            System.out.println("🎉 Navigation réussie!");

        } catch (Exception e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur: " + e.getMessage());
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