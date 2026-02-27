package Finance.Controller;

import Finance.model.Depense;
import Finance.service.depenseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

public class DepenseTableController implements Initializable {

    @FXML private TableView<Depense> tableView;
    @FXML private TableColumn<Depense, Long> colId;
    @FXML private TableColumn<Depense, Double> colMontant;
    @FXML private TableColumn<Depense, String> colType;
    @FXML private TableColumn<Depense, String> colDescription;
    @FXML private TableColumn<Depense, Date> colDate;
    @FXML private TableColumn<Depense, Void> colAction;
    @FXML private Label lblStatus;
    @FXML private Button btnAddDepense;
    @FXML private Button btnOCRScan;

    private depenseService depenseService = new depenseService();
    private FinanceController mainController;

    public void setMainController(FinanceController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadDepenses();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDepense"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(column -> new TableCell<Depense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(String.format("€%.2f", item));
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("typeDepense"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(column -> new TableCell<Depense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setText("-");
                    setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                } else {
                    String displayText = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                    setText(displayText);
                    setStyle("-fx-text-fill: #64748b;");
                }
            }
        });
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDepense"));

        setupActionColumn();
    }

    private void setupActionColumn() {
        colAction.setCellFactory(column -> new TableCell<Depense, Void>() {
            private final HBox hbox = new HBox(8);
            {
                hbox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hbox.getChildren().clear();
                    Depense depense = getTableView().getItems().get(getIndex());

                    StackPane viewBtn = createCircleButton("👁", "#1a4d2e");
                    viewBtn.setOnMouseClicked(e -> showDetails(depense));

                    StackPane editBtn = createCircleButton("✏", "#f59e0b");
                    editBtn.setOnMouseClicked(e -> openEditWindow(depense));

                    StackPane deleteBtn = createCircleButton("🗑", "#dc2626");
                    deleteBtn.setOnMouseClicked(e -> deleteDepense(depense));

                    hbox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });
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
    private void handleAddDepense() {
        openModal("/Finance/view/DepenseView.fxml", "Ajouter Dépense", "#1a4d2e");
        loadDepenses();
        if (mainController != null) mainController.refreshDashboard();
    }

    @FXML
    private void handleOCRScan() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Receipt Image");
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

            if (montant <= 0) {
                showError("Erreur", "Montant invalide !");
                return;
            }

            String type = extractType(cleaned);
            Depense depense = new Depense();
            depense.setTypeDepense(type);
            depense.setDateDepense(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            depense.setDescription(description);
            depense.setMontant(montant);
            depenseService.ajouterDepense(depense);
            loadDepenses();
            if (mainController != null) mainController.refreshDashboard();

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

    private String extractType(String text) {
        Pattern pattern = Pattern.compile("TYPE\\s*:?\\s*([A-Z]{3,})[^A-Z]+DATE", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group(1).trim();
        return "No Type";
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
        Pattern pattern = Pattern.compile("DESCRIPTION\\s*:?\\s*(.*?)\\s*MONTANT", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group(1).trim();
        return "No Description";
    }

    private double extractMontant(String text) {
        Pattern pattern = Pattern.compile("MONTANT\\s*:?\\s*([0-9]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return Double.parseDouble(matcher.group(1));
        return 0;
    }

    public void loadDepenses() {
        try {
            var depenses = depenseService.getAllDepenses();
            tableView.setItems(FXCollections.observableArrayList(depenses));
            lblStatus.setText("📋 " + depenses.size() + " dépenses chargées");
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    private void showDetails(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Dépense");
        alert.setHeaderText("Dépense #" + depense.getIdDepense());
        String description = (depense.getDescription() != null && !depense.getDescription().trim().isEmpty())
                ? depense.getDescription() : "Aucune description";
        alert.setContentText(
                "💰 Montant: €" + String.format("%.2f", depense.getMontant()) + "\n" +
                        "📋 Type: " + depense.getTypeDepense() + "\n" +
                        "📝 Description: " + description + "\n" +
                        "📅 Date: " + depense.getDateDepense()
        );
        alert.showAndWait();
    }

    private void openEditWindow(Depense depense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Finance/view/DepenseView.fxml"));
            Parent root = loader.load();
            DepenseController controller = loader.getController();
            controller.setDepense(depense);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Modifier Dépense");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadDepenses();
            if (mainController != null) mainController.refreshDashboard();
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void deleteDepense(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la dépense ?");
        alert.setContentText("ID: " + depense.getIdDepense() + "\nMontant: €" + String.format("%.2f", depense.getMontant()));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                depenseService.deleteDepense(depense.getIdDepense());
                loadDepenses();
                if (mainController != null) mainController.refreshDashboard();
                lblStatus.setText("✅ Dépense supprimée");
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