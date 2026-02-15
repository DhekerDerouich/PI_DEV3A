package com.pi.controller;

import com.pi.model.Maintenance;
import com.pi.model.Equipement;
import com.pi.service.MaintenanceService;
import com.pi.service.EquipementService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class MaintenanceController {

    @FXML private TableView<Maintenance> maintenanceTable;
    @FXML private TableColumn<Maintenance, Integer> colId;
    @FXML private TableColumn<Maintenance, String> colEquipementNom;
    @FXML private TableColumn<Maintenance, String> colType;
    @FXML private TableColumn<Maintenance, String> colDescription;
    @FXML private TableColumn<Maintenance, String> colDate;
    @FXML private TableColumn<Maintenance, Double> colCout;
    @FXML private TableColumn<Maintenance, String> colStatut;
    @FXML private TableColumn<Maintenance, String> colActions;

    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<Integer> filterEquipement;
    @FXML private Label totalLabel;
    @FXML private Label planifieesLabel;
    @FXML private Label realiseesLabel;
    @FXML private Label coutTotalLabel;

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final EquipementService equipementService = new EquipementService();
    private final ObservableList<Maintenance> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

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

        // Style pour le statut
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

        // Colonne Actions avec boutons Modifier/Supprimer
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button completeBtn = new Button("✅");

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                completeBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
                completeBtn.setTooltip(new Tooltip("Marquer comme réalisée"));

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
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.getChildren().add(editBtn);
                    buttons.getChildren().add(deleteBtn);
                    if ("Planifiée".equals(m.getStatut())) {
                        buttons.getChildren().add(completeBtn);
                    }
                    setGraphic(buttons);
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
        filterEquipement.getItems().add(0); // 0 = Tous
        filterEquipement.getItems().addAll(
                equipementService.getAllEquipements().stream()
                        .map(Equipement::getId)
                        .sorted()
                        .collect(Collectors.toList())
        );
        filterEquipement.setValue(0);
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

        // ComboBox pour l'équipement
        ComboBox<Equipement> equipementBox = new ComboBox<>();
        equipementBox.getItems().addAll(equipementService.getAllEquipements());
        equipementBox.setConverter(new javafx.util.StringConverter<Equipement>() {
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

        // Type de maintenance
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Préventive", "Corrective");
        typeBox.setPromptText("Type de maintenance");

        // Description
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description de la maintenance");

        // Date
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date de maintenance");

        // Coût
        TextField coutField = new TextField();
        coutField.setPromptText("Coût (DT)");

        // Statut
        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("Planifiée", "Réalisée");
        statutBox.setPromptText("Statut");

        // Pré-remplir si édition
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
            datePicker.setValue(LocalDate.now().plusDays(1));
            statutBox.setValue("Planifiée");
        }

        // Ajout au grid
        grid.add(new Label("Équipement:"), 0, 0);
        grid.add(equipementBox, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Coût:"), 0, 4);
        grid.add(coutField, 1, 4);
        grid.add(new Label("Statut:"), 0, 5);
        grid.add(statutBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validation
                    if (equipementBox.getValue() == null) {
                        showError("Erreur", "Veuillez sélectionner un équipement");
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

                    double cout;
                    try {
                        cout = Double.parseDouble(coutField.getText().trim());
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
                        "📆 Maintenances à venir : %d",
                total, planifiees, realisees, preventives, correctives,
                coutTotal, coutMoyen, aujourdhui,
                maintenanceService.getUpcomingMaintenances().size()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques");
        alert.setHeaderText("📈 Rapport des maintenances");
        alert.setContentText(stats);
        alert.showAndWait();
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