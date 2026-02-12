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
import tn.esprit.farmvision.gestionstock.model.Marketplace;
import tn.esprit.farmvision.gestionstock.service.MarketplaceService;
import java.io.IOException;
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

    private MarketplaceService marketplaceService;
    private ObservableList<Marketplace> marketplaceList;

    @FXML
    public void initialize() {
        System.out.println("\n=== INITIALISATION MARKETPLACE CONTROLLER ===");

        marketplaceService = new MarketplaceService();
        marketplaceList = FXCollections.observableArrayList();

        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idMarketplace"));
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
                            setStyle("-fx-text-fill: red;");
                            break;
                        case "Réservé":
                            setStyle("-fx-text-fill: orange;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Filtre statut
        comboFiltreStatut.getItems().addAll("Tous", "En vente", "Vendu", "Réservé");
        comboFiltreStatut.setValue("Tous");

        chargerMarketplaces();

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> filtrerMarketplaces());
        comboFiltreStatut.valueProperty().addListener((observable, oldValue, newValue) -> filtrerMarketplaces());

        System.out.println("✅ Initialisation terminée\n");
    }

    private void chargerMarketplaces() {
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

            System.out.println("✅ Affichage mis à jour\n");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des annonces", e.getMessage());
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

    @FXML
    private void handleAjouterAnnonce() {
        try {
            System.out.println("\n=== OUVERTURE FENÊTRE AJOUT ANNONCE ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouter_marketplace.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une annonce");
            stage.setScene(new Scene(root, 500, 450));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des annonces...");
            chargerMarketplaces();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage());
            e.printStackTrace();
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
            System.out.println("Annonce sélectionnée: ID=" + annonceSelectionnee.getIdMarketplace() +
                    ", Produit=" + annonceSelectionnee.getNomProduit());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_marketplace.fxml"));
            Parent root = loader.load();
            ModifierMarketplaceController controller = loader.getController();
            controller.setMarketplace(annonceSelectionnee);
            Stage stage = new Stage();
            stage.setTitle("Modifier l'annonce");
            stage.setScene(new Scene(root, 500, 450));
            stage.showAndWait();

            System.out.println("🔄 Fenêtre fermée - Rechargement des annonces...");
            chargerMarketplaces();

        } catch (IOException e) {
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
                System.out.println("Suppression de l'annonce ID: " + annonceSelectionnee.getIdMarketplace());

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

        // Boîte de dialogue pour saisir la quantité
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
        details.append("👤 Vendeur: ").append(annonceSelectionnee.getNomVendeur()).append("\n");
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

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_stock.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("FarmVision - Gestion Stock");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
            ((Stage) marketplaceTable.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String entete, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}