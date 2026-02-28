package tn.esprit.farmvision.gestionstock.controller;

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
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Marketplace;
import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.MarketplaceService;
import tn.esprit.farmvision.gestionstock.service.StockService;
import tn.esprit.farmvision.integrations.api.GoogleMerchantClient;
import tn.esprit.farmvision.integrations.api.InvenTreeClient;
import tn.esprit.farmvision.integrations.model.SyncResult;
import tn.esprit.farmvision.gestionstock.metier.QualityTraceabilityController;
import tn.esprit.farmvision.gestionstock.metier.StockForecastingAnalyst;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MarketplaceController {

    @FXML private TableView<Marketplace> marketplaceTable;
    @FXML private TableColumn<Marketplace, Integer> colId;
    @FXML private TableColumn<Marketplace, String> colProduit;
    @FXML private TableColumn<Marketplace, String> colCategorie;
    @FXML private TableColumn<Marketplace, Double> colPrix;
    @FXML private TableColumn<Marketplace, Double> colQuantite;
    @FXML private TableColumn<Marketplace, String> colVendeur;
    @FXML private TableColumn<Marketplace, String> colStatut;
    @FXML private TableColumn<Marketplace, String> colDate;

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private Label lblMessage;

    @FXML private Button btnDashboard;
    @FXML private Button btnStocks;
    @FXML private Button btnMarketplace;
    @FXML private Button btnStats;
    @FXML private Button btnGoogleSync;
    @FXML private Button btnAnalytics;

    @FXML private Label lblTotalAnnonces;
    @FXML private Label lblChiffreAffaires;
    @FXML private Label lblVentesMois;

    private MarketplaceService marketplaceService;
    private StockService stockService;
    private QualityTraceabilityController qualityController;
    private StockForecastingAnalyst forecastingAnalyst;
    private GoogleMerchantClient googleMerchantClient;
    private ObservableList<Marketplace> marketplaceList;

    @FXML
    public void initialize() {
        System.out.println("\n=== INITIALISATION MARKETPLACE CONTROLLER ===");

        marketplaceService = new MarketplaceService();
        stockService = new StockService();
        qualityController = new QualityTraceabilityController();
        forecastingAnalyst = new StockForecastingAnalyst();

        // Initialiser Google Merchant Client (avec token fictif pour l'instant)
        googleMerchantClient = new GoogleMerchantClient("fake-token", "merchant-123");

        marketplaceList = FXCollections.observableArrayList();

        setupTableColumns();
        setupFilters();

        chargerMarketplaces();

        System.out.println("✅ Initialisation terminée\n");
    }

    private void setupTableColumns() {
        colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteEnVente"));
        colVendeur.setCellValueFactory(new PropertyValueFactory<>("nomVendeur"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePublicationFormatted"));

        // Formatage prix
        colPrix.setCellFactory(column -> new TableCell<Marketplace, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.3f DT", item));
                }
            }
        });

        // Formatage vendeur
        colVendeur.setCellFactory(column -> new TableCell<Marketplace, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty() || "null null".equals(item)) {
                    setText("Vendeur inconnu");
                    setStyle("-fx-text-fill: #9e9e9e; -fx-font-style: italic;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                }
            }
        });

        // Formatage statut avec couleurs
        colStatut.setCellFactory(column -> new TableCell<Marketplace, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "En vente":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "Vendu":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "Réservé":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        comboFiltreStatut.getItems().addAll("Tous", "En vente", "Vendu", "Réservé");
        comboFiltreStatut.setValue("Tous");

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filtrerMarketplaces());
        comboFiltreStatut.valueProperty().addListener((observable, oldValue, newValue) -> filtrerMarketplaces());
    }

    public void chargerMarketplaces() {
        try {
            System.out.println("\n=== CHARGEMENT DES ANNONCES ===");
            List<Marketplace> marketplaces = marketplaceService.getAllMarketplaces();
            System.out.println("📊 Résultat: " + marketplaces.size() + " annonces trouvées");

            marketplaceList.clear();
            marketplaceList.addAll(marketplaces);
            marketplaceTable.setItems(marketplaceList);
            marketplaceTable.refresh();

            if (marketplaces.isEmpty()) {
                lblMessage.setText("⚠️ Aucune annonce trouvée dans la base de données");
                lblMessage.setStyle("-fx-text-fill: orange;");
            } else {
                lblMessage.setText("✅ Chargement réussi : " + marketplaces.size() + " annonces");
                lblMessage.setStyle("-fx-text-fill: green;");
            }

            mettreAJourStatistiques();

            System.out.println("✅ Affichage mis à jour\n");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des annonces", e.getMessage());
        }
    }

    private void mettreAJourStatistiques() {
        try {
            int total = marketplaceList.size();
            double chiffreAffaires = marketplaceList.stream()
                    .filter(m -> "Vendu".equals(m.getStatut()))
                    .mapToDouble(m -> m.getPrixUnitaire() * m.getQuantiteEnVente())
                    .sum();
            long ventesMois = marketplaceList.stream()
                    .filter(m -> "Vendu".equals(m.getStatut()))
                    .filter(m -> m.getDatePublication() != null)
                    .filter(m -> m.getDatePublication().getMonth() == LocalDateTime.now().getMonth())
                    .count();

            lblTotalAnnonces.setText(String.valueOf(total));
            lblChiffreAffaires.setText(String.format("%.2f DT", chiffreAffaires));
            lblVentesMois.setText(String.valueOf(ventesMois));

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour stats: " + e.getMessage());
        }
    }

    private void filtrerMarketplaces() {
        String recherche = txtRecherche.getText().toLowerCase();
        String statut = comboFiltreStatut.getValue();

        ObservableList<Marketplace> listeFiltree = FXCollections.observableArrayList();

        for (Marketplace m : marketplaceList) {
            boolean matchesRecherche = recherche.isEmpty() ||
                    (m.getNomProduit() != null && m.getNomProduit().toLowerCase().contains(recherche)) ||
                    (m.getDescription() != null && m.getDescription().toLowerCase().contains(recherche)) ||
                    (m.getNomVendeur() != null && m.getNomVendeur().toLowerCase().contains(recherche));

            boolean matchesStatut = statut.equals("Tous") ||
                    (m.getStatut() != null && m.getStatut().equals(statut));

            if (matchesRecherche && matchesStatut) {
                listeFiltree.add(m);
            }
        }
        marketplaceTable.setItems(listeFiltree);
    }

    // ==================== MÉTHODES DE CHARGEMENT FXML ====================

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
                if (url != null) break;
            }

            if (url == null) {
                showAlert("Erreur", "Fichier introuvable", "Impossible de trouver " + fxmlFile);
                return null;
            }

            System.out.println("✅ Fichier trouvé: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            return loader.load();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement échoué", e.getMessage());
            return null;
        }
    }

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
                showAlert("Erreur", "Fichier introuvable", "Impossible de trouver " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            Stage stage = (Stage) btnStocks.getScene().getWindow();
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            StackPane contentPane = (StackPane) root.getCenter();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

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

    // ==================== ACTIONS CRUD ====================

    @FXML
    private void handleAjouterAnnonce() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT ANNONCE ===");

            String[] chemins = {
                    "/tn/esprit/farmvision/resources1/fxml/ajouter_marketplace.fxml",
                    "/resources1/fxml/ajouter_marketplace.fxml",
                    "/fxml/ajouter_marketplace.fxml"
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
                showAlert("Erreur", "Fichier introuvable",
                        "ajouter_marketplace.fxml n'a pas été trouvé");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            System.out.println("✅ FXML chargé avec succès");

            // ✅ Vérifier que le contrôleur est bien chargé
            Object controller = loader.getController();
            System.out.println("✅ Contrôleur: " + (controller != null ? controller.getClass().getSimpleName() : "null"));

            Stage stage = new Stage();
            stage.setTitle("Ajouter une annonce");
            stage.setScene(new Scene(root, 560, 600));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des annonces...");
            chargerMarketplaces();

        } catch (Exception e) {
            System.err.println("❌ Erreur détaillée: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
        }
    }

    @FXML
    private void handleModifierAnnonce() {
        Marketplace annonceSelectionnee = marketplaceTable.getSelectionModel().getSelectedItem();
        if (annonceSelectionnee == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner une annonce à modifier.");
            return;
        }

        try {
            System.out.println("\n=== OUVERTURE FENÊTRE MODIFICATION ANNONCE ===");

            String[] chemins = {
                    "/tn/esprit/farmvision/resources1/fxml/modifier_marketplace.fxml",
                    "/resources1/fxml/modifier_marketplace.fxml",
                    "/fxml/modifier_marketplace.fxml"
            };

            URL url = null;
            for (String chemin : chemins) {
                url = getClass().getResource(chemin);
                if (url != null) break;
            }

            if (url == null) {
                showAlert("Erreur", "Fichier introuvable", "modifier_marketplace.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            ModifierMarketplaceController controller = loader.getController();
            controller.setMarketplace(annonceSelectionnee);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'annonce");
            stage.setScene(new Scene(root, 560, 600));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des annonces...");
            chargerMarketplaces();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimerAnnonce() {
        Marketplace annonceSelectionnee = marketplaceTable.getSelectionModel().getSelectedItem();
        if (annonceSelectionnee == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner une annonce à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'annonce");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'annonce : " +
                annonceSelectionnee.getNomProduit() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                System.out.println("\n=== SUPPRESSION ANNONCE ===");
                marketplaceService.supprimerMarketplace(annonceSelectionnee.getIdMarketplace());
                lblMessage.setText("✅ Annonce supprimée : " + annonceSelectionnee.getNomProduit());
                lblMessage.setStyle("-fx-text-fill: green;");
                chargerMarketplaces();

            } catch (Exception e) {
                showAlert("Erreur", "Erreur de suppression", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAcheter() {
        Marketplace annonceSelectionnee = marketplaceTable.getSelectionModel().getSelectedItem();
        if (annonceSelectionnee == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner un produit à acheter.");
            return;
        }

        if (!"En vente".equals(annonceSelectionnee.getStatut())) {
            showAlert("Information", "Produit non disponible", "Ce produit n'est plus disponible à la vente.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Achat de produit");
        dialog.setHeaderText("Acheter : " + annonceSelectionnee.getNomProduit());
        dialog.setContentText("Quantité (max " + annonceSelectionnee.getQuantiteEnVente() + "):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double quantite = Double.parseDouble(result.get());
                if (quantite <= 0) {
                    showAlert("Erreur", "Quantité invalide", "La quantité doit être positive.");
                    return;
                }
                if (quantite > annonceSelectionnee.getQuantiteEnVente()) {
                    showAlert("Erreur", "Quantité insuffisante",
                            "Stock disponible: " + annonceSelectionnee.getQuantiteEnVente());
                    return;
                }

                marketplaceService.acheterProduit(annonceSelectionnee.getIdMarketplace(), quantite);

                double total = quantite * annonceSelectionnee.getPrixUnitaire();
                showAlert("Succès", "Achat effectué!",
                        "Vous avez acheté " + quantite + " unités de " + annonceSelectionnee.getNomProduit() +
                                "\nPrix unitaire: " + annonceSelectionnee.getPrixUnitaire() + " DT" +
                                "\nTotal: " + String.format("%.3f", total) + " DT");

                chargerMarketplaces();

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Quantité invalide", "Veuillez saisir un nombre valide.");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur d'achat", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleVoirDetails() {
        Marketplace annonceSelectionnee = marketplaceTable.getSelectionModel().getSelectedItem();
        if (annonceSelectionnee == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner une annonce.");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("🆔 ID Annonce: ").append(annonceSelectionnee.getIdMarketplace()).append("\n");
        details.append("🆔 ID Stock: ").append(annonceSelectionnee.getIdStock()).append("\n");
        details.append("📦 Produit: ").append(annonceSelectionnee.getNomProduit()).append("\n");
        details.append("🏷️ Catégorie: ").append(annonceSelectionnee.getCategorie()).append("\n");
        details.append("💰 Prix unitaire: ").append(String.format("%.3f", annonceSelectionnee.getPrixUnitaire())).append(" DT\n");
        details.append("📊 Quantité disponible: ").append(annonceSelectionnee.getQuantiteEnVente()).append("\n");

        String vendeur = annonceSelectionnee.getNomVendeur();
        if (vendeur == null || vendeur.trim().isEmpty() || "null null".equals(vendeur)) {
            details.append("👤 Vendeur: ").append("Non spécifié").append("\n");
        } else {
            details.append("👤 Vendeur: ").append(vendeur).append("\n");
        }

        details.append("📅 Date publication: ").append(annonceSelectionnee.getDatePublicationFormatted()).append("\n");
        details.append("📋 Statut: ").append(annonceSelectionnee.getStatut()).append("\n");

        if (annonceSelectionnee.getDescription() != null && !annonceSelectionnee.getDescription().isEmpty()) {
            details.append("📝 Description: ").append(annonceSelectionnee.getDescription()).append("\n");
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Détails de l'annonce");
        info.setHeaderText("📋 Informations détaillées");
        info.setContentText(details.toString());
        info.showAndWait();
    }

    @FXML
    private void handleRafraichir() {
        System.out.println("\n=== RAFRAÎCHISSEMENT MANUEL ===");
        chargerMarketplaces();
        lblMessage.setText("✅ Liste rafraîchie");
        lblMessage.setStyle("-fx-text-fill: green;");
    }

    // ==================== MÉTIER AVANCÉ ====================

    @FXML
    private void handleControleQualite() {
        Marketplace annonceSelectionnee = marketplaceTable.getSelectionModel().getSelectedItem();
        if (annonceSelectionnee == null) {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner une annonce pour le contrôle qualité.");
            return;
        }

        try {
            // Récupérer le stock associé
            Stock stock = stockService.getStockById(annonceSelectionnee.getIdStock());
            if (stock == null) {
                showAlert("Erreur", "Stock introuvable", "Le stock associé à cette annonce n'existe pas.");
                return;
            }

            // Effectuer le contrôle qualité
            QualityTraceabilityController.RapportQualite rapport = qualityController.effectuerControleQualite(stock);

            // Afficher le rapport
            String message = String.format(
                    "📋 RAPPORT QUALITÉ\n\n" +
                            "Produit: %s\n" +
                            "Statut: %s\n" +
                            "Alerte: %s\n" +
                            "Action requise: %s\n" +
                            "Recommandation: %s\n" +
                            "Jours avant expiration: %d\n",
                    rapport.getProduit(),
                    rapport.getStatut(),
                    rapport.getAlerte(),
                    rapport.getActionRequise(),
                    rapport.getRecommendation(),
                    rapport.getJoursAvantExpiration()
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Contrôle Qualité");
            alert.setHeaderText("Résultat du contrôle qualité");
            alert.setContentText(message);

            // Colorer selon le statut
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + rapport.getCouleurStatut() + ";");

            alert.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur contrôle qualité", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnalysePrevisionnelle() {
        try {
            List<Stock> tousStocks = stockService.getAllStocks();
            if (tousStocks.isEmpty()) {
                showAlert("Information", "Aucune donnée", "Aucun stock disponible pour l'analyse.");
                return;
            }

            StringBuilder resultat = new StringBuilder("📈 ANALYSE PRÉVISIONNELLE\n\n");

            for (Stock stock : tousStocks) {
                StockForecastingAnalyst.RapportPrevision prevision = forecastingAnalyst.analyserVitesseEcoulement(stock);

                resultat.append(String.format(
                        "Produit: %s\n" +
                                "Stock actuel: %.2f %s\n" +
                                "Jours avant rupture: %.1f\n" +
                                "Date critique: %s\n" +
                                "Niveau risque: %s\n" +
                                "Recommandation: %s\n\n",
                        prevision.getNomProduit(),
                        prevision.getStockActuel(),
                        prevision.getUnite(),
                        prevision.getJoursAvantRupture(),
                        prevision.getDateCritique(),
                        prevision.getNiveauRisque(),
                        prevision.getRecommendation()
                ));
            }

            // Ajouter les recommandations d'achat
            List<StockForecastingAnalyst.RecommandationAchat> recommandations = forecastingAnalyst.genererRecommandationsAchat();
            if (!recommandations.isEmpty()) {
                resultat.append("🛒 RECOMMANDATIONS D'ACHAT\n\n");
                for (StockForecastingAnalyst.RecommandationAchat rec : recommandations) {
                    resultat.append(String.format(
                            "%s: Commander %.2f %s (stock actuel: %.2f, priorité: %s)\n",
                            rec.getProduit(),
                            rec.getQuantiteRecommande(),
                            rec.getUnite(),
                            rec.getStockActuel(),
                            rec.getPriorite()
                    ));
                }
            }

            showAlert("Analyse Prévisionnelle", "Résultats", resultat.toString());

        } catch (Exception e) {
            showAlert("Erreur", "Erreur analyse", e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== INTÉGRATIONS API ====================

    @FXML
    private void handleGoogleSync() {
        try {
            System.out.println("\n=== SYNCHRONISATION GOOGLE MERCHANT ===");

            // Récupérer toutes les annonces et stocks
            List<Marketplace> annonces = marketplaceList;
            List<Stock> stocks = stockService.getAllStocks();

            if (annonces.isEmpty()) {
                showAlert("Information", "Aucune donnée",
                        "Aucune annonce à synchroniser.");
                return;
            }

            // Activer le mode test pour éviter les vrais appels API
            googleMerchantClient.setUseMockMode(true);

            // Effectuer la synchronisation
            GoogleMerchantClient.SyncResult result = googleMerchantClient.synchronizeAllMarketplaces(annonces, stocks);

            // Afficher le résultat
            String message = String.format(
                    "📊 RÉSULTAT SYNCHRONISATION\n\n" +
                            "Total: %d annonces\n" +
                            "✅ Succès: %d\n" +
                            "❌ Échecs: %d\n" +
                            "Taux de réussite: %.1f%%\n\n",
                    result.getTotalItems(),
                    result.getSuccessCount(),
                    result.getFailedCount(),
                    result.getSuccessRate()
            );

            if (!result.getErrors().isEmpty()) {
                message += "⚠️ Erreurs rencontrées:\n";
                for (String error : result.getErrors()) {
                    message += "  - " + error + "\n";
                }
            }

            showAlert("Synchronisation Google Merchant", "Résultat", message);

        } catch (Exception e) {
            System.err.println("❌ Erreur synchronisation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur synchronisation", e.getMessage());
        }
    }

    @FXML
    private void handleInvenTreeSync() {
        try {
            System.out.println("\n=== SYNCHRONISATION INVENTREE ===");

            // Créer le client avec un token (à configurer)
            InvenTreeClient invenTreeClient = new InvenTreeClient("votre-token-ici");

            // Authentifier (si nécessaire)
            boolean auth = invenTreeClient.authenticate("admin", "password");

            if (!auth) {
                showAlert("Erreur", "Authentification échouée",
                        "Impossible de se connecter à InvenTree");
                return;
            }

            // Récupérer tous les stocks
            List<Stock> stocks = stockService.getAllStocks();

            // Synchroniser
            InvenTreeClient.SyncResult result = invenTreeClient.synchronizeAllStocks(stocks);

            showAlert("Synchronisation InvenTree", "Résultat", result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur synchronisation", e.getMessage());
        }
    }

    // ==================== MÉTHODES DE NAVIGATION ====================

    @FXML
    private void handleDashboard() {
        loadViewInContentPane("dashboard_marketplace.fxml", "Dashboard Marketplace");
    }

    @FXML
    private void handleStocks() {
        loadViewInContentPane("gestion_stock.fxml", "Gestion des Stocks");
    }

    @FXML
    private void handleMarketplace() {
        chargerMarketplaces();
    }

    @FXML
    private void handleStats() {
        loadViewInContentPane("stats_marketplace.fxml", "Statistiques Marketplace");
    }

    @FXML
    private void handleParametres() {
        showAlert("Paramètres", "Fonctionnalité à venir", "La gestion des paramètres sera disponible prochainement.");
    }

    @FXML
    private void handleExporterPDF() {
        showAlert("Export PDF", "Fonctionnalité à venir", "L'export PDF sera disponible prochainement.");
    }

    private void showAlert(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

}