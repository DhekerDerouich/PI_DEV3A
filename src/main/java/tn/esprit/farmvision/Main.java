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

            // Charger directement la page de stocks
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestion_stock.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 800);

            String css = getClass().getResource("/css/style.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
                System.out.println("✅ CSS chargé");
            }

            primaryStage.setTitle("FarmVision - Gestion Agricole");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
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