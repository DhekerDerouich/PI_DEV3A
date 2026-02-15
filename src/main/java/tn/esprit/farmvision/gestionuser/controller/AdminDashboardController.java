package tn.esprit.farmvision.gestionuser.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label lblWelcome;
    @FXML private BorderPane rootPane;

    @FXML
    private void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();

        if (!(currentUser instanceof Administrateur)) {
            lblWelcome.setText("⛔ Accès refusé - Réservé aux administrateurs");
            lblWelcome.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return;
        }

        lblWelcome.setText("👋 Bienvenue Administrateur " + currentUser.getNomComplet() + " !");

        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event ->
                    AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }
    }

    @FXML
    private void ouvrirGestionUsers() {
        navigateToGestionUsers(false);
    }

    @FXML
    private void validerComptes() {
        navigateToGestionUsers(true);
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Ouvrir la page profil
     */
    @FXML
    private void ouvrirProfil() {
        navigateToPage("/fxml/Profile.fxml", "FarmVision - Mon Profil");
    }

    @FXML
    private void logout() {
        SessionManager.getInstance().logout();
        navigateToPage("/fxml/Login.fxml", "FarmVision - Connexion");
    }

    private void navigateToGestionUsers(boolean filterPendingOnly) {
        try {
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GestionUsers.fxml"));
            Parent root = loader.load();

            GestionUsersControllerFX controller = loader.getController();
            if (filterPendingOnly) {
                controller.showPendingAccountsOnly();
            }

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1200,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle("FarmVision - " + (filterPendingOnly ? "Comptes en Attente" : "Gestion des Utilisateurs"));

            if (isFullScreen) {
                stage.setFullScreen(true);
            } else if (isMaximized) {
                stage.setMaximized(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToPage(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1200,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle(title);

            if (isFullScreen) {
                stage.setFullScreen(true);
            } else if (isMaximized) {
                stage.setMaximized(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}