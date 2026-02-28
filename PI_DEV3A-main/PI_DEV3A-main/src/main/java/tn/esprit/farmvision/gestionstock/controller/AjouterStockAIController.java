package tn.esprit.farmvision.gestionstock.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;

import java.io.File;
import java.time.LocalDate;

public class AjouterStockAIController {

    @FXML private TextField txtIdUtilisateur;
    @FXML private TextField txtNomProduit;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private ComboBox<String> comboUnite;
    @FXML private TextField txtQuantite;
    @FXML private DatePicker dateExpiration;
    @FXML private Label lblMessage;
    @FXML private ImageView imagePreview;
    @FXML private Label lblAiConfidence;
    @FXML private ProgressBar aiConfidenceBar;
    @FXML private Button btnScanPhoto;
    @FXML private Label lblSuggestion;
    @FXML private Label lblNoImage;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;

    private File selectedImageFile;
    private StockService stockService;

    @FXML
    public void initialize() {
        System.out.println("=== INITIALISATION CONTROLLEUR IA STOCK ===");

        stockService = new StockService();

        // Initialiser les combos
        comboCategorie.getItems().addAll("Légumes", "Fruits", "Céréales",
                "Légumineuses", "Produits laitiers", "Viandes", "Volailles", "Œufs", "Autre");
        comboUnite.getItems().addAll("kg", "g", "L", "ml", "pièce", "douzaine", "sac", "botte");

        // Valeurs par défaut
        comboUnite.setValue("kg");
        txtIdUtilisateur.setText("1");
        txtQuantite.setText("1");

        // Message par défaut
        lblAiConfidence.setText("En attente de scan...");
        aiConfidenceBar.setProgress(0);

        // Rendre le label "Aucune image" visible au début
        if (lblNoImage != null) {
            lblNoImage.setVisible(true);
        }

        // Vérifier que les boutons sont bien initialisés
        System.out.println("✅ Bouton Ajouter: " + (btnAjouter != null ? "OK" : "NULL"));
        System.out.println("✅ Bouton Annuler: " + (btnAnnuler != null ? "OK" : "NULL"));
        System.out.println("✅ Initialisation terminée");
    }

    @FXML
    private void handleScanPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo du produit");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) btnScanPhoto.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            try {
                // Afficher l'image
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);

                // Masquer le label "Aucune image"
                if (lblNoImage != null) {
                    lblNoImage.setVisible(false);
                }

                lblMessage.setText("🔍 Analyse de l'image en cours...");
                lblMessage.setStyle("-fx-text-fill: #2196F3;");

                // Simuler une reconnaissance
                simulateRecognition(selectedImageFile.getName());

            } catch (Exception e) {
                lblMessage.setText("❌ Erreur chargement image: " + e.getMessage());
                lblMessage.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    private void simulateRecognition(String fileName) {
        String fileNameLower = fileName.toLowerCase();

        // Simuler la reconnaissance basée sur le nom du fichier
        if (fileNameLower.contains("tomate")) {
            txtNomProduit.setText("Tomate");
            comboCategorie.setValue("Légumes");
            lblAiConfidence.setText("Confiance: 85%");
            aiConfidenceBar.setProgress(0.85);
            lblSuggestion.setText("✓ Tomate détectée");
            lblSuggestion.setStyle("-fx-text-fill: #4CAF50;");
        }
        else if (fileNameLower.contains("pomme")) {
            txtNomProduit.setText("Pomme");
            comboCategorie.setValue("Fruits");
            lblAiConfidence.setText("Confiance: 90%");
            aiConfidenceBar.setProgress(0.90);
            lblSuggestion.setText("✓ Pomme détectée");
            lblSuggestion.setStyle("-fx-text-fill: #4CAF50;");
        }
        else if (fileNameLower.contains("carotte")) {
            txtNomProduit.setText("Carotte");
            comboCategorie.setValue("Légumes");
            lblAiConfidence.setText("Confiance: 75%");
            aiConfidenceBar.setProgress(0.75);
            lblSuggestion.setText("✓ Carotte détectée");
            lblSuggestion.setStyle("-fx-text-fill: #4CAF50;");
        }
        else if (fileNameLower.contains("banane")) {
            txtNomProduit.setText("Banane");
            comboCategorie.setValue("Fruits");
            lblAiConfidence.setText("Confiance: 80%");
            aiConfidenceBar.setProgress(0.80);
            lblSuggestion.setText("✓ Banane détectée");
            lblSuggestion.setStyle("-fx-text-fill: #4CAF50;");
        }
        else if (fileNameLower.contains("orange")) {
            txtNomProduit.setText("Orange");
            comboCategorie.setValue("Fruits");
            lblAiConfidence.setText("Confiance: 85%");
            aiConfidenceBar.setProgress(0.85);
            lblSuggestion.setText("✓ Orange détectée");
            lblSuggestion.setStyle("-fx-text-fill: #4CAF50;");
        }
        else if (fileNameLower.contains("salade")) {
            txtNomProduit.setText("Salade");
            comboCategorie.setValue("Légumes");
            lblAiConfidence.setText("Confiance: 70%");
            aiConfidenceBar.setProgress(0.70);
            lblSuggestion.setText("✓ Salade détectée");
            lblSuggestion.setStyle("-fx-text-fill: #4CAF50;");
        }
        else {
            txtNomProduit.setText("");
            lblAiConfidence.setText("Produit non reconnu");
            aiConfidenceBar.setProgress(0);
            lblSuggestion.setText("⚠️ Saisie manuelle requise");
            lblSuggestion.setStyle("-fx-text-fill: #FF9800;");
        }

        lblMessage.setText("✅ Analyse terminée");
        lblMessage.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void handleAjouter() {
        System.out.println("=== BOUTON AJOUTER CLIQUE ===");

        // Validation
        if (txtIdUtilisateur.getText().isEmpty()) {
            showError("Veuillez entrer l'ID utilisateur");
            return;
        }

        if (txtNomProduit.getText().isEmpty()) {
            showError("Veuillez entrer le nom du produit");
            return;
        }

        if (comboCategorie.getValue() == null) {
            showError("Veuillez sélectionner une catégorie");
            return;
        }

        if (comboUnite.getValue() == null) {
            showError("Veuillez sélectionner une unité");
            return;
        }

        if (txtQuantite.getText().isEmpty()) {
            showError("Veuillez entrer la quantité");
            return;
        }

        try {
            int idUtilisateur = Integer.parseInt(txtIdUtilisateur.getText());
            double quantite = Double.parseDouble(txtQuantite.getText());

            if (quantite <= 0) {
                showError("La quantité doit être positive");
                return;
            }

            // Créer le stock
            Stock stock = new Stock();
            stock.setIdUtilisateur(idUtilisateur);
            stock.setNomProduit(txtNomProduit.getText().trim());
            stock.setTypeProduit(comboCategorie.getValue());
            stock.setUnite(comboUnite.getValue());
            stock.setQuantite(quantite);
            stock.setDateEntree(LocalDate.now());
            stock.setDateExpiration(dateExpiration.getValue());
            stock.setStatut("Disponible");

            // Ajouter à la base de données
            stockService.ajouterStock(stock);

            showSuccess("✅ Stock ajouté avec succès!\nID: " + stock.getIdStock() +
                    "\nProduit: " + stock.getNomProduit());

            // Fermer la fenêtre
            Stage stage = (Stage) txtNomProduit.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showError("ID Utilisateur et Quantité doivent être des nombres valides");
        } catch (Exception e) {
            showError("Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler() {
        System.out.println("=== BOUTON ANNULER CLIQUE ===");
        Stage stage = (Stage) txtNomProduit.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        lblMessage.setText("❌ " + message);
        lblMessage.setStyle("-fx-text-fill: red;");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        lblMessage.setText("✅ " + message);
        lblMessage.setStyle("-fx-text-fill: green;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}