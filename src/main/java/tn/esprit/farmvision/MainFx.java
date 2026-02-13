package tn.esprit.farmvision;  // ← ton package racine correct

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charge ton FXML principal (exemple : Login.fxml)
            // Change le nom du FXML ici si tu as créé un autre (ex. MainDashboard.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));

            // Vérification rapide si le FXML existe vraiment
            if (loader.getLocation() == null) {
                throw new IOException("FXML non trouvé : /fxml/Login.fxml\n" +
                        "Vérifiez que le fichier est dans src/main/resources/fxml/Login.fxml");
            }

            Scene scene = new Scene(loader.load(), 900, 650); // taille confortable pour login + dashboard

            // Fond beige clair global (comme dans ton projet gestion_feedback)
            scene.getRoot().setStyle("-fx-background-color: #f5f1ee;");

            primaryStage.setTitle("FarmVision - Authentification");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true); // permet de redimensionner
            primaryStage.centerOnScreen(); // centre la fenêtre
            primaryStage.show();

            System.out.println("FarmVision lancée avec succès ! FXML chargé : Login.fxml");

        } catch (IOException e) {
            e.printStackTrace();
            // Alerte graphique si erreur (très utile pour le prof)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de lancement");
            alert.setHeaderText("Impossible de charger l'interface");
            alert.setContentText("Détails : " + e.getMessage() + "\n\n" +
                    "Vérifiez :\n" +
                    "1. Le fichier FXML existe dans src/main/resources/fxml/\n" +
                    "2. Le chemin dans getResource est correct\n" +
                    "3. pom.xml a bien javafx-fxml et javafx-controls");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}