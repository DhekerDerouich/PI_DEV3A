package com.pi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // CORRECTION IMPORTANTE :
            // D'après votre capture d'écran, vos fichiers sont dans le dossier "tn/esprit/farmvision/..."
            // Même si votre package s'appelle "com.pi", le fichier physique est plus profond.

            // On essaie le chemin complet basé sur votre structure de dossiers
            String fxmlPath = "/tn/esprit/farmvision/com/pi/view/main.fxml";

            URL fxmlUrl = getClass().getResource(fxmlPath);

            // Si ça ne marche pas, on essaie une variante courante (au cas où "tn.esprit..." soit le root)
            if (fxmlUrl == null) {
                System.out.println("⚠️ Chemin complet non trouvé, essai du chemin court...");
                fxmlPath = "/com/pi/view/main.fxml";
                fxmlUrl = getClass().getResource(fxmlPath);
            }

            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR CRITIQUE : Impossible de trouver le fichier FXML !");
                System.err.println("Vérifiez le dossier 'target/classes' pour voir où Maven a copié le fichier.");
                return;
            }

            System.out.println("✅ Fichier FXML trouvé : " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 700);
            primaryStage.setTitle("FarmVision");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}