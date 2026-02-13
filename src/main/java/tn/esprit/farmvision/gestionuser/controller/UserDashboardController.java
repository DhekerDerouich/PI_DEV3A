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
import tn.esprit.farmvision.gestionuser.util.AnimationManager;

import java.io.IOException;

/**
 * 🌾 Dashboard Utilisateur FarmVision
 * ✅ Maintien du plein écran lors de la navigation
 * 🎮 Easter egg activé!
 */
public class UserDashboardController {

    @FXML private Label lblTitre;
    @FXML private Label lblRole;
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

        // Emojis selon le rôle
        String emoji = switch (role) {
            case "Agriculteur" -> "👨‍🌾";
            case "ResponsableExploitation" -> "👔";
            default -> "👤";
        };

        lblTitre.setText(emoji + " Bienvenue " + role + " " + user.getNomComplet() + " !");
        lblRole.setText("Vous êtes connecté en tant que : " + role);

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

        System.out.println("✅ UserDashboard initialisé pour: " + user.getNomComplet());
        System.out.println("🎮 Easter Egg activé! Tapez 'FARM' rapidement!");
    }

    @FXML
    private void ouvrirEspace() {
        // À personnaliser plus tard selon le rôle
        Utilisateur user = SessionManager.getInstance().getCurrentUser();
        String role = user != null ? user.getClass().getSimpleName() : "Inconnu";
        System.out.println("📂 Espace utilisateur ouvert pour rôle: " + role + " (fonctionnalité à implémenter)");

        // TODO: Implémenter selon le rôle
        // if (user instanceof Agriculteur) { ... }
        // if (user instanceof ResponsableExploitation) { ... }
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion de l'utilisateur...");
        SessionManager.getInstance().logout();
        navigateToPage("/fxml/Login.fxml", "FarmVision - Connexion");
    }

    /**
     * ✅ Navigation avec maintien du plein écran
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            System.out.println("🔄 Navigation vers: " + fxmlPath);

            Stage stage = (Stage) lblTitre.getScene().getWindow();

            // ✅ SAUVEGARDER L'ÉTAT ACTUEL
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            System.out.println("📐 État actuel: Width=" + currentWidth +
                    ", Height=" + currentHeight +
                    ", Maximized=" + isMaximized +
                    ", FullScreen=" + isFullScreen);

            // Charger la nouvelle page
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer la scène avec les bonnes dimensions
            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1000,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle(title);

            // ✅ RESTAURER L'ÉTAT
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

            System.out.println("✅ Navigation réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur navigation vers " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}