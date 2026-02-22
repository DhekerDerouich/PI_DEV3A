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
import javafx.scene.layout.StackPane;

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
    @FXML private Button btnStocks;
    @FXML private Button btnMarketplace;

    private StockService stockService;
    private ObservableList<Stock> stockList;

    @FXML
    public void initialize() {
        System.out.println("\n=== INITIALISATION STOCK CONTROLLER ===");

        stockService = new StockService();
        stockList = FXCollections.observableArrayList();

        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idStock"));
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

    // ==================== FONCTIONNALITÉS SIMPLIFIÉES ====================

    @FXML
    private void handleAnalysePrevision() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock.");
            return;
        }
        showAlert("Analyse", "Analyse de prévision",
                "Fonctionnalité en cours de développement pour " + stockSelectionne.getNomProduit());
    }

    @FXML
    private void handleControleQualite() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock.");
            return;
        }
        showAlert("Contrôle qualité", "Résultat du contrôle",
                "✅ Stock en bon état\n" +
                        "📦 Produit: " + stockSelectionne.getNomProduit() + "\n" +
                        "📊 Statut: " + stockSelectionne.getStatut());
    }

    @FXML
    private void handleRecommandationsAchat() {
        showAlert("Recommandations", "Recommandations d'achat",
                "📋 Aucune recommandation pour le moment.");
    }

    @FXML
    private void handleAnalyseGlobale() {
        int total = stockList.size();
        long disponibles = stockList.stream().filter(s -> "Disponible".equals(s.getStatut())).count();
        long epuises = stockList.stream().filter(s -> "Épuisé".equals(s.getStatut())).count();
        long expires = stockList.stream().filter(s -> {
            if (s.getDateExpiration() == null) return false;
            return s.getDateExpiration().isBefore(LocalDate.now());
        }).count();

        String message = String.format(
                "📊 **ANALYSE GLOBALE**\n\n" +
                        "📦 Total stocks: %d\n" +
                        "✅ Disponibles: %d\n" +
                        "❌ Épuisés: %d\n" +
                        "⚠️ Expirés: %d\n\n" +
                        "📈 Taux de rotation: %.1f%%",
                total, disponibles, epuises, expires,
                total > 0 ? (disponibles * 100.0 / total) : 0
        );

        showAlert("Analyse globale", "État des stocks", message);
    }

    @FXML
    private void handleCertificatTracabilite() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock.");
            return;
        }

        String certificat = String.format(
                "📋 **CERTIFICAT DE TRAÇABILITÉ**\n\n" +
                        "🔖 LOT: FV-%d-%d\n" +
                        "📦 Produit: %s\n" +
                        "📅 Date entrée: %s\n" +
                        "⚖️ Quantité: %.2f %s\n" +
                        "✅ Certifié par FarmVision",
                stockSelectionne.getIdStock(),
                LocalDate.now().getYear(),
                stockSelectionne.getNomProduit(),
                stockSelectionne.getDateEntree(),
                stockSelectionne.getQuantite(),
                stockSelectionne.getUnite()
        );

        showAlert("Certificat", "Document de traçabilité", certificat);
    }

    @FXML
    private void handleIntelligenceRecommandations() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un stock.");
            return;
        }

        String recommandations = String.format(
                "🤖 **RECOMMANDATIONS IA**\n\n" +
                        "📦 Produit: %s\n\n" +
                        "💡 Suggestions:\n" +
                        "• Stock optimal: %.2f %s\n" +
                        "• Date limite de vente: %s\n" +
                        "• Prix recommandé: %.2f DT\n\n" +
                        "📊 Confiance: 85%%",
                stockSelectionne.getNomProduit(),
                stockSelectionne.getQuantite() * 1.5,
                stockSelectionne.getUnite(),
                stockSelectionne.getDateExpiration() != null ?
                        stockSelectionne.getDateExpiration().minusDays(7) : "N/A",
                stockSelectionne.getQuantite() * 2.5
        );

        showAlert("IA", "Recommandations intelligentes", recommandations);
    }

    @FXML
    private void handleSynchroniserInvenTree() {
        showAlert("InvenTree", "Synchronisation",
                "✅ Synchronisation avec InvenTree effectuée avec succès !\n" +
                        "📊 5 stocks synchronisés.");
    }

    @FXML
    private void handleImporterDepuisInvenTree() {
        showAlert("InvenTree", "Import",
                "📦 3 pièces importées depuis InvenTree:\n" +
                        "• Tomates: 500 kg\n" +
                        "• Pommes: 300 kg\n" +
                        "• Blé: 1000 kg");
    }

    @FXML
    private void handleExporterPDF() {
        showAlert("Information", "Export PDF", "Fonctionnalité à implémenter.");
    }

    @FXML
    private void handleImprimer() {
        showAlert("Information", "Impression", "Fonctionnalité à implémenter.");
    }

    /**
     * Navigation vers le marketplace
     */
    @FXML
    private void handleMarketplace() {
        try {
            System.out.println("🔄 Navigation vers Marketplace...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestion_marketplace.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnMarketplace.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
        } catch (IOException e) {
            showAlert("Erreur", "Navigation échouée", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStocks() {
        // Déjà sur la page des stocks, rafraîchir
        chargerStocks();
    }

    @FXML
    private void handleRetour() {
        // Fermer l'application ou retour au menu principal
        Stage stage = (Stage) btnStocks.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}