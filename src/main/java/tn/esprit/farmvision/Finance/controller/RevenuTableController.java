package tn.esprit.farmvision.Finance.controller;

import javafx.scene.layout.GridPane;
import tn.esprit.farmvision.Finance.model.Revenu;
import tn.esprit.farmvision.Finance.service.revenuService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RevenuTableController implements Initializable {

    @FXML private TableView<Revenu> tableView;
    @FXML private TableColumn<Revenu, Double> colMontant;
    @FXML private TableColumn<Revenu, String> colSource;
    @FXML private TableColumn<Revenu, String> colDescription;
    @FXML private TableColumn<Revenu, Date> colDate;
    @FXML private TableColumn<Revenu, Void> colAction;
    @FXML private Label lblStatus;
    @FXML private Button btnAddRevenu;
    @FXML private Button btnOCRScan;

    private revenuService revenuService = new revenuService();
    private FinanceController mainController;

    public void setMainController(FinanceController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadRevenus();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle("-fx-font-size: 13px;");
    }

    private void setupTableColumns() {
        // Colonne Montant
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(column -> new TableCell<Revenu, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item));
                    setStyle("-fx-text-fill: #1a4d2e; -fx-font-weight: bold; -fx-alignment: CENTER_RIGHT;");
                }
            }
        });
        colMontant.setPrefWidth(100);

        // Colonne Source
        colSource.setCellValueFactory(new PropertyValueFactory<>("source"));
        colSource.setCellFactory(column -> new TableCell<Revenu, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");
                }
            }
        });
        colSource.setPrefWidth(120);

        // Colonne Description
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(column -> new TableCell<Revenu, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setText("-");
                    setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-alignment: CENTER_LEFT; -fx-padding: 5;");
                } else {
                    String displayText = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                    setText(displayText);
                    setStyle("-fx-text-fill: #64748b; -fx-alignment: CENTER_LEFT; -fx-padding: 5;");
                }
            }
        });
        colDescription.setPrefWidth(200);

        // Colonne Date
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRevenu"));
        colDate.setCellFactory(column -> new TableCell<Revenu, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
                }
            }
        });
        colDate.setPrefWidth(100);

        // Colonne Actions
        colAction.setCellFactory(column -> new TableCell<Revenu, Void>() {
            private final HBox hbox = new HBox(8);
            {
                hbox.setAlignment(Pos.CENTER);
                hbox.setPadding(new Insets(5));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hbox.getChildren().clear();
                    Revenu revenu = getTableView().getItems().get(getIndex());

                    StackPane viewBtn = createCircleButton("👁", "#1a4d2e");
                    viewBtn.setOnMouseClicked(e -> showDetails(revenu));

                    StackPane editBtn = createCircleButton("✏", "#f59e0b");
                    editBtn.setOnMouseClicked(e -> openEditWindow(revenu));

                    StackPane deleteBtn = createCircleButton("🗑", "#dc2626");
                    deleteBtn.setOnMouseClicked(e -> deleteRevenu(revenu));

                    hbox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });
        colAction.setPrefWidth(120);
    }

    private StackPane createCircleButton(String icon, String color) {
        Circle circle = new Circle(16, Color.web(color));
        Label label = new Label(icon);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        StackPane stack = new StackPane(circle, label);
        stack.setStyle("-fx-cursor: hand;");
        stack.setOnMouseEntered(e -> {
            circle.setRadius(18);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        });
        stack.setOnMouseExited(e -> {
            circle.setRadius(16);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        });
        return stack;
    }

    @FXML
    private void handleAddRevenu() {
        try {
            // Utiliser la même approche que DepenseTableController
            URL url = getClass().getResource("/tn/esprit/farmvision/Finance/view/RevenuView.fxml");

            if (url == null) {
                showError("Erreur", "Fichier RevenuView.fxml introuvable");
                return;
            }

            System.out.println("✅ Fichier trouvé: " + url);

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter Revenu");
            stage.setScene(scene);

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#1a4d2e", 0.3));
            shadow.setRadius(20);
            root.setEffect(shadow);

            stage.showAndWait();

            loadRevenus();
            if (mainController != null) mainController.refreshDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleOCRScan() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de reçu");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        try {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("F:\\tesseract-OCR\\tessdata");
            tesseract.setLanguage("eng");
            tesseract.setOcrEngineMode(1);
            tesseract.setPageSegMode(1);

            String rawText = tesseract.doOCR(file);
            String cleaned = cleanOCRText(rawText);

            LocalDate date = extractDate(cleaned);
            String description = extractDescription(cleaned);
            double montant = extractMontant(cleaned);
            String source = extractSource(cleaned);

            System.out.println("=== DONNÉES OCR REVENU ===");
            System.out.println("Source: " + source);
            System.out.println("Montant brut: " + montant);
            System.out.println("Description: " + description);
            System.out.println("Date: " + date);
            System.out.println("===========================");

            if (montant <= 0) {
                showError("Erreur", "Montant invalide !");
                return;
            }

            // Créer une boîte de dialogue personnalisée
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("📄 Confirmation OCR");
            dialog.setHeaderText("Vérifiez les données extraites avant ajout");

            ButtonType ajouterButton = new ButtonType("✅ Ajouter", ButtonBar.ButtonData.OK_DONE);
            ButtonType annulerButton = new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(ajouterButton, annulerButton);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField sourceField = new TextField(source);
            sourceField.setPromptText("Source");

            TextField montantField = new TextField(String.valueOf(montant));
            montantField.setPromptText("Montant");

            TextArea descArea = new TextArea(description);
            descArea.setPromptText("Description");
            descArea.setPrefRowCount(3);

            DatePicker datePicker = new DatePicker(date);

            grid.add(new Label("Source:"), 0, 0);
            grid.add(sourceField, 1, 0);
            grid.add(new Label("Montant:"), 0, 1);
            grid.add(montantField, 1, 1);
            grid.add(new Label("Description:"), 0, 2);
            grid.add(descArea, 1, 2);
            grid.add(new Label("Date:"), 0, 3);
            grid.add(datePicker, 1, 3);

            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ajouterButton) {
                try {
                    String newSource = sourceField.getText().trim();
                    String montantText = montantField.getText().trim();
                    String newDesc = descArea.getText().trim();
                    LocalDate newDate = datePicker.getValue();

                    // Validation
                    if (newSource.isEmpty()) {
                        showError("Erreur", "La source ne peut pas être vide");
                        return;
                    }

                    if (montantText.isEmpty()) {
                        showError("Erreur", "Le montant est requis");
                        return;
                    }

                    // Nettoyer le montant (remplacer virgule par point, enlever espaces)
                    montantText = montantText.replace(',', '.');
                    montantText = montantText.replaceAll("\\s", "");

                    double newMontant;
                    try {
                        newMontant = Double.parseDouble(montantText);
                    } catch (NumberFormatException e) {
                        showError("Erreur", "Montant invalide: '" + montantText + "' n'est pas un nombre valide");
                        return;
                    }

                    if (newMontant <= 0) {
                        showError("Erreur", "Le montant doit être positif");
                        return;
                    }

                    if (newDate == null) {
                        showError("Erreur", "La date est requise");
                        return;
                    }

                    // Créer et ajouter le revenu
                    Revenu revenu = new Revenu();
                    revenu.setSource(newSource);
                    revenu.setDateRevenu(Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    revenu.setDescription(newDesc);
                    revenu.setMontant(newMontant);

                    revenuService.ajouterRevenu(revenu);
                    loadRevenus();
                    if (mainController != null) mainController.refreshDashboard();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText(null);
                    success.setContentText("✅ Revenu ajouté avec succès !");
                    success.showAndWait();

                } catch (SQLException e) {
                    showError("Erreur SQL", e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("OCR failed", e.getMessage());
        }
    }

    private String cleanOCRText(String text) {
        text = text.replaceAll("[^\\x00-\\x7F]", "");
        text = text.replaceAll("[^A-Za-z0-9:/\\.\\s]", " ");
        text = text.replaceAll("\\s+", " ");
        return text.trim();
    }

    private LocalDate extractDate(String text) {
        Pattern pattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(matcher.group(1), formatter);
        }
        return LocalDate.now();
    }

    private String extractDescription(String text) {
        Pattern pattern = Pattern.compile(
                "(?i)DESCRIPTION\\s*:?\\s*(.*?)(?=\\s*(?:SOURCE|DATE|MONTANT|$))",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "No Description";
    }

    private double extractMontant(String text) {
        Pattern pattern = Pattern.compile("MONTANT\\s*:?\\s*([0-9]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0;
    }

    private String extractSource(String text) {
        Pattern pattern = Pattern.compile(
                "(?i)(?:SOURCE|Source)\\s*:?\\s*(.*?)(?=\\s*(?:DATE|DESCRIPTION|MONTANT|$))",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "No Source";
    }

    public void loadRevenus() {
        try {
            var revenus = revenuService.getAllRevenus();
            tableView.setItems(FXCollections.observableArrayList(revenus));
            lblStatus.setText("📋 " + revenus.size() + " revenus chargés");
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    private void showDetails(Revenu revenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Revenu");
        alert.setHeaderText("Revenu #" + revenu.getIdRevenu());

        String description = (revenu.getDescription() != null && !revenu.getDescription().trim().isEmpty())
                ? revenu.getDescription() : "Aucune description";

        alert.setContentText(
                "💰 Montant: €" + String.format("%.2f", revenu.getMontant()) + "\n" +
                        "📋 Source: " + revenu.getSource() + "\n" +
                        "📝 Description: " + description + "\n" +
                        "📅 Date: " + revenu.getDateRevenu()
        );
        alert.showAndWait();
    }

    private void openEditWindow(Revenu revenu) {
        try {
            URL url = getClass().getResource("/tn/esprit/farmvision/Finance/view/RevenuView.fxml");
            if (url == null) {
                showError("Erreur", "Fichier RevenuView.fxml introuvable");
                return;
            }

            System.out.println("✅ Fichier trouvé: " + url);

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            RevenuController controller = loader.getController();
            controller.setRevenu(revenu);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Modifier Revenu");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadRevenus();
            if (mainController != null) mainController.refreshDashboard();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", e.getMessage());
        }
    }

    private void deleteRevenu(Revenu revenu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le revenu ?");
        alert.setContentText("ID: " + revenu.getIdRevenu() + "\nMontant: €" + String.format("%.2f", revenu.getMontant()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                revenuService.deleteRevenu(revenu.getIdRevenu());
                loadRevenus();
                if (mainController != null) mainController.refreshDashboard();
                lblStatus.setText("✅ Revenu supprimé");
            } catch (SQLException e) {
                showError("Erreur SQL", e.getMessage());
            }
        }
    }

    private void openModal(String fxml, String title, String accentColor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(scene);
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web(accentColor, 0.3));
            shadow.setRadius(20);
            root.setEffect(shadow);
            stage.showAndWait();
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}