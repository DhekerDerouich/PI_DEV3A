package tn.esprit.farmvision.gestionuser.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.esprit.farmvision.SessionManager;
import javafx.scene.control.Button;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import javafx.scene.control.ComboBox;

import java.io.IOException;

public class UserDashboardController {

    @FXML private Label lblTitre, lblRole;
    @FXML private Button btnEspace;
    @FXML private BorderPane rootPane;

    @FXML
    private void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();

        if (user == null) {
            lblTitre.setText("❌ Session expirée");
            return;
        }

        String role = user.getClass().getSimpleName();
        String emoji = role.equals("Agriculteur") ? "👨‍🌾" :
                (role.equals("ResponsableExploitation") ? "👔" :
                        (role.equals("Administrateur") ? "⚙️" : "👤"));
        String roleDisplay = switch(role) {
            case "Administrateur" -> "Administrateur";
            case "ResponsableExploitation" -> "Responsable d'Exploitation";
            case "Agriculteur" -> "Agriculteur";
            default -> role;
        };
        lblRole.setText("Votre rôle : " + roleDisplay);
        btnEspace.setVisible(true);
        btnEspace.setManaged(true);
        switch(role) {
            case "Administrateur":
                btnEspace.setText("📊 Dashboard Admin");
                break;
            case "ResponsableExploitation":
                btnEspace.setText("🚜 Gestion des Équipements");
                break;
            case "Agriculteur":
                btnEspace.setText("🌾 Mon Espace Agricole");
                break;
            default:
                btnEspace.setText("📂 Mon Espace");
        }
        System.out.println("✅ Bouton espace visible pour: " + role);



        lblTitre.setText(emoji + " Bienvenue " + user.getNomComplet() + " !");

        if (role.equals("ResponsableExploitation")) {
            lblRole.setText("Vous avez accès au module de gestion des équipements");
        } else {
            lblRole.setText("Consultez vos données agricoles");
        }

        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event ->
                    AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
        }
    }
    private void navigateToModuleCOM() {
        try {
            String[] chemins = {
                    "/com/pi/view/main.fxml",
                    "/tn/esprit/farmvision/com/pi/view/main.fxml",
                    "/main.fxml"
            };

            FXMLLoader loader = null;
            for (String chemin : chemins) {
                java.net.URL url = getClass().getResource(chemin);
                if (url != null) {
                    loader = new FXMLLoader(url);
                    break;
                }
            }

            if (loader == null) {
                showInfo("Erreur", "Module COM non trouvé");
                return;
            }

            Parent root = loader.load();
            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("FarmVision - Gestion des Équipements");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Erreur", "Impossible d'ouvrir le module: " + e.getMessage());
        }
    }
    private void ouvrirModuleCOM() {
        try {
            System.out.println("🚜 Ouverture du module COM pour Responsable");

            String[] chemins = {
                    "/com/pi/view/main.fxml",
                    "/tn/esprit/farmvision/com/pi/view/main.fxml",
                    "/main.fxml"
            };

            FXMLLoader loader = null;
            for (String chemin : chemins) {
                var url = getClass().getResource(chemin);
                if (url != null) {
                    loader = new FXMLLoader(url);
                    System.out.println("✅ FXML trouvé: " + chemin);
                    break;
                }
            }

            if (loader == null) {
                showInfo("Erreur", "Module COM introuvable");
                return;
            }

            Parent root = loader.load();
            Stage stage = (Stage) btnEspace.getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean fullScreen = stage.isFullScreen();

            Scene scene = new Scene(root, width > 0 ? width : 1300, height > 0 ? height : 800);
            stage.setScene(scene);
            stage.setTitle("FarmVision - Gestion des Équipements");

            if (fullScreen) {
                stage.setFullScreen(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showInfo("Erreur", "Impossible d'ouvrir le module: " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirEspace() {
        var user = SessionManager.getInstance().getCurrentUser();

        if (user == null) {
            showInfo("Erreur", "Session expirée");
            return;
        }

        String role = user.getClass().getSimpleName();

        switch(role) {
            case "Administrateur":
                ouvrirDashboardAdmin();
                break;
            case "ResponsableExploitation":
                ouvrirModuleCOM();
                break;
            case "Agriculteur":
                ouvrirEspaceAgriculteur();
                break;
            default:
                showInfo("Info", "Espace non disponible pour ce rôle");
        }
    }
    private void ouvrirEspaceAgriculteur() {
        // TODO: Implémenter l'espace agriculteur
        showInfo("Espace Agriculteur",
                "🌾 Bienvenue dans votre espace agricole!\n\n" +
                        "Cette fonctionnalité sera disponible prochainement.\n" +
                        "Vous pourrez bientôt gérer vos parcelles, cultures et récoltes.");
    }
    private void ouvrirDashboardAdmin() {
        navigateToPage("/fxml/AdminDashboard.fxml", "FarmVision - Dashboard Admin");
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

    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
