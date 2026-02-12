package tn.esprit.farmvision.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class MainStockController {

    @FXML
    private Button btnGestionStock;

    @FXML
    private Button btnMarketplace;

    @FXML
    private void handleGestionStock() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestion_stock.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des Stocks");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
            ((Stage) btnGestionStock.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMarketplace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestion_marketplace.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("FarmVision - Marketplace");
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();
            ((Stage) btnMarketplace.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        // Fermer simplement la fenêtre
        ((Stage) btnGestionStock.getScene().getWindow()).close();
    }
}