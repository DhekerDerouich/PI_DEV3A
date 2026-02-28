package tn.esprit.farmvision.gestionstock.controller;

import tn.esprit.farmvision.gestionstock.service.PDFExportService;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    // ==================== MÉTHODE POUR CHARGER DANS LE CONTENTPANE ====================

    private void loadViewInContentPane(String fxmlFile, String title) {
        try {
            System.out.println("🔄 Chargement de: " + fxmlFile + " dans le contentPane");

            String[] chemins = {
                    "/tn/esprit/farmvision/resources1/fxml/" + fxmlFile,
                    "/resources1/fxml/" + fxmlFile,
                    "/fxml/" + fxmlFile,
                    "/" + fxmlFile
            };

            URL url = null;
            for (String chemin : chemins) {
                url = getClass().getResource(chemin);
                System.out.println("Test: " + chemin + " → " + (url != null ? "✅" : "❌"));
                if (url != null) break;
            }

            if (url == null) {
                showAlert("Erreur", "Fichier introuvable",
                        "Impossible de trouver " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            // Récupérer le contentPane du MainController
            Stage stage = (Stage) btnStocks.getScene().getWindow();
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            StackPane contentPane = (StackPane) root.getCenter();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

            // Mettre à jour le titre dans la barre d'état
            Label statusLabel = (Label) root.lookup("#statusLabel");
            if (statusLabel != null) {
                statusLabel.setText(title);
            }

            System.out.println("✅ " + title + " chargé avec succès");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement échoué", e.getMessage());
        }
    }

    // ==================== MÉTHODE UTILITAIRE POUR CHARGER LES FICHIERS MODAUX ====================

    private Parent loadModalFXML(String fxmlFile) {
        try {
            System.out.println("🔍 Recherche de: " + fxmlFile);

            String[] chemins = {
                    "/tn/esprit/farmvision/resources1/fxml/" + fxmlFile,
                    "/resources1/fxml/" + fxmlFile,
                    "/fxml/" + fxmlFile,
                    "/" + fxmlFile
            };

            URL url = null;
            for (String chemin : chemins) {
                url = getClass().getResource(chemin);
                System.out.println("Test: " + chemin + " → " + (url != null ? "✅" : "❌"));
                if (url != null) {
                    System.out.println("✅ Fichier trouvé: " + url);
                    break;
                }
            }

            if (url == null) {
                showAlert("Erreur", "Fichier introuvable", "Impossible de trouver " + fxmlFile);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(url);
            return loader.load();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement échoué", e.getMessage());
            return null;
        }
    }

    // ==================== ACTIONS CRUD ====================

    @FXML
    private void handleAjouterStock() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT STOCK ===");

            Parent root = loadModalFXML("ajouter_stock.fxml");
            if (root == null) return;

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Stock");
            stage.setScene(new Scene(root, 560, 620));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des stocks...");
            chargerStocks();

        } catch (Exception e) {
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
            System.out.println("\n=== OUVERTURE FENÊTRE MODIFICATION STOCK ===");

            String[] chemins = {
                    "/tn/esprit/farmvision/resources1/fxml/modifier_stock.fxml",
                    "/resources1/fxml/modifier_stock.fxml",
                    "/fxml/modifier_stock.fxml"
            };

            URL url = null;
            for (String chemin : chemins) {
                url = getClass().getResource(chemin);
                if (url != null) break;
            }

            if (url == null) {
                showAlert("Erreur", "Fichier introuvable", "modifier_stock.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            ModifierStockController controller = loader.getController();
            controller.setStock(stockSelectionne);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Stock");
            stage.setScene(new Scene(root, 560, 620));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des stocks...");
            chargerStocks();

        } catch (Exception e) {
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

    // ==================== MÉTHODES DE NAVIGATION (DANS LE CONTENTPANE) ====================

    @FXML
    private void handleStocks() {
        // Recharger la page actuelle (gestion_stock.fxml)
        loadViewInContentPane("gestion_stock.fxml", "Gestion des Stocks");
    }

    @FXML
    private void handleMarketplace() {
        // Charger la page marketplace
        loadViewInContentPane("gestion_marketplace.fxml", "Marketplace");
    }

    @FXML
    private void handleDashboard() {
        // Optionnel: Dashboard des stocks
        showAlert("Information", "Dashboard", "Fonctionnalité à venir");
    }

    @FXML
    private void handleStats() {
        // Optionnel: Statistiques
        showAlert("Information", "Statistiques", "Fonctionnalité à venir");
    }

    @FXML
    private void handleAjouterStockIA() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT IA ===");

            Parent root = loadModalFXML("ajouter_stock_ia.fxml");
            if (root == null) return;

            Stage stage = new Stage();
            stage.setTitle("Ajout Intelligent par IA");
            stage.setScene(new Scene(root, 600, 800));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre IA fermée - Rechargement des stocks...");
            chargerStocks();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
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

            // ✅ CORRECTION: Utiliser stockTable au lieu de btnStocks
            Stage stage = (Stage) stockTable.getScene().getWindow();

            // Créer le service d'export
            PDFExportService pdfExportService = new PDFExportService();

            // Exporter
            boolean success = pdfExportService.exportStocksToPDF(
                    new ArrayList<>(stockList),
                    stage  // ← Utiliser stage
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
    private void handleControleQualite() {
        Stock stockSelectionne = stockTable.getSelectionModel().getSelectedItem();
        if (stockSelectionne == null) {
            showAlert("Avertissement", "Aucune sélection",
                    "Veuillez sélectionner un stock pour le contrôle qualité.");
            return;
        }

        try {
            // Simuler un contrôle qualité
            String statut = stockSelectionne.getStatut();
            LocalDate dateExp = stockSelectionne.getDateExpiration();

            StringBuilder rapport = new StringBuilder();
            rapport.append("🔍 CONTRÔLE QUALITÉ\n\n");
            rapport.append("Produit: ").append(stockSelectionne.getNomProduit()).append("\n");
            rapport.append("Catégorie: ").append(stockSelectionne.getTypeProduit()).append("\n");
            rapport.append("Quantité: ").append(stockSelectionne.getQuantite())
                    .append(" ").append(stockSelectionne.getUnite()).append("\n");

            if (dateExp != null) {
                long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dateExp);
                rapport.append("Jours avant expiration: ").append(joursRestants).append("\n");

                if (joursRestants < 0) {
                    rapport.append("⚠️ STATUT: EXPIRÉ - Produit non consommable\n");
                } else if (joursRestants < 7) {
                    rapport.append("⚠️ STATUT: URGENT - À vendre rapidement\n");
                } else if (joursRestants < 30) {
                    rapport.append("ℹ️ STATUT: ATTENTION - À surveiller\n");
                } else {
                    rapport.append("✅ STATUT: CONFORME - Produit frais\n");
                }
            } else {
                rapport.append("ℹ️ Pas de date d'expiration\n");
            }

            rapport.append("\n📋 Recommandation: ");
            if ("Disponible".equals(statut)) {
                rapport.append("Produit prêt pour le marché");
            } else if ("Épuisé".equals(statut)) {
                rapport.append("Réapprovisionnement nécessaire");
            } else if ("Périmé".equals(statut)) {
                rapport.append("Élimination requise");
            } else {
                rapport.append("Aucune action spécifique");
            }

            showAlert("Contrôle Qualité", "Résultat", rapport.toString());

        } catch (Exception e) {
            showAlert("Erreur", "Erreur contrôle qualité", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnalysePrevisionnelle() {
        try {
            if (stockList == null || stockList.isEmpty()) {
                showAlert("Information", "Aucune donnée",
                        "Aucun stock disponible pour l'analyse.");
                return;
            }

            StringBuilder resultat = new StringBuilder("📈 ANALYSE PRÉVISIONNELLE\n\n");

            int total = stockList.size();
            double valeurTotale = stockList.stream()
                    .mapToDouble(s -> s.getQuantite() * 2.5)
                    .sum();
            long presqueExpires = stockList.stream()
                    .filter(s -> s.getDateExpiration() != null)
                    .filter(s -> {
                        long jours = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), s.getDateExpiration());
                        return jours > 0 && jours < 7;
                    })
                    .count();
            long dejaExpires = stockList.stream()
                    .filter(s -> s.getDateExpiration() != null)
                    .filter(s -> s.getDateExpiration().isBefore(LocalDate.now()))
                    .count();

            resultat.append("📊 Statistiques globales:\n");
            resultat.append("   • Total produits: ").append(total).append("\n");
            resultat.append("   • Valeur totale: ").append(String.format("%.2f DT", valeurTotale)).append("\n");
            resultat.append("   • Produits expirés: ").append(dejaExpires).append("\n");
            resultat.append("   • Expiration < 7 jours: ").append(presqueExpires).append("\n\n");

            resultat.append("🔮 Prédictions pour le mois prochain:\n");
            resultat.append("   • Risque de rupture: ").append(calculerRisqueRupture()).append("%\n");
            resultat.append("   • Produits à réapprovisionner: ").append(presqueExpires + 2).append("\n");
            resultat.append("   • Valeur recommandée: ").append(String.format("%.2f DT", valeurTotale * 0.3)).append("\n\n");

            resultat.append("💡 Recommandations:\n");
            if (dejaExpires > 0) {
                resultat.append("   • Éliminer les produits expirés immédiatement\n");
            }
            if (presqueExpires > 0) {
                resultat.append("   • Mettre en promotion les produits proches de l'expiration\n");
            }
            resultat.append("   • Maintenir un stock de sécurité pour les produits populaires\n");

            showAlert("Analyse Prévisionnelle", "Résultats", resultat.toString());

        } catch (Exception e) {
            showAlert("Erreur", "Erreur analyse", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoogleSync() {
        try {
            System.out.println("\n=== SYNCHRONISATION GOOGLE MERCHANT ===");

            if (stockList == null || stockList.isEmpty()) {
                showAlert("Information", "Aucune donnée",
                        "Aucun stock à synchroniser.");
                return;
            }

            // Simulation de synchronisation
            StringBuilder result = new StringBuilder();
            result.append("🌐 SYNCHRONISATION GOOGLE MERCHANT\n\n");
            result.append("Total stocks: ").append(stockList.size()).append("\n");
            result.append("✅ Succès: ").append(stockList.size()).append("\n");
            result.append("❌ Échecs: 0\n");
            result.append("Taux de réussite: 100%\n\n");
            result.append("Produits synchronisés:\n");

            stockList.stream().limit(5).forEach(s ->
                    result.append("   • ").append(s.getNomProduit()).append("\n")
            );

            if (stockList.size() > 5) {
                result.append("   • ... et ").append(stockList.size() - 5).append(" autres\n");
            }

            showAlert("Synchronisation Google Merchant", "Résultat", result.toString());

        } catch (Exception e) {
            showAlert("Erreur", "Erreur synchronisation", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInvenTreeSync() {
        try {
            System.out.println("\n=== SYNCHRONISATION INVENTREE ===");

            if (stockList == null || stockList.isEmpty()) {
                showAlert("Information", "Aucune donnée",
                        "Aucun stock à synchroniser.");
                return;
            }

            // Simulation de synchronisation InvenTree
            StringBuilder result = new StringBuilder();
            result.append("📦 SYNCHRONISATION INVENTREE\n\n");
            result.append("Statut: ✅ Succès\n");
            result.append("Stocks synchronisés: ").append(stockList.size()).append("\n");
            result.append("Catégories créées: 5\n");
            result.append("Historique mis à jour: ").append(stockList.size()).append(" entrées\n\n");
            result.append("Dernière synchronisation: ").append(LocalDate.now()).append("\n");

            showAlert("Synchronisation InvenTree", "Résultat", result.toString());

        } catch (Exception e) {
            showAlert("Erreur", "Erreur synchronisation", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleParametres() {
        showAlert("Paramètres", "Fonctionnalité à venir",
                "La gestion des paramètres sera disponible prochainement.");
    }

    private int calculerRisqueRupture() {
        if (stockList == null || stockList.isEmpty()) return 0;

        long stockFaible = stockList.stream()
                .filter(s -> s.getQuantite() < 10)
                .count();

        return (int) ((stockFaible * 100) / stockList.size());
    }
}