package tn.esprit.farmvision.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Marketplace;
import tn.esprit.farmvision.gestionstock.service.MarketplaceService;
import tn.esprit.farmvision.gestionstock.service.StockService;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.util.Validator;
import java.util.List;

public class AjouterMarketplaceController {

    @FXML private ComboBox<Stock> comboStock;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private MarketplaceService marketplaceService;
    private StockService stockService;

    @FXML
    public void initialize() {
        System.out.println("🔄 [AjouterMarketplaceController] Initialisation...");

        marketplaceService = new MarketplaceService();
        stockService = new StockService();

        // Charger les stocks disponibles
        chargerStocksDisponibles();

        // Validation prix (chiffres seulement)
        txtPrix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtPrix.setText(oldValue);
            }
        });

        // Validation quantité (chiffres seulement)
        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantite.setText(oldValue);
            }
        });

        // Formatage de l'affichage du ComboBox
        comboStock.setCellFactory(param -> new ListCell<Stock>() {
            @Override
            protected void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomProduit() + " - " + item.getQuantite() + " " + item.getUnite());
                }
            }
        });

        comboStock.setButtonCell(new ListCell<Stock>() {
            @Override
            protected void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomProduit() + " - " + item.getQuantite() + " " + item.getUnite());
                }
            }
        });

        System.out.println("✅ [AjouterMarketplaceController] Initialisé\n");
    }

    private void chargerStocksDisponibles() {
        try {
            List<Stock> stocks = stockService.getAllStocks();
            comboStock.getItems().clear();
            comboStock.getItems().addAll(stocks);
            if (!stocks.isEmpty()) {
                comboStock.setValue(stocks.get(0));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement stocks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        System.out.println("\n🟡 [AjouterMarketplaceController] Tentative d'ajout d'annonce...");

        if (!validerFormulaire()) {
            return;
        }

        try {
            Stock stockSelectionne = comboStock.getValue();

            Marketplace marketplace = new Marketplace(
                    stockSelectionne.getIdStock(),
                    Double.parseDouble(txtPrix.getText()),
                    Double.parseDouble(txtQuantite.getText()),
                    txtDescription.getText().trim()
            );

            System.out.println("📦 Annonce à ajouter:");
            System.out.println("   - Stock ID: " + marketplace.getIdStock());
            System.out.println("   - Produit: " + stockSelectionne.getNomProduit());
            System.out.println("   - Prix: " + marketplace.getPrixUnitaire() + " DT");
            System.out.println("   - Quantité: " + marketplace.getQuantiteEnVente());

            marketplaceService.ajouterMarketplace(marketplace);

            lblMessage.setText("✅ Annonce ajoutée avec succès! ID: " + marketplace.getIdMarketplace());
            lblMessage.setStyle("-fx-text-fill: green;");

            System.out.println("✅ Annonce ajoutée avec ID: " + marketplace.getIdMarketplace());

            // Fermer la fenêtre après 1.5 secondes
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                Stage stage = (Stage) txtPrix.getScene().getWindow();
                                stage.close();
                                System.out.println("✅ Fenêtre ajout fermée\n");
                            });
                        }
                    },
                    1500
            );

        } catch (Exception e) {
            lblMessage.setText("❌ Erreur: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
            System.err.println("❌ Erreur ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (comboStock.getValue() == null) {
            erreurs.append("Veuillez sélectionner un stock.\n");
        }

        if (txtPrix.getText().trim().isEmpty()) {
            erreurs.append("Le prix est obligatoire.\n");
        } else if (!Validator.isValidPrice(txtPrix.getText())) {
            erreurs.append("Le prix doit être un nombre positif.\n");
        }

        if (txtQuantite.getText().trim().isEmpty()) {
            erreurs.append("La quantité est obligatoire.\n");
        } else if (!Validator.isValidQuantity(txtQuantite.getText())) {
            erreurs.append("La quantité doit être un nombre positif.\n");
        }

        if (erreurs.length() > 0) {
            lblMessage.setText(erreurs.toString());
            lblMessage.setStyle("-fx-text-fill: red;");
            System.err.println("❌ Validation échouée:\n" + erreurs.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleAnnuler() {
        System.out.println("🟡 [AjouterMarketplaceController] Annulation - Fermeture fenêtre\n");
        Stage stage = (Stage) txtPrix.getScene().getWindow();
        stage.close();
    }
}