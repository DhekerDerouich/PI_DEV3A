package tn.esprit.farmvision.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Marketplace;
import tn.esprit.farmvision.gestionstock.service.MarketplaceService;
import tn.esprit.farmvision.gestionstock.util.Validator;

public class ModifierMarketplaceController {

    @FXML private Label lblId;
    @FXML private Label lblProduit;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private MarketplaceService marketplaceService;
    private Marketplace annonceAModifier;

    @FXML
    public void initialize() {
        marketplaceService = new MarketplaceService();

        comboStatut.getItems().addAll("En vente", "Réservé", "Vendu");

        txtPrix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtPrix.setText(oldValue);
            }
        });

        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantite.setText(oldValue);
            }
        });
    }

    public void setMarketplace(Marketplace annonce) {
        this.annonceAModifier = annonce;

        lblId.setText(String.valueOf(annonce.getIdMarketplace()));
        lblProduit.setText(annonce.getNomProduit());
        txtPrix.setText(String.valueOf(annonce.getPrixUnitaire()));
        txtQuantite.setText(String.valueOf(annonce.getQuantiteEnVente()));
        comboStatut.setValue(annonce.getStatut());
        txtDescription.setText(annonce.getDescription());
    }

    @FXML
    private void handleModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            annonceAModifier.setPrixUnitaire(Double.parseDouble(txtPrix.getText()));
            annonceAModifier.setQuantiteEnVente(Double.parseDouble(txtQuantite.getText()));
            annonceAModifier.setStatut(comboStatut.getValue());
            annonceAModifier.setDescription(txtDescription.getText().trim());

            marketplaceService.modifierMarketplace(annonceAModifier);

            lblMessage.setText("✅ Annonce modifiée avec succès!");
            lblMessage.setStyle("-fx-text-fill: green;");

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
            return false;
        }

        return true;
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) txtPrix.getScene().getWindow();
        stage.close();
    }
}