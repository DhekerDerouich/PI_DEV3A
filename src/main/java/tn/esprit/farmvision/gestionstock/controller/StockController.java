package tn.esprit.farmvision.gestionstock.controller;
import tn.esprit.farmvision.gestionstock.service.PDFExportService;
import java.util.ArrayList;
import javafx.stage.Stage;
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
import java.time.format.DateTimeFormatter;
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

    @FXML private Button btnDashboard;
    @FXML private Button btnStocks;
    @FXML private Button btnMarketplace;
    @FXML private Button btnStats;

    @FXML private Label lblTotalProduits;
    @FXML private Label lblValeurTotale;
    @FXML private Label lblProduitsExpires;

    private StockService stockService;
    private ObservableList<Stock> stockList;

    @FXML
    public void initialize() {
        System.out.println("\n=== INITIALISATION STOCK CONTROLLER ===");

        stockService = new StockService();
        stockList = FXCollections.observableArrayList();

        // Configuration des colonnes
        colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("typeProduit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));
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
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), item);
                    if (joursRestants < 0) {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    } else if (joursRestants < 7) {
                        setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                    } else if (joursRestants < 30) {
                        setStyle("-fx-text-fill: #2196F3;");
                    } else {
                        setStyle("-fx-text-fill: #2e7d32;");
                    }
                }
            }
        });

        // Colorer le statut
        colStatut.setCellFactory(column -> new TableCell<Stock, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Disponible":
                            setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            break;
                        case "Réservé":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        case "Épuisé":
                            setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                            break;
                        case "Périmé":
                            setStyle("-fx-text-fill: #9e9e9e; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
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

    public void chargerStocks() {
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

            mettreAJourStatistiques();

            System.out.println("✅ Affichage mis à jour\n");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des stocks", e.getMessage());
        }
    }

    private void mettreAJourStatistiques() {
        try {
            int total = stockList.size();
            double valeurTotale = stockList.stream()
                    .mapToDouble(s -> s.getQuantite() * 2.5)
                    .sum();
            long expires = stockList.stream()
                    .filter(s -> s.getDateExpiration() != null)
                    .filter(s -> s.getDateExpiration().isBefore(LocalDate.now()))
                    .count();

            lblTotalProduits.setText(String.valueOf(total));
            lblValeurTotale.setText(String.format("%.2f DT", valeurTotale));
            lblProduitsExpires.setText(String.valueOf(expires));

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour stats: " + e.getMessage());
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
                    (stock.getTypeProduit() != null && stock.getTypeProduit().toLowerCase().contains(recherche));

            boolean matchesCategorie = categorie.equals("Tous") ||
                    (stock.getTypeProduit() != null && stock.getTypeProduit().equals(categorie));

            boolean matchesStatut = statut.equals("Tous") ||
                    (stock.getStatut() != null && stock.getStatut().equals(statut));

            if (matchesRecherche && matchesCategorie && matchesStatut) {
                listeFiltree.add(stock);
            }
        }
        stockTable.setItems(listeFiltree);
    }

    // ==================== ACTIONS CRUD ====================

    @FXML
    private void handleAjouterStock() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_stock.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Stock");
            stage.setScene(new Scene(root, 560, 620));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_stock.fxml"));
            Parent root = loader.load();
            ModifierStockController controller = loader.getController();
            controller.setStock(stockSelectionne);
            Stage stage = new Stage();
            stage.setTitle("Modifier le Stock");
            stage.setScene(new Scene(root, 560, 620));
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

    // ==================== MÉTHODES DE NAVIGATION ====================


    @FXML
    private void handleStocks() {
        try {
            System.out.println("🔄 Rechargement de la page Stocks...");

            // Recharger la vue actuelle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestion_stock.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et recharger les données
            StockController controller = loader.getController();
            controller.chargerStocks();

            // Récupérer la scène actuelle
            Stage stage = (Stage) btnStocks.getScene().getWindow();

            // Créer et appliquer la nouvelle scène
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.setTitle("FarmVision - Gestion des Stocks");
            stage.show();

            System.out.println("✅ Page Stocks rechargée avec succès");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du rechargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Rechargement échoué", e.getMessage());
        }
    }

    @FXML
    private void handleMarketplace() {
        try {
            System.out.println("🔄 Navigation vers Marketplace...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestion_marketplace.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnMarketplace.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("FarmVision - Marketplace");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Navigation échouée", e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void handleExporterPDF() {
        try {
            System.out.println("\n=== EXPORT PDF ===");

            // Vérifier s'il y a des stocks à exporter
            if (stockList == null || stockList.isEmpty()) {
                showAlert("Information", "Aucune donnée",
                        "Il n'y a aucun stock à exporter. Veuillez d'abord ajouter des stocks.");
                return;
            }

            // Créer le service d'export
            PDFExportService pdfExportService = new PDFExportService();

            // Exporter
            boolean success = pdfExportService.exportStocksToPDF(
                    new ArrayList<>(stockList),
                    (Stage) btnStocks.getScene().getWindow()
            );

            if (success) {
                lblMessage.setText("✅ Rapport PDF exporté avec succès!");
                lblMessage.setStyle("-fx-text-fill: green;");

                // Optionnel: message de confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export PDF");
                alert.setHeaderText("Export réussi");
                alert.setContentText("Le rapport PDF a été généré avec succès!");
                alert.showAndWait();
            } else {
                lblMessage.setText("❌ Export annulé ou échoué");
                lblMessage.setStyle("-fx-text-fill: orange;");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'export PDF",
                    "Une erreur est survenue: " + e.getMessage());
        }
    }

    private void showAlert(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
    @FXML
    private void handleAjouterStockIA() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT IA ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_stock_ia.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajout Intelligent par IA");
            stage.setScene(new Scene(root, 600, 800));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre IA fermée - Rechargement des stocks...");
            chargerStocks();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
            e.printStackTrace();
        }
    }
}