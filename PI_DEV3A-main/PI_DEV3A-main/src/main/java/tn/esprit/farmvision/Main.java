package tn.esprit.farmvision;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
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
            System.out.println("📂 Recherche des fichiers FXML...");

            SessionManager.WindowState windowState = SessionManager.getInstance().restoreWindowState();

            if (windowState != null) {
                primaryStage.setX(windowState.x);
                primaryStage.setY(windowState.y);
                primaryStage.setWidth(windowState.width);
                primaryStage.setHeight(windowState.height);

                if (windowState.fullscreen) {
                    primaryStage.setFullScreen(true);
                } else if (windowState.maximized) {
                    primaryStage.setMaximized(true);
                }
                System.out.println("🔄 État fenêtre restauré");
            } else {
                primaryStage.setWidth(1300);
                primaryStage.setHeight(800);
                primaryStage.centerOnScreen();
            }

            if (SessionManager.getInstance().restoreSessionFromFile()) {
                Utilisateur user = SessionManager.getInstance().getCurrentUser();
                System.out.println("✅ Session restaurée: " + user.getNomComplet() + " (" + user.getClass().getSimpleName() + ")");
                redirectToAppropriateDashboard(primaryStage, user);
                return;
            }

            URL loginUrl = findFXML("Login.fxml");
            System.out.println("📄 CHEMIN EXACT CHARGÉ: " + loginUrl);

            if (loginUrl == null) {
                System.err.println("❌ ERREUR: Impossible de trouver Login.fxml");
                return;
            }

            System.out.println("✅ Login FXML trouvé: " + loginUrl);

            FXMLLoader loader = new FXMLLoader(loginUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
            loadCSS(scene);

            primaryStage.setTitle("FarmVision - Connexion");
            primaryStage.setScene(scene);
            setupWindowListeners(primaryStage);
            primaryStage.show();

            System.out.println("✅ Application démarrée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur au démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupWindowListeners(Stage stage) {
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized() && !stage.isFullScreen()) {
                SessionManager.getInstance().saveWindowState(
                        stage.getWidth(), stage.getHeight(),
                        stage.isMaximized(), stage.isFullScreen(),
                        stage.getX(), stage.getY()
                );
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized() && !stage.isFullScreen()) {
                SessionManager.getInstance().saveWindowState(
                        stage.getWidth(), stage.getHeight(),
                        stage.isMaximized(), stage.isFullScreen(),
                        stage.getX(), stage.getY()
                );
            }
        });

        stage.xProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized() && !stage.isFullScreen()) {
                SessionManager.getInstance().saveWindowState(
                        stage.getWidth(), stage.getHeight(),
                        stage.isMaximized(), stage.isFullScreen(),
                        stage.getX(), stage.getY()
                );
            }
        });

        stage.yProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.isShowing() && !stage.isMaximized() && !stage.isFullScreen()) {
                SessionManager.getInstance().saveWindowState(
                        stage.getWidth(), stage.getHeight(),
                        stage.isMaximized(), stage.isFullScreen(),
                        stage.getX(), stage.getY()
                );
            }
        });

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            SessionManager.getInstance().saveWindowState(
                    stage.getWidth(), stage.getHeight(),
                    newVal, stage.isFullScreen(),
                    stage.getX(), stage.getY()
            );
        });

        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            SessionManager.getInstance().saveWindowState(
                    stage.getWidth(), stage.getHeight(),
                    stage.isMaximized(), newVal,
                    stage.getX(), stage.getY()
            );
        });
    }

    /**
     * 🔍 Recherche un fichier FXML dans tous les chemins possibles
     */
    private URL findFXML(String fileName) {
        String[] searchPaths = {
                // Module COM
                "/com/pi/view/" + fileName,
                "/tn/esprit/farmvision/com/pi/view/" + fileName,

                // Module gestionuser
                "/fxml/" + fileName,
                "/tn/esprit/farmvision/gestionuser/view/" + fileName,

                // Module Finance
                "/finance/view/" + fileName,
                "/tn/esprit/farmvision/finance/view/" + fileName,

                // ✅ Module Gestion Stock
                "/gestionstock/" + fileName,
                "/tn/esprit/farmvision/gestionstock/" + fileName,

                // Racine
                "/" + fileName
        };

        for (String path : searchPaths) {
            URL url = getClass().getResource(path);
            if (url != null) {
                System.out.println("  ✅ Trouvé: " + path);
                return url;
            }
        }

        System.out.println("  ❌ Non trouvé: " + fileName);
        return null;
    }

    /**
     * 🎨 Charge le CSS depuis tous les chemins possibles
     */
    private void loadCSS(Scene scene) {
        String[] cssPaths = {
                "/fxml/styles.css",
                "/com/pi/view/styles.css",
                "/tn/esprit/farmvision/com/pi/view/styles.css",
                "/tn/esprit/farmvision/gestionuser/view/styles.css",
                "/css/styles.css",
                "/gestionstock/style.css",
                "/style.css",
                "/styles.css"
        };

        boolean cssLoaded = false;
        for (String cssPath : cssPaths) {
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("✅ CSS chargé depuis: " + cssPath);
                cssLoaded = true;
                break;
            }
        }

        if (!cssLoaded) {
            System.err.println("⚠️ CSS non trouvé");
        }
    }

    /**
     * 🔀 Rediriger vers le dashboard approprié selon le rôle
     */
    private void redirectToAppropriateDashboard(Stage stage, Utilisateur user) throws Exception {
        String fxmlPath;
        String title;
        String role = user.getClass().getSimpleName();

        switch (role) {
            case "Administrateur":
                fxmlPath = "AdminDashboard.fxml";
                title = "FarmVision - Dashboard Admin";
                break;

            case "ResponsableExploitation":
                fxmlPath = "main.fxml";
                title = "FarmVision - Gestion des Équipements";
                break;

            case "Agriculteur":
                fxmlPath = "UserDashboard.fxml";
                title = "FarmVision - Espace Agriculteur";
                break;

            default:
                fxmlPath = "UserDashboard.fxml";
                title = "FarmVision - Dashboard";
        }

        System.out.println("🔄 Redirection vers: " + fxmlPath + " pour rôle: " + role);

        URL fxmlUrl = findFXML(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("❌ FXML non trouvé: " + fxmlPath);
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        loadCSS(scene);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}