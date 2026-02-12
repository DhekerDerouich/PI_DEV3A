package tn.esprit.farmvision.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;
import tn.esprit.farmvision.gestionstock.util.Validator;
import java.time.LocalDate;

public class ModifierStockController {

    @FXML private Label lblIdStock;
    @FXML private Label lblIdUtilisateur;
    @FXML private TextField txtNomProduit;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUnite;
    @FXML private DatePicker dateExpiration;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label lblMessage;

    private StockService stockService;
    private Stock stockAModifier;

    @FXML
    public void initialize() {
        stockService = new StockService();

        // Catégories
        comboCategorie.getItems().addAll("Légumes", "Fruits", "Céréales", "Légumineuses",
                "Produits laitiers", "Viandes", "Volailles", "Œufs", "Autre");

        // Unités
        comboUnite.getItems().addAll("kg", "g", "litre", "ml", "unité", "pièce", "carton", "sac");

        // Statuts
        comboStatut.getItems().addAll("Disponible", "Épuisé", "Périmé", "Réservé");

        // Validation quantité (chiffres seulement)
        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantite.setText(oldValue);
            }
        });
    }

    public void setStock(Stock stock) {
        this.stockAModifier = stock;

        lblIdStock.setText(String.valueOf(stock.getIdStock()));
        lblIdUtilisateur.setText(String.valueOf(stock.getIdUtilisateur()));
        txtNomProduit.setText(stock.getNomProduit());
        comboCategorie.setValue(stock.getTypeProduit());     // ← typeProduit au lieu de categorie
        txtQuantite.setText(String.valueOf(stock.getQuantite())); // ← quantite au lieu de quantiteTotale
        comboUnite.setValue(stock.getUnite());
        dateExpiration.setValue(stock.getDateExpiration());
        comboStatut.setValue(stock.getStatut());
    }

    @FXML
    private void handleModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Mise à jour avec les nouveaux noms de méthodes
            stockAModifier.setNomProduit(txtNomProduit.getText().trim());
            stockAModifier.setTypeProduit(comboCategorie.getValue());     // ← setTypeProduit
            stockAModifier.setQuantite(Double.parseDouble(txtQuantite.getText())); // ← setQuantite
            stockAModifier.setUnite(comboUnite.getValue());
            stockAModifier.setDateExpiration(dateExpiration.getValue());
            stockAModifier.setStatut(comboStatut.getValue());

            stockService.modifierStock(stockAModifier);

            lblMessage.setText("✅ Stock modifié avec succès!");
            lblMessage.setStyle("-fx-text-fill: green;");

            // Fermer la fenêtre après 1.5 secondes
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> handleFermer());
                        }
                    },
                    1500
            );

        } catch (Exception e) {
            lblMessage.setText("❌ Erreur: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        // Validation nom produit
        if (txtNomProduit.getText().trim().isEmpty()) {
            erreurs.append("Le nom du produit est obligatoire.\n");
        } else if (!Validator.isValidString(txtNomProduit.getText(), 2, 100)) {
            erreurs.append("Le nom du produit doit contenir entre 2 et 100 caractères.\n");
        }

        // Validation quantité
        if (txtQuantite.getText().trim().isEmpty()) {
            erreurs.append("La quantité est obligatoire.\n");
        } else if (!Validator.isValidQuantity(txtQuantite.getText())) {
            erreurs.append("La quantité doit être un nombre positif.\n");
        }

        // Validation date expiration
        if (dateExpiration.getValue() == null) {
            erreurs.append("La date d'expiration est obligatoire.\n");
        } else if (dateExpiration.getValue().isBefore(LocalDate.now())) {
            erreurs.append("La date d'expiration doit être dans le futur.\n");
        }

        if (erreurs.length() > 0) {
            lblMessage.setText(erreurs.toString());
            lblMessage.setStyle("-fx-text-fill: red;");
            return false;
        }

        return true;
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) txtNomProduit.getScene().getWindow();
        stage.close();
    }
}