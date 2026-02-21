package com.pi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Démarrage de FarmVision...");

            // Essayer différents chemins possibles pour le fichier FXML
            URL fxmlUrl = null;
            String[] chemins = {
                    "/com/pi/view/main.fxml",
                    "/tn/esprit/farmvision/com/pi/view/main.fxml",
                    "/main.fxml"
            };

            for (String chemin : chemins) {
                fxmlUrl = getClass().getResource(chemin);
                if (fxmlUrl != null) {
                    System.out.println("✅ FXML trouvé: " + chemin);
                    break;
                }
            }

            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR: Impossible de trouver main.fxml");
                System.err.println("Vérifiez que le fichier est dans src/main/resources/com/pi/view/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1300, 800);

            // ============================================
            // CHARGEMENT CSS - méthode robuste multi-chemins
            // ============================================
            String[] cssPaths = {
                    "/com/pi/view/styles.css",
                    "/tn/esprit/farmvision/com/pi/view/styles.css",
                    "/styles.css"
            };

            boolean cssLoaded = false;
            for (String cssPath : cssPaths) {
                URL cssUrl = getClass().getResource(cssPath);
                if (cssUrl != null) {
                    scene.getStylesheets().clear();
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                    System.out.println("✅ CSS chargé depuis: " + cssPath);
                    cssLoaded = true;
                    break;
                }
            }

            if (!cssLoaded) {
                System.err.println("⚠️ CSS non trouvé - vérifiez que styles.css est dans src/main/resources/com/pi/view/");
            }

            primaryStage.setTitle("FarmVision - Gestion Agricole Intelligente");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Application démarrée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur au démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}