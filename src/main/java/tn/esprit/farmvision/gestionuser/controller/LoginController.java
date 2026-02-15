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

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;

    private final UtilisateurService service = new UtilisateurService();

    @FXML
    private void initialize() {
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event ->
                    AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }
        if (logoImageView != null) {
            AnimationManager.animateLogoStart(logoImageView);
        }
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

                if (logoImageView != null && rootPane != null) {
                    AnimationManager.playLogoSuccessAnimation(logoImageView, rootPane,
                            () -> navigateToDashboard(user));
                } else {
                    navigateToDashboard(user);
                }
            } catch (Exception e) {
                AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                AnimationManager.showError(lblError, "Erreur : " + e.getMessage());
            }
        });
        pause.play();
    }

    private void navigateToDashboard(Utilisateur user) {
        try {
            String fxmlPath = (user instanceof Administrateur) ?
                    "/fxml/AdminDashboard.fxml" : "/fxml/UserDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();
            boolean full = stage.isFullScreen();

            Scene newScene = new Scene(root, w > 0 ? w : 1000, h > 0 ? h : 700);
            stage.setScene(newScene);
            stage.setTitle("FarmVision - " + user.getClass().getSimpleName());

            if (full) stage.setFullScreen(true);
            else if (max) stage.setMaximized(true);

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();
        } catch (IOException e) {
            AnimationManager.showError(lblError, "Erreur chargement : " + e.getMessage());
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();
            boolean full = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Signup.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, w > 0 ? w : 900, h > 0 ? h : 700);
            stage.setScene(scene);
            stage.setTitle("FarmVision - Inscription");

            if (full) stage.setFullScreen(true);
            else if (max) stage.setMaximized(true);

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();
        } catch (Exception e) {
            AnimationManager.showError(lblError, "Erreur: " + e.getMessage());
        }
    }

    @FXML private void onButtonHover() { AnimationManager.buttonHoverEffect(btnLogin); }
    @FXML private void onButtonExit() { AnimationManager.buttonExitEffect(btnLogin); }
}