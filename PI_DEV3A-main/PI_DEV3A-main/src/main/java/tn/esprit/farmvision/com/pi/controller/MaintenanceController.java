package tn.esprit.farmvision.com.pi.controller;

import tn.esprit.farmvision.com.pi.model.Maintenance;
import tn.esprit.farmvision.com.pi.model.Equipement;
import tn.esprit.farmvision.com.pi.service.MaintenanceService;
import tn.esprit.farmvision.com.pi.service.EquipementService;
import tn.esprit.farmvision.com.pi.service.external.SunriseSunsetService;
import tn.esprit.farmvision.com.pi.service.external.SunriseSunsetService.SunriseSunsetData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class MaintenanceController {

    @FXML private TableView<Maintenance> maintenanceTable;
    @FXML private TableColumn<Maintenance, String> colEquipementNom;
    @FXML private TableColumn<Maintenance, String> colType;
    @FXML private TableColumn<Maintenance, String> colDescription;
    @FXML private TableColumn<Maintenance, String> colDate;
    @FXML private TableColumn<Maintenance, Double> colCout;
    @FXML private TableColumn<Maintenance, String> colStatut;
    @FXML private TableColumn<Maintenance, String> colMeilleurePeriode;
    @FXML private TableColumn<Maintenance, String> colActions;

    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<Integer> filterEquipement;
    @FXML private Label totalLabel;
    @FXML private Label planifieesLabel;
    @FXML private Label realiseesLabel;
    @FXML private Label coutTotalLabel;
    @FXML private Label infoSolaireLabel;

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final EquipementService equipementService = new EquipementService();
    private final ObservableList<Maintenance> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private SunriseSunsetService sunriseService = new SunriseSunsetService();
    private LocalDate datePreselectionnee = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadData();
        mettreAJourInfosSolaires();

        // Ajuster la largeur de la colonne Actions pour correspondre au FXML
        colActions.setPrefWidth(290);
        colActions.setMinWidth(290);
        colActions.setMaxWidth(290);
    }

    private void setupTableColumns() {
        colEquipementNom.setCellValueFactory(cellData -> {
            try {
                int equipId = cellData.getValue().getEquipementId();
                Equipement equip = equipementService.getEquipementById(equipId);
                return new SimpleStringProperty(equip != null ? equip.getNom() : "ID: " + equipId);
            } catch (Exception e) {
                return new SimpleStringProperty("Inconnu");
            }
        });

        colType.setCellValueFactory(cellData -> cellData.getValue().typeMaintenanceProperty());
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDateMaintenance() != null ?
                        cellData.getValue().getDateMaintenance().format(dateFormatter) : ""));

        colCout.setCellValueFactory(cellData -> cellData.getValue().coutProperty().asObject());
        colStatut.setCellValueFactory(cellData -> cellData.getValue().statutProperty());

        colMeilleurePeriode.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateMaintenance();
            String periode = sunriseService.getRecommendedWorkHours(date);
            return new SimpleStringProperty(periode);
        });

        colMeilleurePeriode.setCellFactory(column -> new TableCell<Maintenance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    setTooltip(new Tooltip("Meilleure période pour les travaux extérieurs"));
                }
            }
        });

        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Planifiée".equals(item)) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colCout.setCellFactory(column -> new TableCell<Maintenance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item));
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        // COLONNE ACTIONS - AVEC BOUTONS VISIBLES ET CLASSES CSS
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("📝");
            private final Button deleteBtn = new Button("❌");
            private final Button completeBtn = new Button("✓");
            private final Button calendarBtn = new Button("📅");
            private final Button soleilBtn = new Button("🌞");
            private final HBox buttonsContainer = new HBox(5);

            {
                // Style commun pour tous les boutons
                String buttonStyle = "-fx-background-radius: 4; -fx-padding: 6 8; -fx-cursor: hand; " +
                        "-fx-font-size: 14px; -fx-min-width: 36px; -fx-min-height: 32px; " +
                        "-fx-max-width: 36px; -fx-max-height: 32px; " +
                        "-fx-background-insets: 0; -fx-border-insets: 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);";

                editBtn.setStyle(buttonStyle + " -fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle(buttonStyle + " -fx-background-color: #e74c3c; -fx-text-fill: white;");
                completeBtn.setStyle(buttonStyle + " -fx-background-color: #2ecc71; -fx-text-fill: white;");
                calendarBtn.setStyle(buttonStyle + " -fx-background-color: #3498db; -fx-text-fill: white;");
                soleilBtn.setStyle(buttonStyle + " -fx-background-color: #f39c12; -fx-text-fill: white;");

                // AJOUT DES CLASSES CSS
                editBtn.getStyleClass().addAll("button-action", "button-edit");
                deleteBtn.getStyleClass().addAll("button-action", "button-delete");
                completeBtn.getStyleClass().addAll("button-action", "button-complete");
                calendarBtn.getStyleClass().addAll("button-action", "button-calendar");
                soleilBtn.getStyleClass().addAll("button-action", "button-sun");

                // Tooltips
                editBtn.setTooltip(new Tooltip("Modifier la maintenance"));
                deleteBtn.setTooltip(new Tooltip("Supprimer la maintenance"));
                completeBtn.setTooltip(new Tooltip("Marquer comme réalisée"));
                calendarBtn.setTooltip(new Tooltip("Voir dans le calendrier"));
                soleilBtn.setTooltip(new Tooltip("Voir les informations solaires"));

                // Actions
                editBtn.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    showEditDialog(m);
                });

                deleteBtn.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    handleDelete(m);
                });

                completeBtn.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    markAsCompleted(m);
                });

                calendarBtn.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    ouvrirCalendrierAvecDate(m.getDateMaintenance());
                });

                soleilBtn.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    afficherInfosSolaires(m);
                });

                // Configuration du conteneur
                buttonsContainer.setAlignment(Pos.CENTER);
                buttonsContainer.setSpacing(4);
                buttonsContainer.setPadding(new Insets(2, 0, 2, 0));
                buttonsContainer.setMaxWidth(Double.MAX_VALUE);
                buttonsContainer.setPrefWidth(280);
                buttonsContainer.getStyleClass().add("action-buttons-container");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    buttonsContainer.getChildren().clear();

                    // Ajouter les boutons selon le statut
                    buttonsContainer.getChildren().addAll(editBtn, deleteBtn, calendarBtn, soleilBtn);
                    if ("Planifiée".equals(m.getStatut())) {
                        buttonsContainer.getChildren().add(completeBtn);
                    }

                    setGraphic(buttonsContainer);
                }
            }
        });

        maintenanceTable.setItems(data);
    }

    private void setupFilters() {
        filterStatut.getItems().addAll("Tous", "Planifiée", "Réalisée");
        filterStatut.setValue("Tous");

        refreshEquipementFilter();

        filterStatut.valueProperty().addListener((obs, old, val) -> filterData());
        filterEquipement.valueProperty().addListener((obs, old, val) -> filterData());
    }

    private void refreshEquipementFilter() {
        filterEquipement.getItems().clear();
        filterEquipement.getItems().add(0);
        filterEquipement.getItems().addAll(
                equipementService.getAllEquipements().stream()
                        .map(Equipement::getId)
                        .sorted()
                        .collect(Collectors.toList())
        );
        filterEquipement.setValue(0);

        filterEquipement.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else if (id == 0) {
                    setText("Tous les équipements");
                } else {
                    try {
                        Equipement e = equipementService.getEquipementById(id);
                        setText(e != null ? e.getNom() : "ID: " + id);
                    } catch (Exception e) {
                        setText("ID: " + id);
                    }
                }
            }
        });

        filterEquipement.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else if (id == 0) {
                    setText("Tous les équipements");
                } else {
                    try {
                        Equipement e = equipementService.getEquipementById(id);
                        setText(e != null ? e.getNom() : "ID: " + id);
                    } catch (Exception e) {
                        setText("ID: " + id);
                    }
                }
            }
        });
    }

    @FXML
    private void loadData() {
        data.setAll(maintenanceService.getAllMaintenances());
        updateStats();
    }

    @FXML
    public void refreshTable() {
        loadData();
        refreshEquipementFilter();
    }

    @FXML
    public void clearFilters() {
        filterStatut.setValue("Tous");
        filterEquipement.setValue(0);
        loadData();
    }

    private void filterData() {
        String statut = filterStatut.getValue();
        Integer equipId = filterEquipement.getValue();

        ObservableList<Maintenance> filtered = maintenanceService.getAllMaintenances().stream()
                .filter(m -> statut == null || "Tous".equals(statut) || m.getStatut().equals(statut))
                .filter(m -> equipId == null || equipId == 0 || m.getEquipementId() == equipId)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        data.setAll(filtered);
        updateStats();
    }

    private void updateStats() {
        int total = data.size();
        long planifiees = data.stream().filter(m -> "Planifiée".equals(m.getStatut())).count();
        long realisees = data.stream().filter(m -> "Réalisée".equals(m.getStatut())).count();
        double coutTotal = data.stream().mapToDouble(Maintenance::getCout).sum();

        totalLabel.setText(String.valueOf(total));
        planifieesLabel.setText(String.valueOf(planifiees));
        realiseesLabel.setText(String.valueOf(realisees));
        coutTotalLabel.setText(String.format("%.2f DT", coutTotal));
    }

    private void mettreAJourInfosSolaires() {
        if (infoSolaireLabel != null) {
            SunriseSunsetData data = sunriseService.getSunriseSunsetToday();
            if (data != null) {
                String info = String.format("☀️ Aujourd'hui: Lever %s, Coucher %s, Durée %s",
                        data.getSunrise().format(timeFormatter),
                        data.getSunset().format(timeFormatter),
                        data.getDayLengthFormatted());
                infoSolaireLabel.setText(info);
                infoSolaireLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            } else {
                infoSolaireLabel.setText("☀️ Données solaires temporairement indisponibles");
                infoSolaireLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            }
        }
    }

    private void afficherInfosSolaires(Maintenance maintenance) {
        LocalDate date = maintenance.getDateMaintenance();
        SunriseSunsetData solar = sunriseService.getSunriseSunsetForDate(date);

        String message;
        String title;

        if (solar != null) {
            message = String.format(
                    "☀️ INFORMATIONS SOLAIRES - %s\n\n" +
                            "Date: %s\n" +
                            "Lever du soleil: %s\n" +
                            "Coucher du soleil: %s\n" +
                            "Durée du jour: %s\n" +
                            "Midi solaire: %s\n\n" +
                            "🌞 Meilleure période pour les travaux extérieurs:\n" +
                            "%s\n\n" +
                            "📊 Recommandation: %s",
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    date.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")),
                    solar.getSunrise().format(timeFormatter),
                    solar.getSunset().format(timeFormatter),
                    solar.getDayLengthFormatted(),
                    solar.getSolarNoon() != null ? solar.getSolarNoon().format(timeFormatter) : "12:00",
                    sunriseService.getRecommendedWorkHours(date),
                    getRecommandationSpecifique(date, solar)
            );
            title = "☀️ Informations Solaires";
        } else {
            message = String.format(
                    "☀️ INFORMATIONS SOLAIRES (estimations) - %s\n\n" +
                            "Date: %s\n" +
                            "Lever du soleil estimé: 06:30\n" +
                            "Coucher du soleil estimé: 18:30\n" +
                            "Durée du jour estimée: 12h 00m\n\n" +
                            "🌞 Meilleure période pour les travaux extérieurs:\n" +
                            "08:00 - 17:00\n\n" +
                            "⚠️ Note: Les données sont des estimations (API temporairement indisponible)",
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    date.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy"))
            );
            title = "☀️ Informations Solaires (Estimations)";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("☀️ " + maintenance.getDescription());
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getRecommandationSpecifique(LocalDate date, SunriseSunsetData solar) {
        int month = date.getMonthValue();
        int dayLength = solar.getDayLengthSeconds();

        if (dayLength > 14 * 3600) {
            return "Très longue journée ! Idéal pour les gros travaux extérieurs.";
        } else if (dayLength < 10 * 3600) {
            return "Journée courte. Planifiez les travaux prioritaires le matin.";
        } else if (month >= 6 && month <= 8) {
            return "Attention à la chaleur en été. Travaillez tôt le matin.";
        } else if (month >= 12 || month <= 2) {
            return "Période froide. Prévoyez des pauses régulières.";
        } else {
            return "Conditions favorables pour les travaux extérieurs.";
        }
    }

    public void setDatePreselectionnee(LocalDate date) {
        this.datePreselectionnee = date;
    }

    @FXML
    public void showAddDialog() {
        showMaintenanceDialog(null);
    }

    private void showEditDialog(Maintenance maintenance) {
        showMaintenanceDialog(maintenance);
    }

    private void showMaintenanceDialog(Maintenance maintenance) {
        boolean isEdit = (maintenance != null);
        Dialog<Maintenance> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier la maintenance" : "Planifier une maintenance");
        dialog.setHeaderText(isEdit ? "Modification de la maintenance #" + maintenance.getId()
                : "Nouvelle maintenance");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Equipement> equipementBox = new ComboBox<>();
        equipementBox.getItems().addAll(equipementService.getAllEquipements());
        equipementBox.setConverter(new StringConverter<Equipement>() {
            @Override
            public String toString(Equipement e) {
                return e == null ? "" : e.getId() + " - " + e.getNom();
            }
            @Override
            public Equipement fromString(String string) {
                return null;
            }
        });
        equipementBox.setPromptText("Sélectionner un équipement");
        equipementBox.setPrefWidth(300);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Préventive", "Corrective");
        typeBox.setPromptText("Type de maintenance");
        typeBox.setPrefWidth(200);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description de la maintenance");
        descriptionField.setPrefWidth(300);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date de maintenance");

        Label solarIndicator = new Label("☀️");
        solarIndicator.setTooltip(new Tooltip("Vérifier les heures d'ensoleillement"));
        solarIndicator.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                SunriseSunsetData solar = sunriseService.getSunriseSunsetForDate(newDate);
                if (solar != null) {
                    solarIndicator.setText("☀️ " + solar.getDayLengthFormatted());
                    solarIndicator.setStyle("-fx-text-fill: #27ae60; -fx-cursor: hand; -fx-font-weight: bold;");

                    Tooltip tip = new Tooltip(
                            String.format("Lever: %s, Coucher: %s\nMeilleure période: %s",
                                    solar.getSunrise().format(timeFormatter),
                                    solar.getSunset().format(timeFormatter),
                                    sunriseService.getRecommendedWorkHours(newDate))
                    );
                    Tooltip.install(solarIndicator, tip);
                } else {
                    solarIndicator.setText("☀️ Données indisponibles");
                    solarIndicator.setStyle("-fx-text-fill: #7f8c8d; -fx-cursor: hand;");
                }
            } else {
                solarIndicator.setText("☀️");
                solarIndicator.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");
            }
        });

        HBox dateBox = new HBox(10, datePicker, solarIndicator);

        TextField coutField = new TextField();
        coutField.setPromptText("Coût (DT)");
        coutField.setPrefWidth(150);

        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("Planifiée", "Réalisée");
        statutBox.setPromptText("Statut");
        statutBox.setPrefWidth(150);

        if (isEdit) {
            try {
                Equipement equip = equipementService.getEquipementById(maintenance.getEquipementId());
                equipementBox.setValue(equip);
            } catch (Exception e) {
                e.printStackTrace();
            }
            typeBox.setValue(maintenance.getTypeMaintenance());
            descriptionField.setText(maintenance.getDescription());
            datePicker.setValue(maintenance.getDateMaintenance());
            coutField.setText(String.valueOf(maintenance.getCout()));
            statutBox.setValue(maintenance.getStatut());
        } else {
            typeBox.setValue("Préventive");
            if (datePreselectionnee != null) {
                datePicker.setValue(datePreselectionnee);
            } else {
                datePicker.setValue(LocalDate.now().plusDays(1));
            }
            statutBox.setValue("Planifiée");
        }

        int row = 0;
        grid.add(new Label("Équipement:"), 0, row);
        grid.add(equipementBox, 1, row++);

        grid.add(new Label("Type:"), 0, row);
        grid.add(typeBox, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionField, 1, row++);

        grid.add(new Label("Date:"), 0, row);
        grid.add(dateBox, 1, row++);

        grid.add(new Label("Coût:"), 0, row);
        grid.add(coutField, 1, row++);

        grid.add(new Label("Statut:"), 0, row);
        grid.add(statutBox, 1, row++);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (equipementBox.getValue() == null) {
                        showError("Erreur", "Veuillez sélectionner un équipement");
                        return null;
                    }
                    if (typeBox.getValue() == null) {
                        showError("Erreur", "Veuillez sélectionner un type de maintenance");
                        return null;
                    }
                    if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
                        showError("Erreur", "La description est obligatoire");
                        return null;
                    }
                    if (datePicker.getValue() == null) {
                        showError("Erreur", "La date est obligatoire");
                        return null;
                    }
                    if (statutBox.getValue() == null) {
                        showError("Erreur", "Veuillez sélectionner un statut");
                        return null;
                    }

                    double cout;
                    try {
                        cout = Double.parseDouble(coutField.getText().trim().replace(",", "."));
                        if (cout < 0) {
                            showError("Erreur", "Le coût ne peut pas être négatif");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError("Erreur", "Le coût doit être un nombre valide");
                        return null;
                    }

                    Maintenance m = isEdit ? maintenance : new Maintenance();
                    m.setEquipementId(equipementBox.getValue().getId());
                    m.setTypeMaintenance(typeBox.getValue());
                    m.setDescription(descriptionField.getText().trim());
                    m.setDateMaintenance(datePicker.getValue());
                    m.setCout(cout);
                    m.setStatut(statutBox.getValue());
                    return m;
                } catch (Exception e) {
                    showError("Erreur", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Maintenance> result = dialog.showAndWait();
        result.ifPresent(m -> {
            try {
                if (isEdit) {
                    maintenanceService.updateMaintenance(
                            m.getId(),
                            m.getEquipementId(),
                            m.getTypeMaintenance(),
                            m.getDescription(),
                            m.getDateMaintenance(),
                            m.getCout(),
                            m.getStatut()
                    );
                    showInfo("Succès", "Maintenance modifiée avec succès !");
                } else {
                    maintenanceService.planifierMaintenance(
                            m.getEquipementId(),
                            m.getTypeMaintenance(),
                            m.getDescription(),
                            m.getDateMaintenance(),
                            m.getCout(),
                            m.getStatut()
                    );
                    showInfo("Succès", "Maintenance planifiée avec succès !");
                }
                refreshTable();
                datePreselectionnee = null;
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void markAsCompleted(Maintenance maintenance) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Marquer comme réalisée");
        confirm.setContentText("Voulez-vous marquer cette maintenance comme réalisée ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maintenanceService.changerStatutMaintenance(maintenance.getId(), "Réalisée");
                refreshTable();
                showInfo("Succès", "Maintenance marquée comme réalisée !");
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
            }
        }
    }

    private void handleDelete(Maintenance maintenance) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la maintenance #" + maintenance.getId() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maintenanceService.deleteMaintenance(maintenance.getId());
                refreshTable();
                showInfo("Succès", "Maintenance supprimée !");
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    public void showStats() {
        double coutTotal = maintenanceService.getCoutTotalMaintenances();
        long total = maintenanceService.getAllMaintenances().size();
        long planifiees = maintenanceService.countMaintenancesByStatut("Planifiée");
        long realisees = maintenanceService.countMaintenancesByStatut("Réalisée");
        long preventives = maintenanceService.countMaintenancesByType("Préventive");
        long correctives = maintenanceService.countMaintenancesByType("Corrective");
        double coutMoyen = maintenanceService.getCoutMoyenMaintenance();
        long aujourdhui = maintenanceService.getNombreMaintenancesAujourdhui();

        SunriseSunsetData solar = sunriseService.getSunriseSunsetToday();
        String solarInfo = solar != null ?
                String.format("Lever: %s | Coucher: %s | Durée: %s",
                        solar.getSunrise().format(timeFormatter),
                        solar.getSunset().format(timeFormatter),
                        solar.getDayLengthFormatted()) :
                "Données solaires non disponibles";

        String stats = String.format(
                "📊 STATISTIQUES DES MAINTENANCES\n\n" +
                        "Total des maintenances : %d\n" +
                        "✅ Planifiées : %d\n" +
                        "✓ Réalisées : %d\n" +
                        "🛡️ Préventives : %d\n" +
                        "🔧 Correctives : %d\n\n" +
                        "💰 Coût total : %.2f DT\n" +
                        "💵 Coût moyen : %.2f DT\n\n" +
                        "📅 Maintenances aujourd'hui : %d\n" +
                        "📆 Maintenances à venir : %d\n\n" +
                        "☀️ Infos solaires aujourd'hui:\n%s\n\n" +
                        "🌞 Recommandation: %s",
                total, planifiees, realisees, preventives, correctives,
                coutTotal, coutMoyen, aujourdhui,
                maintenanceService.getUpcomingMaintenances().size(),
                solarInfo,
                sunriseService.getRecommendedWorkHours(LocalDate.now())
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques");
        alert.setHeaderText("📈 Rapport des maintenances");
        alert.setContentText(stats);
        alert.showAndWait();
    }

    @FXML
    private void ouvrirCalendrierMaintenance() {
        try {
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

    private void ouvrirCalendrierAvecDate(LocalDate date) {
        try {
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

            CalendrierMaintenanceController controller = loader.getController();
            controller.setDateSelectionnee(date);

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
    private void ouvrirCentreAlertes() {
        try {
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
            stage.show();

        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir les alertes: " + e.getMessage());
            e.printStackTrace();
        }
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
}