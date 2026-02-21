package com.pi.controller;

import com.pi.dao.DatabaseConnection;
import com.pi.service.AlertesService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.net.URL;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatus;
    @FXML private Label alerteCount;  // Vérifiez que ce champ existe
    @FXML private Button alertesButton;

    // Chemins possibles pour les fichiers FXML
    private final String[] FXML_PATHS = {
            "/com/pi/view/",
            "/tn/esprit/farmvision/com/pi/view/",
            "/"
    };

    @FXML
    public void initialize() {
        try {
            // Vérifier que tous les champs FXML sont injectés
            System.out.println("=== Initialisation du MainController ===");
            System.out.println("contentPane: " + (contentPane != null ? "✅" : "❌"));
            System.out.println("statusLabel: " + (statusLabel != null ? "✅" : "❌"));
            System.out.println("connectionStatus: " + (connectionStatus != null ? "✅" : "❌"));
            System.out.println("alerteCount: " + (alerteCount != null ? "✅" : "❌"));
            System.out.println("alertesButton: " + (alertesButton != null ? "✅" : "❌"));

            if (alerteCount == null) {
                System.err.println("⚠️ ATTENTION: alerteCount est null dans MainController");
                System.err.println("Vérifiez que le fx:id='alerteCount' est bien défini dans main.fxml");
                // Créer un label par défaut pour éviter NullPointerException
                alerteCount = new Label("0");
            }

            // Vérifier la connexion à la base de données
            if (DatabaseConnection.testConnection()) {
                connectionStatus.setText("✅ Connecté à la base");
                connectionStatus.setStyle("-fx-text-fill: #2ecc71;");
            } else {
                connectionStatus.setText("❌ Non connecté");
                connectionStatus.setStyle("-fx-text-fill: #e74c3c;");
            }

            statusLabel.setText("Prêt");

            // Mettre à jour le compteur d'alertes
            mettreAJourCompteurAlertes();

        } catch (Exception e) {
            System.err.println("❌ Erreur dans initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère l'URL d'une ressource FXML
     */
    private URL getFXMLResource(String fileName) {
        for (String path : FXML_PATHS) {
            URL url = getClass().getResource(path + fileName);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    @FXML
    private void openAlertes() {
        try {
            URL url = getFXMLResource("AlertesView.fxml");
            if (url == null) {
                showError("Erreur", "Fichier AlertesView.fxml introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("🔔 Centre de notifications intelligentes");
            stage.setScene(new Scene(root, 500, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

            // Mettre à jour le compteur après fermeture
            stage.setOnHidden(e -> mettreAJourCompteurAlertes());

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir les alertes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openCalendrier() {
        try {
            URL url = getFXMLResource("CalendrierMaintenance.fxml");
            if (url == null) {
                showError("Erreur", "Fichier CalendrierMaintenance.fxml introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Calendrier interactif des maintenances");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showEquipementView() {
        loadView("equipement.fxml", "Gestion des équipements");
    }

    @FXML
    private void showMaintenanceView() {
        loadView("maintenance.fxml", "Gestion des maintenances");
    }

    private void loadView(String fxmlFile, String statusText) {
        try {
            URL resource = getFXMLResource(fxmlFile);

            if (resource == null) {
                showError("Fichier introuvable", "Le fichier " + fxmlFile + " n'existe pas.");
                return;
            }

            System.out.println("Chargement de : " + resource.toExternalForm());

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            statusLabel.setText(statusText);

        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            showError("Erreur de chargement",
                    "Erreur lors du chargement de " + fxmlFile + ":\n" + cause.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showAddEquipementDialog() {
        try {
            URL resource = getFXMLResource("equipement.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            EquipementController controller = loader.getController();
            controller.showAddDialog();

            // Rafraîchir la vue après ajout
            showEquipementView();

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showAddMaintenanceDialog() {
        try {
            URL resource = getFXMLResource("maintenance.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            MaintenanceController controller = loader.getController();
            controller.showAddDialog();

            // Rafraîchir la vue après ajout
            showMaintenanceView();

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        // Retour à l'écran d'accueil
        contentPane.getChildren().clear();
        statusLabel.setText("Accueil");
    }

    @FXML
    private void handleQuit() {
        DatabaseConnection.closeConnection();
        System.exit(0);
    }

    private void mettreAJourCompteurAlertes() {
        try {
            if (alerteCount == null) {
                System.err.println("⚠️ alerteCount est null, impossible de mettre à jour");
                return;
            }

            AlertesService alertesService = new AlertesService();
            int nbAlertes = alertesService.getToutesLesAlertes().size();
            alerteCount.setText(String.valueOf(nbAlertes));

            if (nbAlertes > 0) {
                alerteCount.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
                if (alertesButton != null) {
                    alertesButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-background-radius: 20;");
                }
            } else {
                alerteCount.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
                if (alertesButton != null) {
                    alertesButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-background-radius: 20;");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur dans mettreAJourCompteurAlertes: " + e.getMessage());
            if (alerteCount != null) {
                alerteCount.setText("0");
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}