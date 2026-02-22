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
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import javafx.scene.control.ComboBox;

import java.io.IOException;

public class UserDashboardController {

    @FXML private Label lblTitre, lblRole;
    @FXML private BorderPane rootPane;

    @FXML
    private void initialize() {
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            lblTitre.setText("❌ Erreur : session non trouvée");
            lblTitre.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            return;
        }

        String role = user.getClass().getSimpleName();
        String emoji = switch (role) {
            case "Agriculteur" -> "👨‍🌾";
            case "ResponsableExploitation" -> "👔";
            default -> "👤";
        };

        lblTitre.setText(emoji + " Bienvenue " + role + " " + user.getNomComplet() + " !");
        lblRole.setText("Vous êtes connecté en tant que : " + role);

        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event ->
                    AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }

    }

    @FXML
    private void ouvrirEspace() {
        // À implémenter selon le rôle
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

    private void navigateToPage(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) lblTitre.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();
            boolean full = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene newScene = new Scene(root, w > 0 ? w : 1000, h > 0 ? h : 700);
            stage.setScene(newScene);
            stage.setTitle(title);

            if (full) stage.setFullScreen(true);
            else if (max) stage.setMaximized(true);

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}