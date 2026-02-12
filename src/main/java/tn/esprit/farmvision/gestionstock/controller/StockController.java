package tn.esprit.farmvision.gestionstock.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StockController {

    @FXML private TableView<Stock> stockTable;
    @FXML private TableColumn<Stock, Integer> colId;
    @FXML private TableColumn<Stock, String> colProduit;
    @FXML private TableColumn<Stock, String> colCategorie;
    @FXML private TableColumn<Stock, Double> colQuantite;
    @FXML private TableColumn<Stock, String> colUnite;
    @FXML private TableColumn<Stock, LocalDate> colDate;
    @FXML private TableColumn<Stock, LocalDate> colDateExpiration;
    @FXML private TableColumn<Stock, String> colStatut;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltreCategorie;
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private Label lblMessage;

    private StockService stockService;
    private ObservableList<Stock> stockList;

    @FXML
    public void initialize() {
        System.out.println("\n=== INITIALISATION STOCK CONTROLLER ===");

        stockService = new StockService();
        stockList = FXCollections.observableArrayList();

        // ✅ Configuration des colonnes avec les BONS noms de propriétés
        colId.setCellValueFactory(new PropertyValueFactory<>("idStock"));
        colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("typeProduit"));  // ← typeProduit
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));      // ← quantite
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));       // ← dateEntree
        colDateExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage de la colonne date
        colDate.setCellFactory(column -> new TableCell<Stock, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        colDateExpiration.setCellFactory(column -> new TableCell<Stock, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Filtres
        comboFiltreCategorie.getItems().addAll("Tous", "Légumes", "Fruits", "Céréales",
                "Légumineuses", "Produits laitiers", "Viandes", "Volailles", "Œufs", "Autre");
        comboFiltreCategorie.setValue("Tous");

        comboFiltreStatut.getItems().addAll("Tous", "Disponible", "Épuisé", "Périmé", "Réservé");
        comboFiltreStatut.setValue("Tous");

        chargerStocks();

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filtrerStocks());
        comboFiltreCategorie.valueProperty().addListener((observable, oldValue, newValue) -> filtrerStocks());
        comboFiltreStatut.valueProperty().addListener((observable, oldValue, newValue) -> filtrerStocks());

        System.out.println("✅ Initialisation terminée\n");
    }

    private void chargerStocks() {
        try {
            System.out.println("\n=== CHARGEMENT DES STOCKS ===");
            List<Stock> stocks = stockService.getAllStocks();
            System.out.println("📊 Résultat: " + stocks.size() + " stocks trouvés");

            stockList.clear();
            stockList.addAll(stocks);
            stockTable.setItems(stockList);
            stockTable.refresh();

            if (stocks.isEmpty()) {
                lblMessage.setText("⚠️ Aucun stock trouvé dans la base de données");
                lblMessage.setStyle("-fx-text-fill: orange;");
            } else {
                lblMessage.setText("✅ Chargement réussi : " + stocks.size() + " stocks");
                lblMessage.setStyle("-fx-text-fill: green;");
            }

            System.out.println("✅ Affichage mis à jour\n");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des stocks", e.getMessage());
        }
    }

    private void filtrerStocks() {
        String recherche = txtRecherche.getText().toLowerCase();
        String categorie = comboFiltreCategorie.getValue();
        String statut = comboFiltreStatut.getValue();

        ObservableList<Stock> listeFiltree = FXCollections.observableArrayList();

        for (Stock stock : stockList) {
            boolean matchesRecherche = recherche.isEmpty() ||
                    stock.getNomProduit().toLowerCase().contains(recherche) ||
                    stock.getTypeProduit().toLowerCase().contains(recherche);

            boolean matchesCategorie = categorie.equals("Tous") ||
                    stock.getTypeProduit().equals(categorie);

            boolean matchesStatut = statut.equals("Tous") ||
                    stock.getStatut().equals(statut);

            if (matchesRecherche && matchesCategorie && matchesStatut) {
                listeFiltree.add(stock);
            }
        }
        stockTable.setItems(listeFiltree);
    }

    @FXML
    private void handleAjouterStock() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_stock.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Stock");
            stage.setScene(new Scene(root, 550, 500));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des stocks...");
            chargerStocks();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifierStock() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock à modifier.");
            return;
        }

        try {
            System.out.println("\n=== OUVERTURE FENÊTRE MODIFICATION ===");
            System.out.println("Stock sélectionné: ID=" + stockSelectionne.getIdStock() +
                    ", Produit=" + stockSelectionne.getNomProduit());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_stock.fxml"));
            Parent root = loader.load();
            ModifierStockController controller = loader.getController();
            controller.setStock(stockSelectionne);
            Stage stage = new Stage();
            stage.setTitle("Modifier le Stock");
            stage.setScene(new Scene(root, 550, 550));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des stocks...");
            chargerStocks();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimerStock() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le stock");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le stock : " +
                stockSelectionne.getNomProduit() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                System.out.println("\n=== SUPPRESSION STOCK ===");
                System.out.println("Suppression du stock ID: " + stockSelectionne.getIdStock());

                stockService.supprimerStock(stockSelectionne.getIdStock());
                lblMessage.setText("✅ Stock supprimé : " + stockSelectionne.getNomProduit());
                lblMessage.setStyle("-fx-text-fill: green;");

                chargerStocks();

            } catch (Exception e) {
                showAlert("Erreur", "Erreur de suppression", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleVoirDetails() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock.");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("🆔 ID Stock: ").append(stockSelectionne.getIdStock()).append("\n");
        details.append("👤 ID Utilisateur: ").append(stockSelectionne.getIdUtilisateur()).append("\n");
        details.append("📦 Produit: ").append(stockSelectionne.getNomProduit()).append("\n");
        details.append("🏷️ Type: ").append(stockSelectionne.getTypeProduit()).append("\n");
        details.append("⚖️ Quantité: ").append(stockSelectionne.getQuantite())
                .append(" ").append(stockSelectionne.getUnite()).append("\n");
        details.append("📅 Date entrée: ").append(stockSelectionne.getDateEntree()).append("\n");
        details.append("⏰ Date expiration: ").append(stockSelectionne.getDateExpiration()).append("\n");
        details.append("📊 Statut: ").append(stockSelectionne.getStatut()).append("\n");

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Détails du Stock");
        info.setHeaderText("📋 Informations détaillées");
        info.setContentText(details.toString());
        info.showAndWait();
    }

    @FXML
    private void handleRafraichir() {
        System.out.println("\n=== RAFRAÎCHISSEMENT MANUEL ===");
        chargerStocks();
        lblMessage.setText("✅ Liste rafraîchie");
        lblMessage.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void handleExporterPDF() {
        showAlert("Information", "Export PDF", "Fonctionnalité à implémenter.");
    }

    @FXML
    private void handleImprimer() {
        showAlert("Information", "Impression", "Fonctionnalité à implémenter.");
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_stock.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("FarmVision - Gestion Stock");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
            ((Stage) stockTable.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}