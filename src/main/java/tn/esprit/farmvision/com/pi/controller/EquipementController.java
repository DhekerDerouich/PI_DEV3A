package com.pi.controller;

import com.pi.model.Equipement;
import com.pi.model.Maintenance;
import com.pi.service.EquipementService;
import com.pi.service.MaintenanceService;
import com.pi.service.QRCodeService;
import com.pi.service.external.CO2SignalService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class EquipementController {

    @FXML private TableView<Equipement> equipementTable;
    @FXML private TableColumn<Equipement, String> colNom;
    @FXML private TableColumn<Equipement, String> colType;
    @FXML private TableColumn<Equipement, String> colEtat;
    @FXML private TableColumn<Equipement, String> colDateAchat;
    @FXML private TableColumn<Equipement, Integer> colDureeVie;
    @FXML private TableColumn<Equipement, String> colFinGarantie;
    @FXML private TableColumn<Equipement, String> colAge;
    @FXML private TableColumn<Equipement, String> colEmpreinteCarbone; // Nouvelle colonne
    @FXML private TableColumn<Equipement, String> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterEtat;
    @FXML private ComboBox<String> filterType;
    @FXML private Label totalLabel;
    @FXML private Label fonctionnelLabel;
    @FXML private Label panneLabel;
    @FXML private Label maintenanceLabel;
    @FXML private Label garantieLabel;
    @FXML private Label empreinteCarboneLabel; // Nouveau label pour l'empreinte totale
    @FXML private GridPane statsGrid; // Pour ajouter la carte CO2

    private final EquipementService service = new EquipementService();
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final ObservableList<Equipement> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Nouveau service CO2
    private CO2SignalService co2Service = new CO2SignalService();
    private String countryCode = "TN"; // Tunisie par défaut

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadData();

        // Ajouter la carte d'empreinte carbone
        ajouterCarteEmpreinteCarbone();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        colType.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        colEtat.setCellValueFactory(cellData -> cellData.getValue().etatProperty());

        colDateAchat.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateAchat() != null ?
                        cellData.getValue().getDateAchat().format(dateFormatter) : ""));

        colDureeVie.setCellValueFactory(cellData -> cellData.getValue().dureeVieEstimeeProperty().asObject());

        // Colonne Fin de garantie (date d'achat + durée de vie)
        colFinGarantie.setCellValueFactory(cellData -> {
            LocalDate dateAchat = cellData.getValue().getDateAchat();
            int dureeVie = cellData.getValue().getDureeVieEstimee();
            if (dateAchat != null) {
                LocalDate finGarantie = dateAchat.plusYears(dureeVie);
                return new SimpleStringProperty(finGarantie.format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });

        // Colonne Âge de l'équipement
        colAge.setCellValueFactory(cellData -> {
            LocalDate dateAchat = cellData.getValue().getDateAchat();
            if (dateAchat != null) {
                long age = java.time.temporal.ChronoUnit.YEARS.between(dateAchat, LocalDate.now());
                return new SimpleStringProperty(age + " an(s)");
            }
            return new SimpleStringProperty("-");
        });

        // NOUVELLE COLONNE : Empreinte carbone estimée
        colEmpreinteCarbone.setCellValueFactory(cellData -> {
            Equipement e = cellData.getValue();
            double conso = co2Service.getConsommationEstimee(e.getType());
            double intensite = co2Service.getCarbonIntensity(countryCode);
            // Estimation: 100 heures d'utilisation par an
            double emissionsAnnuelles = conso * 100 * intensite;

            String couleur;
            if (emissionsAnnuelles < 500) {
                couleur = "#2ecc71"; // Vert
            } else if (emissionsAnnuelles < 1000) {
                couleur = "#f39c12"; // Orange
            } else {
                couleur = "#e74c3c"; // Rouge
            }

            Label label = new Label(String.format("%.0f kg CO₂/an", emissionsAnnuelles));
            label.setStyle("-fx-text-fill: " + couleur + "; -fx-font-weight: bold;");

            return new SimpleStringProperty(label.getText());
        });

        // Style pour l'état avec couleurs
        colEtat.setCellFactory(column -> new TableCell<Equipement, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(etat);
                    switch (etat) {
                        case "Fonctionnel":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "En panne":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "Maintenance":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Style pour la fin de garantie (rouge si dépassée)
        colFinGarantie.setCellFactory(column -> new TableCell<Equipement, String>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null || date.equals("-")) {
                    setText(date);
                    setStyle("");
                } else {
                    setText(date);
                    try {
                        LocalDate finGarantie = LocalDate.parse(date, dateFormatter);
                        if (finGarantie.isBefore(LocalDate.now())) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #27ae60;");
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        // Colonne Actions avec boutons
        colActions.setCellFactory(column -> new TableCell<Equipement, String>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button maintenanceBtn = new Button("🔧");
            private final Button qrBtn = new Button("📷");
            private final Button co2Btn = new Button("🌱"); // Nouveau bouton CO2

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                maintenanceBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                qrBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand;");
                co2Btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");

                maintenanceBtn.setTooltip(new Tooltip("Planifier une maintenance"));
                qrBtn.setTooltip(new Tooltip("Générer QR Code"));
                co2Btn.setTooltip(new Tooltip("Voir l'empreinte carbone"));

                editBtn.setOnAction(event -> {
                    Equipement equip = getTableView().getItems().get(getIndex());
                    showEditDialog(equip);
                });

                deleteBtn.setOnAction(event -> {
                    Equipement equip = getTableView().getItems().get(getIndex());
                    handleDelete(equip);
                });

                maintenanceBtn.setOnAction(event -> {
                    Equipement equip = getTableView().getItems().get(getIndex());
                    planifierMaintenance(equip);
                });

                qrBtn.setOnAction(event -> {
                    Equipement equip = getTableView().getItems().get(getIndex());
                    showQRCodeDialog(equip);
                });

                co2Btn.setOnAction(event -> {
                    Equipement equip = getTableView().getItems().get(getIndex());
                    afficherEmpreinteCarbone(equip);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, editBtn, maintenanceBtn, qrBtn, co2Btn, deleteBtn));
                }
            }
        });

        equipementTable.setItems(data);
    }

    private void setupFilters() {
        // Filtre par état
        filterEtat.getItems().addAll("Tous", "Fonctionnel", "En panne", "Maintenance");
        filterEtat.setValue("Tous");

        // Filtre par type (extraire les types uniques)
        filterType.getItems().add("Tous");
        service.getAllEquipements().stream()
                .map(Equipement::getType)
                .distinct()
                .sorted()
                .forEach(filterType.getItems()::add);
        filterType.setValue("Tous");

        // Listeners
        searchField.textProperty().addListener((obs, old, val) -> filterData());
        filterEtat.valueProperty().addListener((obs, old, val) -> filterData());
        filterType.valueProperty().addListener((obs, old, val) -> filterData());
    }

    private void filterData() {
        String search = searchField.getText().toLowerCase();
        String etat = filterEtat.getValue();
        String type = filterType.getValue();

        ObservableList<Equipement> filtered = FXCollections.observableArrayList();

        for (Equipement e : service.getAllEquipements()) {
            boolean matchSearch = search.isEmpty() ||
                    e.getNom().toLowerCase().contains(search) ||
                    e.getType().toLowerCase().contains(search) ||
                    String.valueOf(e.getId()).contains(search);

            boolean matchEtat = etat.equals("Tous") || e.getEtat().equals(etat);
            boolean matchType = type.equals("Tous") || e.getType().equals(type);

            if (matchSearch && matchEtat && matchType) {
                filtered.add(e);
            }
        }
        data.setAll(filtered);
        updateStats();
    }

    private void loadData() {
        data.setAll(service.getAllEquipements());
        updateStats();
    }

    @FXML
    private void refreshTable() {
        loadData();
        filterData();
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        filterEtat.setValue("Tous");
        filterType.setValue("Tous");
    }

    private void updateStats() {
        int total = data.size();
        int fonctionnels = (int) data.stream().filter(e -> "Fonctionnel".equals(e.getEtat())).count();
        int enPanne = (int) data.stream().filter(e -> "En panne".equals(e.getEtat())).count();
        int enMaintenance = (int) data.stream().filter(e -> "Maintenance".equals(e.getEtat())).count();
        int sousGarantie = (int) data.stream().filter(e -> {
            if (e.getDateAchat() == null) return false;
            LocalDate finGarantie = e.getDateAchat().plusYears(e.getDureeVieEstimee());
            return finGarantie.isAfter(LocalDate.now());
        }).count();

        totalLabel.setText(String.valueOf(total));
        fonctionnelLabel.setText(String.valueOf(fonctionnels));
        panneLabel.setText(String.valueOf(enPanne));
        maintenanceLabel.setText(String.valueOf(enMaintenance));
        garantieLabel.setText(String.valueOf(sousGarantie));

        // Mettre à jour l'empreinte carbone
        mettreAJourEmpreinteCarbone();
    }

    /**
     * Ajoute une carte d'empreinte carbone au tableau de bord
     */
    private void ajouterCarteEmpreinteCarbone() {
        if (statsGrid != null) {
            VBox co2Card = new VBox(10);
            co2Card.setAlignment(javafx.geometry.Pos.CENTER);
            co2Card.setStyle("-fx-background-color: linear-gradient(135deg, #27ae60, #2ecc71); -fx-background-radius: 14; -fx-padding: 22 18; -fx-effect: dropshadow(gaussian, rgba(39,174,96,0.4), 12, 0, 0, 4); -fx-min-width: 220;");

            Label icon = new Label("🌱");
            icon.setStyle("-fx-font-size: 32px;");

            empreinteCarboneLabel = new Label("0 kg CO₂");
            empreinteCarboneLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

            Label label = new Label("Empreinte carbone (mois)");
            label.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.95); -fx-font-weight: 600;");

            co2Card.getChildren().addAll(icon, empreinteCarboneLabel, label);

            // Ajouter à la 5ème position (après les 4 cartes existantes)
            statsGrid.add(co2Card, 4, 0);
        }
    }

    /**
     * Calcule et met à jour l'empreinte carbone totale
     */
    private void mettreAJourEmpreinteCarbone() {
        List<Equipement> equipements = service.getAllEquipements();
        List<Maintenance> maintenances = maintenanceService.getAllMaintenances();

        double emissions = co2Service.calculerEmissionsMensuelles(maintenances, equipements, countryCode);

        if (empreinteCarboneLabel != null) {
            empreinteCarboneLabel.setText(String.format("%.0f kg CO₂", emissions));

            // Changer la couleur selon le niveau
            if (emissions > 200) {
                empreinteCarboneLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffcccc;");
            } else if (emissions > 100) {
                empreinteCarboneLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffcc;");
            }
        }

        // Vérifier et créer une alerte si nécessaire
        co2Service.verifierEtAlerter(new com.pi.service.AlertesService(), emissions);
    }

    /**
     * Affiche les détails de l'empreinte carbone d'un équipement
     */
    private void afficherEmpreinteCarbone(Equipement equipement) {
        double conso = co2Service.getConsommationEstimee(equipement.getType());
        double intensite = co2Service.getCarbonIntensity(countryCode);

        // Calculs pour différentes durées
        double parHeure = conso * intensite;
        double parJour = parHeure * 8; // 8h de travail
        double parMois = parJour * 22; // 22 jours ouvrés
        double parAn = parMois * 12;

        String message = String.format(
                "🌱 EMPREINTE CARBONE - %s\n\n" +
                        "Type d'équipement: %s\n" +
                        "Consommation estimée: %.1f kWh/heure\n" +
                        "Intensité carbone (pays %s): %.3f kg CO₂/kWh\n\n" +
                        "📊 Émissions estimées:\n" +
                        "• Par heure: %.2f kg CO₂\n" +
                        "• Par jour (8h): %.2f kg CO₂\n" +
                        "• Par mois: %.2f kg CO₂\n" +
                        "• Par an: %.2f kg CO₂\n\n" +
                        "🌍 Équivalent en km parcourus en voiture:\n" +
                        "• ~%.0f km/an",
                equipement.getNom(),
                equipement.getType(),
                conso,
                countryCode,
                intensite,
                parHeure,
                parJour,
                parMois,
                parAn,
                parAn * 5 // 1 kg CO₂ ≈ 5 km en voiture
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Empreinte Carbone");
        alert.setHeaderText("🌱 " + equipement.getNom());
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void showAddDialog() {
        Dialog<Equipement> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un équipement");
        dialog.setHeaderText("Nouvel équipement agricole");

        ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom de l'équipement");

        TextField typeField = new TextField();
        typeField.setPromptText("Type (Tracteur, Moissonneuse, etc.)");

        ComboBox<String> etatBox = new ComboBox<>();
        etatBox.getItems().addAll("Fonctionnel", "En panne", "Maintenance");
        etatBox.setValue("Fonctionnel");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPromptText("Date d'achat");

        TextField dureeField = new TextField();
        dureeField.setPromptText("Durée de vie (années)");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(new Label("État:"), 0, 2);
        grid.add(etatBox, 1, 2);
        grid.add(new Label("Date d'achat:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Durée de vie:"), 0, 4);
        grid.add(dureeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (nomField.getText().trim().isEmpty()) {
                        showError("Erreur", "Le nom est obligatoire");
                        return null;
                    }
                    if (typeField.getText().trim().isEmpty()) {
                        showError("Erreur", "Le type est obligatoire");
                        return null;
                    }

                    int dureeVie;
                    try {
                        dureeVie = Integer.parseInt(dureeField.getText().trim());
                        if (dureeVie <= 0) {
                            showError("Erreur", "La durée de vie doit être positive");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError("Erreur", "La durée de vie doit être un nombre valide");
                        return null;
                    }

                    return service.addEquipement(
                            nomField.getText().trim(),
                            typeField.getText().trim(),
                            etatBox.getValue(),
                            datePicker.getValue(),
                            dureeVie,
                            null // Pas de parcelle
                    );
                } catch (Exception e) {
                    showError("Erreur", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Equipement> result = dialog.showAndWait();
        result.ifPresent(equip -> {
            showInfo("Succès", "Équipement ajouté avec ID: " + equip.getId());
            refreshTable();
        });
    }

    private void showEditDialog(Equipement equipement) {
        Dialog<Equipement> dialog = new Dialog<>();
        dialog.setTitle("Modifier équipement");
        dialog.setHeaderText("Modification: " + equipement.getNom());

        ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField(equipement.getNom());
        TextField typeField = new TextField(equipement.getType());

        ComboBox<String> etatBox = new ComboBox<>();
        etatBox.getItems().addAll("Fonctionnel", "En panne", "Maintenance");
        etatBox.setValue(equipement.getEtat());

        DatePicker datePicker = new DatePicker(equipement.getDateAchat());
        TextField dureeField = new TextField(String.valueOf(equipement.getDureeVieEstimee()));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(new Label("État:"), 0, 2);
        grid.add(etatBox, 1, 2);
        grid.add(new Label("Date d'achat:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Durée de vie:"), 0, 4);
        grid.add(dureeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int dureeVie;
                    try {
                        dureeVie = Integer.parseInt(dureeField.getText().trim());
                    } catch (NumberFormatException e) {
                        showError("Erreur", "La durée de vie doit être un nombre valide");
                        return null;
                    }

                    return service.updateEquipement(
                            equipement.getId(),
                            nomField.getText(),
                            typeField.getText(),
                            etatBox.getValue(),
                            datePicker.getValue(),
                            dureeVie,
                            null // Pas de parcelle
                    );
                } catch (Exception e) {
                    showError("Erreur", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Equipement> result = dialog.showAndWait();
        result.ifPresent(equip -> {
            showInfo("Succès", "Équipement modifié !");
            refreshTable();
        });
    }

    private void planifierMaintenance(Equipement equipement) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Planifier une maintenance");
            dialog.setHeaderText("Maintenance pour: " + equipement.getNom());

            ButtonType planifierButton = new ButtonType("Planifier", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(planifierButton, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            ComboBox<String> typeBox = new ComboBox<>();
            typeBox.getItems().addAll("Préventive", "Corrective");
            typeBox.setValue("Préventive");

            TextField descriptionField = new TextField();
            descriptionField.setPromptText("Description de la maintenance");

            DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));

            TextField coutField = new TextField();
            coutField.setPromptText("Coût estimé (DT)");

            grid.add(new Label("Type:"), 0, 0);
            grid.add(typeBox, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionField, 1, 1);
            grid.add(new Label("Date:"), 0, 2);
            grid.add(datePicker, 1, 2);
            grid.add(new Label("Coût:"), 0, 3);
            grid.add(coutField, 1, 3);

            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == planifierButton) {
                if (descriptionField.getText().trim().isEmpty()) {
                    showError("Erreur", "La description est obligatoire");
                    return;
                }

                double cout;
                try {
                    cout = Double.parseDouble(coutField.getText().trim());
                    if (cout < 0) {
                        showError("Erreur", "Le coût ne peut pas être négatif");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showError("Erreur", "Le coût doit être un nombre valide");
                    return;
                }

                com.pi.service.MaintenanceService maintenanceService = new com.pi.service.MaintenanceService();
                maintenanceService.planifierMaintenance(
                        equipement.getId(),
                        typeBox.getValue(),
                        descriptionField.getText().trim(),
                        datePicker.getValue(),
                        cout,
                        "Planifiée"
                );
                showInfo("Succès", "Maintenance planifiée !");
            }

        } catch (Exception e) {
            showError("Erreur", "Impossible de planifier: " + e.getMessage());
        }
    }

    private void handleDelete(Equipement equipement) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + equipement.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible. " +
                "Toutes les maintenances associées seront également supprimées.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.deleteEquipement(equipement.getId());
                    refreshTable();
                    showInfo("Succès", "Équipement supprimé !");
                } catch (Exception e) {
                    showError("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void ouvrirCentreAlertes() {
        try {
            // Essayer différents chemins possibles
            FXMLLoader loader = null;
            String[] chemins = {
                    "/com/pi/view/AlertesView.fxml",
                    "/tn/esprit/farmvision/com/pi/view/AlertesView.fxml",
                    "/AlertesView.fxml"
            };

            for (String chemin : chemins) {
                URL url = getClass().getResource(chemin);
                if (url != null) {
                    loader = new FXMLLoader(url);
                    break;
                }
            }

            if (loader == null) {
                showError("Erreur", "Fichier AlertesView.fxml introuvable");
                return;
            }

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("🔔 Centre de notifications");
            stage.setScene(new Scene(root, 500, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir les alertes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirCalendrierMaintenance() {
        try {
            // Essayer différents chemins possibles
            FXMLLoader loader = null;
            String[] chemins = {
                    "/com/pi/view/CalendrierMaintenance.fxml",
                    "/tn/esprit/farmvision/com/pi/view/CalendrierMaintenance.fxml",
                    "/CalendrierMaintenance.fxml"
            };

            for (String chemin : chemins) {
                URL url = getClass().getResource(chemin);
                if (url != null) {
                    loader = new FXMLLoader(url);
                    break;
                }
            }

            if (loader == null) {
                showError("Erreur", "Fichier CalendrierMaintenance.fxml introuvable");
                return;
            }

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📅 Calendrier interactif des maintenances");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToExcel() {
        showInfo("Info", "Export Excel à venir...");
    }

    @FXML
    private void printList() {
        showInfo("Info", "Impression à venir...");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void afficherQRCode() {
        Equipement selected = equipementTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un équipement");
            return;
        }

        showQRCodeDialog(selected);
    }

    private void showQRCodeDialog(Equipement equipement) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("QR Code - " + equipement.getNom());
        dialog.setHeaderText("Code QR pour " + equipement.getNom());

        ButtonType fermerButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType imprimerButton = new ButtonType("🖨️ Imprimer");
        ButtonType sauvegarderButton = new ButtonType("💾 Sauvegarder");
        ButtonType copierButton = new ButtonType("📋 Copier");

        dialog.getDialogPane().getButtonTypes().addAll(
                sauvegarderButton, imprimerButton, copierButton, fermerButton
        );

        QRCodeService qrService = new QRCodeService();
        ImageView qrView = qrService.creerImageViewQR(equipement, 300);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(javafx.geometry.Pos.CENTER);

        if (qrView != null) {
            content.getChildren().add(qrView);
        }

        // Informations de l'équipement
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(5);
        infoGrid.setAlignment(javafx.geometry.Pos.CENTER);

        infoGrid.add(new Label("ID:"), 0, 0);
        infoGrid.add(new Label(String.valueOf(equipement.getId())), 1, 0);
        infoGrid.add(new Label("Nom:"), 0, 1);
        infoGrid.add(new Label(equipement.getNom()), 1, 1);
        infoGrid.add(new Label("Type:"), 0, 2);
        infoGrid.add(new Label(equipement.getType()), 1, 2);
        infoGrid.add(new Label("État:"), 0, 3);

        Label etatLabel = new Label(equipement.getEtat());
        switch (equipement.getEtat()) {
            case "Fonctionnel": etatLabel.setStyle("-fx-text-fill: #2ecc71;"); break;
            case "En panne": etatLabel.setStyle("-fx-text-fill: #e74c3c;"); break;
            case "Maintenance": etatLabel.setStyle("-fx-text-fill: #f39c12;"); break;
        }
        infoGrid.add(etatLabel, 1, 3);

        // Ajouter l'empreinte carbone
        double conso = co2Service.getConsommationEstimee(equipement.getType());
        double intensite = co2Service.getCarbonIntensity(countryCode);
        double parAn = conso * 100 * intensite;

        infoGrid.add(new Label("CO₂/an:"), 0, 4);
        Label co2Label = new Label(String.format("%.0f kg", parAn));
        co2Label.setStyle("-fx-text-fill: #27ae60;");
        infoGrid.add(co2Label, 1, 4);

        content.getChildren().add(infoGrid);

        // Instructions
        Label instruction = new Label("Scannez ce code pour accéder rapidement aux informations de l'équipement");
        instruction.setWrapText(true);
        instruction.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        content.getChildren().add(instruction);

        dialog.getDialogPane().setContent(content);

        // Gestion des boutons
        dialog.setResultConverter(button -> {
            if (button == sauvegarderButton) {
                sauvegarderQRCode(equipement);
            } else if (button == imprimerButton) {
                imprimerQRCode(qrView.getImage());
            } else if (button == copierButton) {
                copierQRCode(qrView.getImage());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void sauvegarderQRCode(Equipement equipement) {
        QRCodeService qrService = new QRCodeService();
        qrService.sauvegarderQRCode(equipement);
        showInfo("Succès", "QR Code sauvegardé dans le dossier 'qr_codes'");
    }

    private void imprimerQRCode(Image image) {
        showInfo("Info", "Fonctionnalité d'impression à venir");
    }

    private void copierQRCode(Image image) {
        showInfo("Info", "Fonctionnalité de copie à venir");
    }
}