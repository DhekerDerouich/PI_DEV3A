package tn.esprit.farmvision;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("=== DÉMARRAGE FARMVISION ===");

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_stock.fxml"));
            Parent root = loader.load();

            // Ajouter la classe CSS pour la fenêtre principale
            root.getStyleClass().add("main-window");

            // Créer la scène avec les dimensions souhaitées
            Scene scene = new Scene(root, 700, 500);

            // Charger le CSS
            String css = getClass().getResource("/css/style.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
                System.out.println("✅ CSS chargé: " + css);
            } else {
                System.err.println("❌ CSS non trouvé!");
            }

            // Configurer la fenêtre
            primaryStage.setTitle("FarmVision - Gestion de Stock");
            primaryStage.setScene(scene);

            // Taille minimale
            primaryStage.setMinWidth(700);
            primaryStage.setMinHeight(500);

            // Empêcher le redimensionnement (optionnel)
            // primaryStage.setResizable(false);

            primaryStage.show();

            System.out.println("✅ Application démarrée");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}