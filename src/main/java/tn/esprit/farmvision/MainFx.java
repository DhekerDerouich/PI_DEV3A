package tn.esprit.farmvision;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;

import java.io.IOException;

/**
 * 🚀 Application principale FarmVision
 * ✅ NOUVELLE FONCTIONNALITÉ : Reconnexion automatique
 * Si une session existe, l'utilisateur est redirigé vers son dashboard
 */
public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🌾 Démarrage FarmVision...");

            // ✅ VÉRIFIER SI UNE SESSION EXISTE
            SessionManager sessionManager = SessionManager.getInstance();
            boolean sessionRestored = sessionManager.restoreSessionFromFile();

            Parent root;
            String title;

            if (sessionRestored) {
                // ✅ SESSION TROUVÉE - Redirection directe vers le dashboard
                Utilisateur user = sessionManager.getCurrentUser();
                System.out.println("✅ Reconnexion automatique : " + user.getNomComplet());

                if (user instanceof Administrateur) {
                    root = FXMLLoader.load(getClass().getResource("/fxml/AdminDashboard.fxml"));
                    title = "FarmVision - Dashboard Administrateur";
                } else {
                    root = FXMLLoader.load(getClass().getResource("/fxml/UserDashboard.fxml"));
                    title = "FarmVision - Dashboard Utilisateur";
                }

            } else {
                // ❌ PAS DE SESSION - Afficher le login
                System.out.println("ℹ️ Aucune session - Affichage du login");
                root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                title = "FarmVision - Connexion";
            }

            // Créer la scène
            Scene scene = new Scene(root, 1000, 700);

            // Configuration de la fenêtre
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();

            // Maximiser si c'était un dashboard
            if (sessionRestored) {
                primaryStage.setMaximized(true);
            }

            primaryStage.show();

            System.out.println("✅ FarmVision lancée avec succès !");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de lancement",
                    "Impossible de charger l'interface",
                    e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur inattendue",
                    "Une erreur s'est produite",
                    e.getMessage());
        }
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content + "\n\nVérifiez :\n" +
                "1. Les fichiers FXML dans src/main/resources/fxml/\n" +
                "2. Les chemins dans getResource()\n" +
                "3. Les dépendances JavaFX dans pom.xml");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}