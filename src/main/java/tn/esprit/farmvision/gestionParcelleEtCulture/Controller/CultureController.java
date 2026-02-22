package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;
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
import javafx.util.Callback;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.CultureService;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CultureController {

    @FXML private TableView<Culture> tableView;
    @FXML private TableColumn<Culture, Integer> colId;
    @FXML private TableColumn<Culture, String> colNom;
    @FXML private TableColumn<Culture, String> colType;
    @FXML private TableColumn<Culture, Date> colSemis;
    @FXML private TableColumn<Culture, Date> colRecolte;
    @FXML private TableColumn<Culture, Void> colActions;

    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private DatePicker semisPicker;
    @FXML private DatePicker recoltePicker;
    @FXML private Label lblStatus;
    @FXML private Label totalCulturesLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label recolteesLabel;
    @FXML private Label totalCulturesChange;
    @FXML private Label recolteesChange;
    @FXML private TextField searchField;
    @FXML private Button calendarBtn;
    @FXML private Label footerStatus;

    private CultureService service = new CultureService();
    private ObservableList<Culture> masterData = FXCollections.observableArrayList();
    private FilteredList<Culture> filteredData;
    private Culture selectedCulture = null;
    private MainController mainController;
    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
        setupSearch();
        setupLiveValidation();
        updateStats();
    }
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    @FXML
    private void showCalendarView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/CalendarView.fxml"));
            Node calendarView = loader.load();

            CalendarController calendarController = loader.getController();
            calendarController.setOnBackToCultures(() -> {
                // Switch back to culture view
                if (mainController != null) {
                    mainController.showCulture();
                }
            });

            // Get the contentArea from MainController and show calendar
            if (mainController != null) {
                mainController.showCustomView(calendarView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le calendrier");
        }
    }

    private void setupTableColumns() {
        // Configure columns
        // colId.setCellValueFactory(new PropertyValueFactory<>("idCulture"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCulture"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeCulture"));
        colSemis.setCellValueFactory(new PropertyValueFactory<>("dateSemis"));
        colRecolte.setCellValueFactory(new PropertyValueFactory<>("dateRecolte"));

        // Style the table
        tableView.setStyle("-fx-background-color: #ffffff;");

        // Style column headers
        String headerStyle = "-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;";
        //colId.setStyle(headerStyle);
        colNom.setStyle(headerStyle);
        colType.setStyle(headerStyle);
        colSemis.setStyle(headerStyle);
        colRecolte.setStyle(headerStyle);
        colActions.setStyle(headerStyle);

        // Set cell factories for custom styling
        //  colId.setCellFactory(column -> createStyledCell());
        colNom.setCellFactory(column -> createStyledCell());
        colType.setCellFactory(column -> createStyledCell());
        colSemis.setCellFactory(column -> createStyledDateCell());
        colRecolte.setCellFactory(column -> createStyledDateCell());

        // Set action buttons cell factory
        colActions.setCellFactory(param -> new TableCell<Culture, Void>() {
            private final Button editBtn = createStyledButton("✏", "#f59e0b", "Modifier");
            private final Button deleteBtn = createStyledButton("❌", "#ef4444", "Supprimer");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.setOnAction(event -> {
                    Culture culture = getTableView().getItems().get(getIndex());
                    showEditModal(culture);
                });
                deleteBtn.setOnAction(event -> {
                    Culture culture = getTableView().getItems().get(getIndex());
                    handleDelete(culture);
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

    private <T> TableCell<Culture, T> createStyledCell() {
        return new TableCell<Culture, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                } else {
                    setText(item.toString());
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-text-fill: #475569; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

                    // Add row hover effect
                    TableRow<?> row = getTableRow();
                    if (row != null && !row.isSelected()) {
                        row.hoverProperty().addListener((obs, old, isHover) -> {
                            if (isHover) {
                                setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-text-fill: #475569; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                            } else {
                                setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-text-fill: #475569; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                            }
                        });
                    }
                }
            }
        };
    }

    private TableCell<Culture, Date> createStyledDateCell() {
        return new TableCell<Culture, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                } else {
                    LocalDate date = item.toLocalDate();
                    LocalDate today = LocalDate.now();

                    // Style based on date proximity
                    long daysUntil = ChronoUnit.DAYS.between(today, date);
                    String color = "#475569"; // default

                    if (daysUntil < 0) {
                        color = "#94a3b8"; // past
                    } else if (daysUntil <= 7) {
                        color = "#22c55e"; // upcoming (within a week)
                    } else if (daysUntil <= 30) {
                        color = "#eab308"; // within a month
                    }

                    setText(item.toString());
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-text-fill: " + color + "; -fx-font-weight: " + (daysUntil <= 7 ? "bold" : "normal") + "; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                }
            }
        };
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(culture -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (culture.getNomCulture().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (culture.getTypeCulture().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(culture.getIdCulture()).contains(lowerCaseFilter)) {
                    return true;
                } else if (culture.getDateSemis().toString().contains(lowerCaseFilter)) {
                    return true;
                } else if (culture.getDateRecolte().toString().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });

            updateFooterStatus();
        });

        SortedList<Culture> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    public void loadData() {
        try {
            masterData.clear();
            masterData.addAll(service.afficher());

            if (lblStatus != null) {
                lblStatus.setText("🌱 " + masterData.size() + " cultures");
            }

            updateFooterStatus();
            updateStats();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les cultures : " + e.getMessage());
        }
    }

    private void updateFooterStatus() {
        if (footerStatus != null) {
            footerStatus.setText("⚡ " + filteredData.size() + " cultures sur " + masterData.size());
        }
    }

    private void updateStats() {
        if (totalCulturesLabel != null) {
            totalCulturesLabel.setText(String.valueOf(masterData.size()));

            // Calculate en cours (cultures not yet harvested or harvested after today)
            LocalDate today = LocalDate.now();
            long enCours = masterData.stream()
                    .filter(c -> c.getDateRecolte().toLocalDate().isAfter(today) ||
                            c.getDateRecolte().toLocalDate().isEqual(today))
                    .count();

            long recoltees = masterData.size() - enCours;

            enCoursLabel.setText(String.valueOf(enCours));
            recolteesLabel.setText(String.valueOf(recoltees));

            // Update change indicators (mock data)
            totalCulturesChange.setText("+" + (int)(Math.random() * 10) + "% ce mois");
            recolteesChange.setText("+" + (int)(Math.random() * 15) + "% ce mois");
        }
    }

    @FXML
    private void showAddModal() {
        showModal("Ajouter une Culture", null);
    }

    private void showEditModal(Culture culture) {
        showModal("Modifier la Culture", culture);
    }

    private void showModal(String title, Culture culture) {
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
        container.setMaxHeight(600);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 18; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> modalStage.close());

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Form fields
        VBox form = new VBox(15);

        // Nom field
        VBox nomBox = new VBox(5);
        Label nomLabel = new Label("Nom de la culture");
        nomLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #334155; -fx-font-weight: bold;");

        TextField nomInput = new TextField();
        nomInput.setPromptText("Ex: Blé tendre");
        nomInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        nomInput.setPrefHeight(40);

        if (culture != null) {
            nomInput.setText(culture.getNomCulture());
        }

        nomBox.getChildren().addAll(nomLabel, nomInput);

        // Type field
        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Type");
        typeLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #334155; -fx-font-weight: bold;");

        TextField typeInput = new TextField();
        typeInput.setPromptText("Ex: Céréale");
        typeInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        typeInput.setPrefHeight(40);

        if (culture != null) {
            typeInput.setText(culture.getTypeCulture());
        }

        typeBox.getChildren().addAll(typeLabel, typeInput);

        // Date semis
        VBox semisBox = new VBox(5);
        Label semisLabel = new Label("Date de semis");
        semisLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #334155; -fx-font-weight: bold;");

        DatePicker semisInput = new DatePicker();
        semisInput.setPromptText("YYYY-MM-DD");
        semisInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        semisInput.setPrefHeight(40);

        if (culture != null && culture.getDateSemis() != null) {
            semisInput.setValue(culture.getDateSemis().toLocalDate());
        }

        semisBox.getChildren().addAll(semisLabel, semisInput);

        // Date récolte
        VBox recolteBox = new VBox(5);
        Label recolteLabel = new Label("Date de récolte");
        recolteLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #334155; -fx-font-weight: bold;");

        DatePicker recolteInput = new DatePicker();
        recolteInput.setPromptText("YYYY-MM-DD");
        recolteInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        recolteInput.setPrefHeight(40);

        if (culture != null && culture.getDateRecolte() != null) {
            recolteInput.setValue(culture.getDateRecolte().toLocalDate());
        }

        recolteBox.getChildren().addAll(recolteLabel, recolteInput);

        form.getChildren().addAll(nomBox, typeBox, semisBox, recolteBox);

        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modalStage.close());

        Button saveBtn = new Button(culture == null ? "Ajouter" : "Modifier");
        saveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            // Validate inputs
            String nom = nomInput.getText().trim();
            String type = typeInput.getText().trim();
            LocalDate semis = semisInput.getValue();
            LocalDate recolte = recolteInput.getValue();

            if (validateInputs(nom, type, semis, recolte, nomInput, typeInput, semisInput, recolteInput)) {
                try {
                    Date sqlSemis = Date.valueOf(semis);
                    Date sqlRecolte = Date.valueOf(recolte);

                    if (culture == null) {
                        // Add new
                        Culture newCulture = new Culture(0, nom, type, sqlSemis, sqlRecolte);
                        service.ajouter(newCulture);
                        showSuccessToast("Culture ajoutée avec succès!");
                    } else {
                        // Update existing
                        culture.setNomCulture(nom);
                        culture.setTypeCulture(type);
                        culture.setDateSemis(sqlSemis);
                        culture.setDateRecolte(sqlRecolte);
                        service.modifier(culture);
                        showSuccessToast("Culture modifiée avec succès!");
                    }

                    modalStage.close();
                    loadData();

                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Opération échouée : " + ex.getMessage());
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

    private boolean validateInputs(String nom, String type, LocalDate semis, LocalDate recolte,
                                   TextField nomField, TextField typeField, DatePicker semisPicker, DatePicker recoltePicker) {
        // Reset styles
        nomField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        typeField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 10;");
        semisPicker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        recoltePicker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        if (nom.isEmpty()) {
            showFieldError(nomField, "Le nom est obligatoire");
            return false;
        }
        if (type.isEmpty()) {
            showFieldError(typeField, "Le type est obligatoire");
            return false;
        }
        if (semis == null) {
            showFieldError(semisPicker, "La date de semis est obligatoire");
            return false;
        }
        if (recolte == null) {
            showFieldError(recoltePicker, "La date de récolte est obligatoire");
            return false;
        }

        if (nom.length() < 3) {
            showFieldError(nomField, "Le nom doit contenir au moins 3 caractères");
            return false;
        }
        if (nom.length() > 100) {
            showFieldError(nomField, "Le nom ne peut pas dépasser 100 caractères");
            return false;
        }

        if (type.length() < 3) {
            showFieldError(typeField, "Le type doit contenir au moins 3 caractères");
            return false;
        }
        if (type.length() > 50) {
            showFieldError(typeField, "Le type ne peut pas dépasser 50 caractères");
            return false;
        }

        if (nom.matches("\\d+")) {
            showFieldError(nomField, "Le nom ne peut pas être composé uniquement de chiffres");
            return false;
        }
        if (type.matches("\\d+")) {
            showFieldError(typeField, "Le type ne peut pas être composé uniquement de chiffres");
            return false;
        }

        String allowedPattern = "[\\p{L}\\d\\s\\-']+";
        if (!nom.matches(allowedPattern)) {
            showFieldError(nomField, "Le nom ne doit contenir que des lettres, chiffres, espaces, tirets ou apostrophes");
            return false;
        }
        if (!type.matches(allowedPattern)) {
            showFieldError(typeField, "Le type ne doit contenir que des lettres, chiffres, espaces, tirets ou apostrophes");
            return false;
        }

        if (recolte.isBefore(semis)) {
            showFieldError(recoltePicker, "La date de récolte doit être postérieure à la date de semis");
            return false;
        }

        return true;
    }

    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-padding: 10;");
        showToast(message, "#ef4444");
    }

    private void showFieldError(DatePicker picker, String message) {
        picker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ef4444; -fx-border-width: 2;");
        showToast(message, "#ef4444");
    }

    private void showSuccessToast(String message) {
        showToast(message, "#22c55e");
    }

    private void showToast(String message, String color) {
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.initModality(Modality.APPLICATION_MODAL);

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

    private void handleDelete(Culture culture) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la culture");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer la culture \"" + culture.getNomCulture() + "\" ?");

        // Style the dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14; -fx-padding: 20;");

        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        confirm.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                try {
                    service.supprimer(culture.getIdCulture());
                    loadData();
                    showSuccessToast("Culture supprimée avec succès!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    private void setupLiveValidation() {
        // Remove existing form validation since we're using modal forms
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 12;");}}