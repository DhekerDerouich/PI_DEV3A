package tn.esprit.farmvision.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;
import tn.esprit.farmvision.gestionstock.util.Validator;
import java.time.LocalDate;

public class AjouterStockController {

    @FXML private TextField txtIdUtilisateur;
    @FXML private TextField txtNomProduit;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUnite;
    @FXML private DatePicker dateExpiration;
    @FXML private Label lblMessage;

    private StockService stockService;

    @FXML
    public void initialize() {
        System.out.println("🔄 [AjouterStockController] Initialisation...");

        stockService = new StockService();

        comboCategorie.getItems().addAll("Légumes", "Fruits", "Céréales", "Légumineuses",
                "Produits laitiers", "Viandes", "Volailles", "Œufs", "Autre");
        comboCategorie.setValue("Légumes");

        comboUnite.getItems().addAll("kg", "g", "litre", "ml", "unité", "pièce", "carton", "sac");
        comboUnite.setValue("kg");

        dateExpiration.setValue(LocalDate.now().plusDays(7));

        txtIdUtilisateur.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtIdUtilisateur.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantite.setText(oldValue);
            }
        });

        System.out.println("✅ [AjouterStockController] Initialisé\n");
    }

    @FXML
    private void handleAjouter() {
        System.out.println("\n🟡 [AjouterStockController] Tentative d'ajout...");

        if (!validerFormulaire()) {
            return;
        }

        try {
            // ✅ CONSTRUCTEUR AVEC 6 PARAMÈTRES (dateExpiration incluse)
            Stock stock = new Stock(
                    Integer.parseInt(txtIdUtilisateur.getText()),
                    txtNomProduit.getText().trim(),
                    comboCategorie.getValue(),
                    Double.parseDouble(txtQuantite.getText()),
                    comboUnite.getValue(),
                    dateExpiration.getValue()  // ← 6ème paramètre OBLIGATOIRE !
            );

            System.out.println("📦 Stock à ajouter:");
            System.out.println("   - Utilisateur ID: " + stock.getIdUtilisateur());
            System.out.println("   - Produit: " + stock.getNomProduit());
            System.out.println("   - Type: " + stock.getTypeProduit());
            System.out.println("   - Quantité: " + stock.getQuantite() + " " + stock.getUnite());
            System.out.println("   - Date expiration: " + stock.getDateExpiration());

            stockService.ajouterStock(stock);

            lblMessage.setText("✅ Stock ajouté avec succès! ID: " + stock.getIdStock());
            lblMessage.setStyle("-fx-text-fill: green;");

            System.out.println("✅ Stock ajouté avec ID: " + stock.getIdStock());

            // Fermer la fenêtre après 1.5 secondes
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                Stage stage = (Stage) txtNomProduit.getScene().getWindow();
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

        if (txtIdUtilisateur.getText().trim().isEmpty()) {
            erreurs.append("L'ID utilisateur est obligatoire.\n");
        } else if (!Validator.isValidInteger(txtIdUtilisateur.getText(), 1, 999999)) {
            erreurs.append("ID utilisateur invalide.\n");
        }

        if (txtNomProduit.getText().trim().isEmpty()) {
            erreurs.append("Le nom du produit est obligatoire.\n");
        } else if (!Validator.isValidString(txtNomProduit.getText(), 2, 100)) {
            erreurs.append("Le nom du produit doit contenir entre 2 et 100 caractères.\n");
        }

        if (txtQuantite.getText().trim().isEmpty()) {
            erreurs.append("La quantité est obligatoire.\n");
        } else if (!Validator.isValidQuantity(txtQuantite.getText())) {
            erreurs.append("La quantité doit être un nombre positif.\n");
        }

        if (dateExpiration.getValue() == null) {
            erreurs.append("La date d'expiration est obligatoire.\n");
        } else if (dateExpiration.getValue().isBefore(LocalDate.now())) {
            erreurs.append("La date d'expiration doit être dans le futur.\n");
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
        System.out.println("🟡 [AjouterStockController] Annulation - Fermeture fenêtre\n");
        Stage stage = (Stage) txtNomProduit.getScene().getWindow();
        stage.close();
    }
}