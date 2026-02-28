package tn.esprit.farmvision.gestionstock.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;
import tn.esprit.farmvision.gestionstock.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AjouterStockController {

    @FXML private TextField txtIdUtilisateur;  // Gardé mais caché dans le FXML
    @FXML private TextField txtNomProduit;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUnite;
    @FXML private DatePicker dateExpiration;
    @FXML private Label lblMessage;

    // Nouveau composant pour les suggestions
    @FXML private ListView<String> suggestionList;
    @FXML private Label lblSuggestionHint;

    private StockService stockService;
    private List<String> produitsExistants;
    private ObservableList<String> suggestions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("🔄 [AjouterStockController] Initialisation...");

        stockService = new StockService();

        // Charger tous les produits existants pour les suggestions
        chargerProduitsExistants();

        comboCategorie.getItems().addAll("Légumes", "Fruits", "Céréales", "Légumineuses",
                "Produits laitiers", "Viandes", "Volailles", "Œufs", "Autre");
        comboCategorie.setValue("Légumes");

        comboUnite.getItems().addAll("kg", "g", "litre", "ml", "unité", "pièce", "carton", "sac");
        comboUnite.setValue("kg");

        dateExpiration.setValue(LocalDate.now().plusDays(7));

        // ID Utilisateur par défaut (1) - caché dans l'interface
        if (txtIdUtilisateur != null) {
            txtIdUtilisateur.setText("1");
        }

        // Validation quantité (nombre décimal)
        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantite.setText(oldValue);
            }
        });

        // SUGGESTIONS : Détection de la frappe dans le champ produit
        txtNomProduit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() >= 3) {
                suggererProduits(newValue);
                suggestionList.setVisible(true);
                lblSuggestionHint.setVisible(true);
            } else {
                suggestionList.setVisible(false);
                lblSuggestionHint.setVisible(false);
            }
        });

        // Gérer le clic sur une suggestion
        suggestionList.setOnMouseClicked(event -> {
            String selected = suggestionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                txtNomProduit.setText(selected);
                suggestionList.setVisible(false);
                lblSuggestionHint.setVisible(false);
            }
        });

        // Cacher les suggestions quand on perd le focus
        txtNomProduit.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                new Thread(() -> {
                    try { Thread.sleep(200); } catch (InterruptedException e) {}
                    javafx.application.Platform.runLater(() -> {
                        suggestionList.setVisible(false);
                        lblSuggestionHint.setVisible(false);
                    });
                }).start();
            }
        });

        System.out.println("✅ [AjouterStockController] Initialisé\n");
    }

    private void chargerProduitsExistants() {
        try {
            List<Stock> stocks = stockService.getAllStocks();
            produitsExistants = stocks.stream()
                    .map(Stock::getNomProduit)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            System.out.println("📋 " + produitsExistants.size() + " produits existants chargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement produits: " + e.getMessage());
            produitsExistants = List.of();
        }
    }

    private void suggererProduits(String debut) {
        if (produitsExistants == null || debut == null || debut.trim().isEmpty()) {
            suggestions.clear();
            suggestionList.setItems(suggestions);
            return;
        }

        String debutLower = debut.toLowerCase().trim();

        List<String> filtered = produitsExistants.stream()
                .filter(produit -> produit != null &&
                        produit.toLowerCase().startsWith(debutLower))
                .limit(10)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            filtered = produitsExistants.stream()
                    .filter(produit -> produit != null &&
                            produit.toLowerCase().contains(debutLower))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        suggestions.setAll(filtered);
        suggestionList.setItems(suggestions);

        if (!filtered.isEmpty()) {
            double itemHeight = 24;
            double maxHeight = 150;
            double newHeight = Math.min(filtered.size() * itemHeight, maxHeight);
            suggestionList.setPrefHeight(newHeight);
        }
    }

    @FXML
    private void handleAjouter() {
        System.out.println("\n🟡 [AjouterStockController] Tentative d'ajout...");

        if (!validerFormulaire()) {
            return;
        }

        try {
            // Utiliser l'ID utilisateur par défaut (1) ou celui du champ s'il existe
            int idUtilisateur = 1;
            if (txtIdUtilisateur != null && !txtIdUtilisateur.getText().isEmpty()) {
                idUtilisateur = Integer.parseInt(txtIdUtilisateur.getText());
            }

            // Utiliser le constructeur existant avec 6 paramètres
            Stock stock = new Stock(
                    idUtilisateur,
                    txtNomProduit.getText().trim(),
                    comboCategorie.getValue(),
                    Double.parseDouble(txtQuantite.getText()),
                    comboUnite.getValue(),
                    dateExpiration.getValue()
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

        } catch (NumberFormatException e) {
            lblMessage.setText("❌ Erreur: Format de nombre invalide");
            lblMessage.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            lblMessage.setText("❌ Erreur: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
            System.err.println("❌ Erreur ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

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