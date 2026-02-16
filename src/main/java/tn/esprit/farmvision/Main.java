package tn.esprit.farmvision;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("=== DÉMARRAGE FARMVISION ===");

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_stock.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root);

            // Charger le CSS
            String css = getClass().getResource("/css/style.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
                System.out.println("✅ CSS chargé");
            }

            primaryStage.setTitle("FarmVision - Gestion de Stock");
            primaryStage.setScene(scene);

            // Centrer la fenêtre sur l'écran
            primaryStage.centerOnScreen();

            // Optionnel: Plein écran centré
            // primaryStage.setFullScreen(true);

            primaryStage.show();

            System.out.println("✅ Application démarrée et centrée");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}