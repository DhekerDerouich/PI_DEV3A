package com.pi.controller;

import com.pi.dao.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.net.URL;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatus;

    // Chemin de base d'après votre structure Maven
    private final String FXML_BASE_PATH = "/tn/esprit/farmvision/com/pi/view/";

    @FXML
    public void initialize() {
        if (DatabaseConnection.testConnection()) {
            connectionStatus.setText("✅ Connecté à la base");
            connectionStatus.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            connectionStatus.setText("❌ Non connecté");
            connectionStatus.setStyle("-fx-text-fill: #e74c3c;");
        }
        statusLabel.setText("Prêt");
    }

    /**
     * Récupère l'URL d'une ressource FXML avec fallback
     */
    private URL getFXMLResource(String fileName) {
        // Tentative avec le chemin complet (Maven)
        URL url = getClass().getResource(FXML_BASE_PATH + fileName);
        if (url == null) {
            // Tentative avec le chemin court
            url = getClass().getResource("/com/pi/view/" + fileName);
        }
        return url;
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
                showError("Fichier introuvable", "Le fichier " + fxmlFile + " n'existe pas dans les ressources.");
                return;
            }

            System.out.println("Chargement de : " + resource.toExternalForm());

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            statusLabel.setText(statusText);

        } catch (Exception e) {
            // Diagnostic amélioré : On cherche la cause profonde de l'erreur FXML
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }

            showError("Erreur de contenu FXML",
                    "Le fichier " + fxmlFile + " est trouvé mais contient une erreur :\n\n" +
                            "Détail : " + cause.getMessage());

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
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}