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

/**
 * 🌾 Dashboard Administrateur FarmVision
 * ✅ Maintien du plein écran lors de la navigation
 * 🎮 Easter egg activé!
 */
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

        System.out.println("✅ AdminDashboard initialisé");
        System.out.println("🎮 Easter Egg activé! Tapez 'FARM' rapidement!");
    }

    @FXML
    private void ouvrirGestionUsers() {
        navigateToPage("/fxml/GestionUsers.fxml", "FarmVision - Gestion des Utilisateurs");
    }

    @FXML
    private void validerComptes() {
        ouvrirGestionUsers(); // Même écran pour valider
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion de l'administrateur...");
        SessionManager.getInstance().logout();
        navigateToPage("/fxml/Login.fxml", "FarmVision - Connexion");
    }

    /**
     * ✅ Navigation avec maintien du plein écran
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            System.out.println("🔄 Navigation vers: " + fxmlPath);

            Stage stage = (Stage) lblWelcome.getScene().getWindow();

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
                    currentWidth > 0 ? currentWidth : 1200,
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