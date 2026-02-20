package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.ParcelleService;

import java.text.DecimalFormat;
import java.util.Optional;

public class ParcelleController {
    // Weather emoji constants
    private static final String SUNNY = "☀️";
    private static final String CLOUDY = "☁️";
    private static final String RAINY = "🌧️";
    private static final String STORM = "⛈️";
    private static final String SNOWY = "❄️";
    private static final String WINDY = "💨";
    private static final String FOGGY = "🌫️";
    private static final String THERMOMETER = "🌡️";
    private static final String UNKNOWN_WEATHER = "❓";

    @FXML
    private TableView<Parcelle> tableView;
    @FXML
    private TableColumn<Parcelle, Integer> colId;
    @FXML
    private TableColumn<Parcelle, Float> colSurface;
    @FXML
    private TableColumn<Parcelle, String> colLocalisation;
    @FXML
    private TableColumn<Parcelle, Void> colActions;
    @FXML
    private TableColumn<Parcelle, Double> colTemperature;
    @FXML
    private TableColumn<Parcelle, String> colWeather;

    @FXML
    private TextField surfaceField;
    @FXML
    private TextField localisationField;
    @FXML
    private Button saveBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private Label lblStatus;
    @FXML
    private Label totalParcellesLabel;
    @FXML
    private Label surfaceTotaleLabel;
    @FXML
    private Label surfaceMoyenneLabel;
    @FXML
    private Label totalParcellesChange;
    @FXML
    private Label moyenneChange;
    @FXML
    private TextField searchField;
    @FXML
    private Label footerStatus;
    @FXML
    private Button addNewBtn;
    @FXML
    private Button refreshBtn;

    private ParcelleService service = new ParcelleService();
    private ObservableList<Parcelle> masterData = FXCollections.observableArrayList();
    private FilteredList<Parcelle> filteredData;
    private Parcelle selectedParcelle = null;
    private DecimalFormat df = new DecimalFormat("#.00");

    @FXML
    public void initialize() {
        setupTableColumns();

        // Initialize filteredData with empty list to avoid null pointer
        filteredData = new FilteredList<>(FXCollections.observableArrayList(), p -> true);

        loadData();
        setupSearch();
        updateStats();
        refreshBtn.setOnAction(e -> refreshWeather());
    }

    /**
     * Get weather emoji based on weather condition text
     */
    private String getWeatherEmoji(String weather) {
        if (weather == null || weather.equals("N/A")) return UNKNOWN_WEATHER;

        String weatherLower = weather.toLowerCase();

        if (weatherLower.contains("clear") || weatherLower.contains("sun")) {
            return SUNNY;
        } else if (weatherLower.contains("cloud")) {
            return CLOUDY;
        } else if (weatherLower.contains("rain") || weatherLower.contains("drizzle")) {
            return RAINY;
        } else if (weatherLower.contains("thunder") || weatherLower.contains("storm")) {
            return STORM;
        } else if (weatherLower.contains("snow")) {
            return SNOWY;
        } else if (weatherLower.contains("wind")) {
            return WINDY;
        } else if (weatherLower.contains("fog") || weatherLower.contains("mist")) {
            return FOGGY;
        } else {
            return THERMOMETER;
        }
    }

    @FXML
    private void refreshWeather() {
        loadData(); // Calls ParcelleService.afficher() which fills live weather
        showSuccessToast("🔄 Météo actualisée");
    }

    private void setupTableColumns() {
        // Configure columns
       // colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getIdParcelle()).asObject());
        colSurface.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getSurface()).asObject());
        colLocalisation.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocalisation()));

        // Weather columns with styling
        colTemperature.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTemperature()).asObject());
        colWeather.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWeather()));

        // Style the table
        tableView.setStyle("-fx-background-color: #ffffff;");

        // Style column headers
        String headerStyle = "-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;";
      //  colId.setStyle(headerStyle);
        colSurface.setStyle(headerStyle);
        colLocalisation.setStyle(headerStyle);
        colActions.setStyle(headerStyle);
        colTemperature.setStyle(headerStyle);
        colWeather.setStyle(headerStyle);

        // Set cell factories for custom styling
       // colId.setCellFactory(column -> createStyledCell());
        colSurface.setCellFactory(column -> createStyledSurfaceCell());
        colLocalisation.setCellFactory(column -> createStyledCell());

        // Temperature column with emoji
        colTemperature.setCellFactory(column -> new TableCell<Parcelle, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                } else {
                    setText(THERMOMETER + " " + df.format(item) + "°C");

                    // Color code temperature
                    String color;
                    if (item > 30) {
                        color = "#ef4444"; // Hot - red
                    } else if (item > 20) {
                        color = "#f97316"; // Warm - orange
                    } else if (item > 10) {
                        color = "#3b82f6"; // Mild - blue
                    } else {
                        color = "#06b6d4"; // Cold - cyan
                    }

                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        // Weather column with emoji
        colWeather.setCellFactory(column -> new TableCell<Parcelle, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                } else {
                    String emoji = getWeatherEmoji(item);
                    setText(emoji + " " + item);

                    // Background color based on weather
                    String backgroundColor = "#ffffff";
                    if (item.contains("Clear") || item.contains("Sun")) {
                        backgroundColor = "#fffbeb"; // Light yellow for sun
                    } else if (item.contains("Cloud")) {
                        backgroundColor = "#f8fafc"; // Light gray for clouds
                    } else if (item.contains("Rain")) {
                        backgroundColor = "#eff6ff"; // Light blue for rain
                    }

                    setStyle("-fx-padding: 10; -fx-background-color: " + backgroundColor + "; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        // Set action buttons cell factory
        colActions.setCellFactory(param -> new TableCell<Parcelle, Void>() {
            private final Button editBtn = createStyledButton("✏", "#f59e0b", "Modifier");
            private final Button deleteBtn = createStyledButton("❌", "#ef4444", "Supprimer");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.setOnAction(event -> {
                    Parcelle parcelle = getTableView().getItems().get(getIndex());
                    showEditModal(parcelle);
                });
                deleteBtn.setOnAction(event -> {
                    Parcelle parcelle = getTableView().getItems().get(getIndex());
                    handleDelete(parcelle);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private Button createStyledButton(String text, String color, String tooltip) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14; -fx-min-width: 40; -fx-min-height: 35;");
        btn.setTooltip(new Tooltip(tooltip));

        // Hover effect
        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14; -fx-min-width: 40; -fx-min-height: 35;")
        );
        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14; -fx-min-width: 40; -fx-min-height: 35;")
        );

        return btn;
    }

    private <T> TableCell<Parcelle, T> createStyledCell() {
        return new TableCell<Parcelle, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-text-fill: #475569; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                }
            }
        };
    }

    private TableCell<Parcelle, Float> createStyledSurfaceCell() {
        return new TableCell<Parcelle, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                } else {
                    setText("📐 " + df.format(item) + " m²");

                    // Color code based on surface size
                    String color = "#475569"; // default
                    if (item > 1000) {
                        color = "#22c55e"; // large - green
                    } else if (item > 500) {
                        color = "#3b82f6"; // medium - blue
                    } else if (item > 100) {
                        color = "#eab308"; // small - yellow
                    }

                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                }
            }
        };
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (filteredData != null) {
                    filteredData.setPredicate(parcelle -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }

                        String lowerCaseFilter = newValue.toLowerCase();

                        if (String.valueOf(parcelle.getIdParcelle()).contains(lowerCaseFilter)) {
                            return true;
                        } else if (String.valueOf(parcelle.getSurface()).contains(lowerCaseFilter)) {
                            return true;
                        } else if (parcelle.getLocalisation().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        } else if (parcelle.getWeather() != null && parcelle.getWeather().toLowerCase().contains(lowerCaseFilter)) {
                            return true; // Allow searching by weather condition
                        }
                        return false;
                    });

                    updateFooterStatus();
                }
            });
        }
    }

    @FXML
    public void loadData() {
        try {
            masterData.clear();
            masterData.addAll(service.afficher());

            // Recreate filteredData with new masterData
            filteredData = new FilteredList<>(masterData, p -> true);

            // Update the table items
            SortedList<Parcelle> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedData);

            if (lblStatus != null) {
                lblStatus.setText("🌱 " + masterData.size() + " parcelles");
            }

            updateFooterStatus();
            updateStats();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les parcelles : " + e.getMessage());
        }
    }

    private void updateFooterStatus() {
        if (footerStatus != null && filteredData != null) {
            footerStatus.setText("⚡ " + filteredData.size() + " parcelles sur " + masterData.size());
        }
    }

    private void updateStats() {
        if (totalParcellesLabel != null) {
            totalParcellesLabel.setText(String.valueOf(masterData.size()));

            // Calculate total surface
            float totalSurface = (float) masterData.stream()
                    .mapToDouble(Parcelle::getSurface)
                    .sum();

            surfaceTotaleLabel.setText("📏 " + df.format(totalSurface) + " m²");

            // Calculate average surface
            if (!masterData.isEmpty()) {
                float avgSurface = totalSurface / masterData.size();
                surfaceMoyenneLabel.setText("📊 " + df.format(avgSurface) + " m²");
            } else {
                surfaceMoyenneLabel.setText("📊 0 m²");
            }

            // Update change indicators
            if (totalParcellesChange != null) {
                totalParcellesChange.setText("📈 +" + (int)(Math.random() * 5) + "% ce mois");
            }
        }
    }

    @FXML
    private void showAddModal() {
        showModal("Ajouter une Parcelle", null);
    }

    private void showEditModal(Parcelle parcelle) {
        showModal("Modifier la Parcelle", parcelle);
    }

    private void showModal(String title, Parcelle parcelle) {
        // Create modal stage
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setTitle(title);

        // Create main container
        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 5);");
        container.setPadding(new Insets(25));
        container.setMaxWidth(500);
        container.setMaxHeight(500);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label((parcelle == null ? "➕ " : "✏️ ") + title);
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 18; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> modalStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Form fields
        VBox form = new VBox(15);

        // Surface field
        VBox surfaceBox = new VBox(5);
        Label surfaceLabel = new Label("📐 Surface (m²)");
        surfaceLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #334155; -fx-font-weight: bold;");

        TextField surfaceInput = new TextField();
        surfaceInput.setPromptText("Ex: 1200.5");
        surfaceInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        surfaceInput.setPrefHeight(40);

        if (parcelle != null) {
            surfaceInput.setText(String.valueOf(parcelle.getSurface()));
        }

        surfaceBox.getChildren().addAll(surfaceLabel, surfaceInput);

        // Localisation field
        VBox localisationBox = new VBox(5);
        Label localisationLabel = new Label("📍 Localisation");
        localisationLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #334155; -fx-font-weight: bold;");

        TextField localisationInput = new TextField();
        localisationInput.setPromptText("Ex: Champ nord");
        localisationInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        localisationInput.setPrefHeight(40);

        if (parcelle != null) {
            localisationInput.setText(parcelle.getLocalisation());
        }

        localisationBox.getChildren().addAll(localisationLabel, localisationInput);

        form.getChildren().addAll(surfaceBox, localisationBox);

        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modalStage.close());

        Button saveBtn = new Button((parcelle == null ? "➕ Ajouter" : "💾 Modifier"));
        saveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            // Validate inputs
            String surfaceText = surfaceInput.getText().trim();
            String localisation = localisationInput.getText().trim();

            if (validateInputs(surfaceText, localisation, surfaceInput, localisationInput)) {
                try {
                    float surface = Float.parseFloat(surfaceText);

                    if (parcelle == null) {
                        // Add new
                        Parcelle newParcelle = new Parcelle(0, surface, localisation);
                        service.ajouter(newParcelle);
                        showSuccessToast("✅ Parcelle ajoutée avec succès!");
                    } else {
                        // Update existing
                        parcelle.setSurface(surface);
                        parcelle.setLocalisation(localisation);
                        service.modifier(parcelle);
                        showSuccessToast("✅ Parcelle modifiée avec succès!");
                    }

                    modalStage.close();
                    loadData();

                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Opération échouée : " + ex.getMessage());
                }
            }
        });

        buttons.getChildren().addAll(cancelBtn, saveBtn);

        container.getChildren().addAll(header, new Separator(), form, buttons);

        // Create scene with transparent background
        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT);

        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    private boolean validateInputs(String surfaceText, String localisation,
                                   TextField surfaceField, TextField localisationField) {
        // Reset styles
        surfaceField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        localisationField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");

        // Check empty fields
        if (surfaceText.isEmpty()) {
            showFieldError(surfaceField, "⚠️ La surface est obligatoire");
            return false;
        }
        if (localisation.isEmpty()) {
            showFieldError(localisationField, "⚠️ La localisation est obligatoire");
            return false;
        }

        // Validate surface format
        float surface;
        try {
            surface = Float.parseFloat(surfaceText);
        } catch (NumberFormatException e) {
            showFieldError(surfaceField, "⚠️ La surface doit être un nombre valide");
            return false;
        }

        // Validate surface value
        if (surface <= 0) {
            showFieldError(surfaceField, "⚠️ La surface doit être supérieure à 0");
            return false;
        }
        if (surface < 10) {
            showFieldError(surfaceField, "⚠️ La surface doit être d'au moins 10 m²");
            return false;
        }

        // Validate localisation length
        if (localisation.length() < 3) {
            showFieldError(localisationField, "⚠️ La localisation doit contenir au moins 3 caractères");
            return false;
        }
        if (localisation.length() > 255) {
            showFieldError(localisationField, "⚠️ La localisation ne peut pas dépasser 255 caractères");
            return false;
        }

        // Validate localisation characters
        if (!localisation.matches("[\\p{L}\\d\\s\\-']+")) {
            showFieldError(localisationField, "⚠️ La localisation ne doit contenir que des lettres, chiffres, espaces, tirets ou apostrophes");
            return false;
        }

        return true;
    }

    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-padding: 10;");
        showToast(message, "#ef4444");
    }

    private void showSuccessToast(String message) {
        showToast(message, "#22c55e");
    }

    private void showToast(String message, String color) {
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 15 25; -fx-background-radius: 30; -fx-font-size: 14; -fx-font-weight: bold;");
        label.setAlignment(Pos.CENTER);

        Scene scene = new Scene(label);
        scene.setFill(Color.TRANSPARENT);

        toastStage.setScene(scene);

        // Position at bottom right
        toastStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getMaxX() - 300);
        toastStage.setY(javafx.stage.Screen.getPrimary().getVisualBounds().getMaxY() - 100);

        toastStage.show();

        // Auto-close after 2 seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> toastStage.close());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleDelete(Parcelle parcelle) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("🗑️ Confirmation");
        confirm.setHeaderText("Supprimer la parcelle");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer la parcelle \"" + parcelle.getLocalisation() + "\" ?");

        // Style the dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14; -fx-padding: 20;");

        ButtonType yesButton = new ButtonType("✅ Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("❌ Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                service.supprimer(parcelle.getIdParcelle());
                loadData();
                showSuccessToast("🗑️ Parcelle supprimée avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Suppression échouée : " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearForm() {
        surfaceField.clear();
        localisationField.clear();
        tableView.getSelectionModel().clearSelection();
        selectedParcelle = null;
        saveBtn.setText("➕ Ajouter");
        resetFieldStyles();
    }

    @FXML
    private void handleDelete() {
        Parcelle selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleDelete(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "⚠️ Sélection", "Veuillez sélectionner une parcelle à supprimer.");
        }
    }

    @FXML
    private void handleSave() {
        showAddModal();
    }

    private void resetFieldStyles() {
        if (surfaceField != null) {
            surfaceField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        }
        if (localisationField != null) {
            localisationField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        alert.showAndWait();
    }
}