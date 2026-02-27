package Finance.Controller;

import Finance.model.Revenu;
import Finance.service.revenuService;
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

public class RevenuTableController implements Initializable {

    // ========== ÉLÉMENTS DE L'INTERFACE ==========
    @FXML private TableView<Revenu> tableView;
    @FXML private TableColumn<Revenu, Long> colId;
    @FXML private TableColumn<Revenu, Double> colMontant;
    @FXML private TableColumn<Revenu, String> colSource;
    @FXML private TableColumn<Revenu, String> colDescription;
    @FXML private TableColumn<Revenu, Date> colDate;
    @FXML private TableColumn<Revenu, Void> colAction;
    @FXML private Label lblStatus;
    @FXML private Button btnAddRevenu;
    @FXML private Button btnOCRScan;

    // ========== SERVICES ==========
    private revenuService revenuService = new revenuService();

    // ========== RÉFÉRENCE AU CONTRÔLEUR PRINCIPAL ==========
    private FinanceController mainController;

    public void setMainController(FinanceController mainController) {
        this.mainController = mainController;
    }

    // ========== INITIALISATION ==========
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadRevenus();
    }

    // ========== CONFIGURATION DES COLONNES ==========
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idRevenu"));

        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMontant.setCellFactory(column -> new TableCell<Revenu, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item));
                    setStyle("-fx-text-fill: #1a4d2e; -fx-font-weight: bold;");
                }
            }
        });

        colSource.setCellValueFactory(new PropertyValueFactory<>("source"));

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(column -> new TableCell<Revenu, String>() {
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

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRevenu"));

        setupActionColumn();
    }

    // ========== CONFIGURATION DE LA COLONNE ACTIONS ==========
    private void setupActionColumn() {
        colAction.setCellFactory(column -> new TableCell<Revenu, Void>() {
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
    }

    // ========== CRÉATION DES BOUTONS CIRCULAIRES ==========
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

    // ========== CHARGEMENT DES DONNÉES ==========
    @FXML
    private void handleAddRevenu() {
        openModal("/Finance/view/RevenuView.fxml", "Ajouter Revenu", "#1a4d2e");
        loadRevenus();
        if (mainController != null) mainController.refreshDashboard();
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

            if (montant <= 0) {
                showError("Erreur", "Montant invalide !");
                return;
            }

            String source = extractSource(cleaned);
            Revenu revenu = new Revenu();
            revenu.setSource(source);
            revenu.setDateRevenu(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            revenu.setDescription(description);
            revenu.setMontant(montant);

            revenuService.ajouterRevenu(revenu);
            loadRevenus();
            if (mainController != null) mainController.refreshDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            showError("OCR failed", e.getMessage());
        }
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

    // ========== ACTIONS SUR LES ÉLÉMENTS ==========
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Finance/view/RevenuView.fxml"));
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

    // ========== FENÊTRE MODALE ==========
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

    // ========== FONCTIONS OCR ==========
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

    // ========== GESTION DES ERREURS ==========
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
