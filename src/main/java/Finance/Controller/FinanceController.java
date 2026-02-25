package Finance.Controller;

import Finance.model.Depense;
import Finance.model.Revenu;
import Finance.service.CurrencyService;
import Finance.service.depenseService;
import Finance.service.revenuService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.net.URL;
import java.sql.SQLException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.FileChooser;
import java.io.File;

@SuppressWarnings("unchecked")
public class FinanceController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private Label lblTableTitle;
    @FXML private Label lblTotalDepense;
    @FXML private Label lblTotalRevenu;
    @FXML private Label lblProfit;
    @FXML private Label lblStatus;
    @FXML private TableView tableView;
    @FXML private Button btnAddDepense;
    @FXML private Button btnOCRScan;
    @FXML private Button btnAddRevenu;
    @FXML private Label lblRates;

    // Sidebar buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnListDepense;
    @FXML private Button btnListRevenu;

    @FXML private Button btnKPI;

    // Sections
    @FXML private VBox tableSection;
    @FXML private VBox kpiSection;

    private depenseService depenseService = new depenseService();
    private revenuService revenuService = new revenuService();
    private String currentView = "dashboard";
    private CurrencyService currencyService = new CurrencyService();

    // Sidebar button styles
    private final String ACTIVE_STYLE = "-fx-background-color: #2d6a4f; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 14 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #a8c5b5; -fx-background-radius: 8; -fx-padding: 14 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblStatus.setText("🟢 Système prêt");
        setActiveButton(btnDashboard);
        updateDashboard();
        updateExchangeRates();
        showDashboard();

        // Cacher la section KPI au démarrage
        kpiSection.setVisible(false);
        kpiSection.setManaged(false);
    }

    @FXML
    private void showDashboard() {
        lblTitle.setText("Tableau de Bord Financier");
        lblTableTitle.setText("Dernières Transactions");
        currentView = "dashboard";

        kpiSection.setVisible(false);
        kpiSection.setManaged(false);
        tableSection.setVisible(true);
        tableSection.setManaged(true);

        btnAddDepense.setVisible(false);
        btnAddRevenu.setVisible(false);
        btnOCRScan.setVisible(false);

        setActiveButton(btnDashboard);
        updateDashboard();
        loadRecentTransactions();
    }

    private void updateDashboard() {
        try {
            double totalDep = depenseService.getAllDepenses().stream()
                    .mapToDouble(Depense::getMontant).sum();
            double totalRev = revenuService.getAllRevenus().stream()
                    .mapToDouble(Revenu::getMontant).sum();
            double profit = totalRev - totalDep;

            lblTotalDepense.setText(String.format("€%.2f", totalDep));
            lblTotalRevenu.setText(String.format("€%.2f", totalRev));
            lblProfit.setText(String.format("€%.2f", profit));

            if (profit >= 0) {
                lblProfit.setTextFill(Color.web("#1a4d2e"));
            } else {
                lblProfit.setTextFill(Color.web("#dc2626"));
            }

        } catch (SQLException e) {
            lblStatus.setText("❌ Erreur: " + e.getMessage());
        }
    }

    private void setActiveButton(Button activeBtn) {
        btnDashboard.setStyle(INACTIVE_STYLE);
        btnListDepense.setStyle(INACTIVE_STYLE);
        btnListRevenu.setStyle(INACTIVE_STYLE);

        btnKPI.setStyle(INACTIVE_STYLE);

        activeBtn.setStyle(ACTIVE_STYLE);
        if (activeBtn == btnDashboard) {
            btnDashboard.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
        } else if (activeBtn == btnListDepense) {
            btnListDepense.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
        } else if (activeBtn == btnListRevenu) {
            btnListRevenu.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");

        } else if (activeBtn == btnKPI) {
            btnKPI.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
        }
    }

    @FXML
    private void openAddDepenseWindow() {
        openModal("/view/DepenseView.fxml", "Ajouter Dépense", "#1a4d2e");
        if (currentView.equals("depenses")) {
            loadDepenses();
        }
        updateDashboard();
    }

    @FXML
    private void openAddRevenuWindow() {
        openModal("/view/RevenuView.fxml", "Ajouter Revenu", "#1a4d2e");
        if (currentView.equals("revenus")) {
            loadRevenus();
        }
        updateDashboard();
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

    @FXML
    private void loadDepenses() {
        lblTitle.setText("Gestion des Dépenses");
        lblTableTitle.setText("Liste des Dépenses");
        currentView = "depenses";

        // Afficher la table, cacher KPIs
        tableSection.setVisible(true);
        tableSection.setManaged(true);
        kpiSection.setVisible(false);
        kpiSection.setManaged(false);

        btnAddDepense.setVisible(true);
        btnOCRScan.setVisible(true);
        btnAddRevenu.setVisible(false);
        setActiveButton(btnListDepense);

        try {
            var depenses = depenseService.getAllDepenses();
            tableView.getColumns().clear();
            tableView.getItems().clear();

            TableColumn<Depense, Long> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("idDepense"));
            colId.setPrefWidth(60);

            TableColumn<Depense, Double> colMontant = new TableColumn<>("Montant");
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setPrefWidth(100);
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

            TableColumn<Depense, String> colType = new TableColumn<>("Catégorie");
            colType.setCellValueFactory(new PropertyValueFactory<>("typeDepense"));
            colType.setPrefWidth(120);

            TableColumn<Depense, String> colDesc = new TableColumn<>("Description");
            colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
            colDesc.setPrefWidth(200);
            colDesc.setCellFactory(column -> new TableCell<Depense, String>() {
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

            TableColumn<Depense, Date> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateDepense"));
            colDate.setPrefWidth(100);

            TableColumn<Depense, Void> colAction = new TableColumn<>("Actions");
            colAction.setPrefWidth(120);
            colAction.setCellFactory(column -> new TableCell<Depense, Void>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setGraphic(null);
                    else {
                        HBox hbox = new HBox(8);
                        hbox.setAlignment(Pos.CENTER);
                        Depense depense = getTableView().getItems().get(getIndex());

                        StackPane viewBtn = createCircleButton("👁", "#1a4d2e");
                        viewBtn.setOnMouseClicked(e -> showDepenseDetails(depense));

                        StackPane editBtn = createCircleButton("✏", "#f59e0b");
                        editBtn.setOnMouseClicked(e -> openEditDepenseWindow(depense));

                        StackPane deleteBtn = createCircleButton("🗑", "#dc2626");
                        deleteBtn.setOnMouseClicked(e -> deleteDepense(depense));

                        hbox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                        setGraphic(hbox);
                    }
                }
            });

            String headerStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 12;";
            colId.setStyle(headerStyle);
            colMontant.setStyle(headerStyle);
            colType.setStyle(headerStyle);
            colDesc.setStyle(headerStyle);
            colDate.setStyle(headerStyle);
            colAction.setStyle(headerStyle);

            tableView.getColumns().addAll(colId, colMontant, colType, colDesc, colDate, colAction);
            tableView.setItems(FXCollections.observableArrayList(depenses));
            lblStatus.setText("📋 " + depenses.size() + " dépenses chargées");

        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void testOCR() {
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
            String descriptionRevenu = extractDescriptionRevenu(cleaned);
            double montant = extractMontant(cleaned);

            if (montant <= 0) {
                showError("Erreur", "Montant invalide !");
                return;
            }

            if ("depenses".equals(currentView)) {
                String type = extractType(cleaned);
                Depense depense = new Depense();
                depense.setTypeDepense(type);
                depense.setDateDepense(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                depense.setDescription(description);
                depense.setMontant(montant);
                depenseService.ajouterDepense(depense);
                loadDepenses();
            } else if ("revenus".equals(currentView)) {
                String source = extractSource(cleaned);
                Revenu revenu = new Revenu();
                revenu.setSource(source);
                revenu.setDateRevenu(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                revenu.setDescription(descriptionRevenu);
                revenu.setMontant(montant);
                revenuService.ajouterRevenu(revenu);
                loadRevenus();
            } else {
                showError("Erreur", "Veuillez d'abord sélectionner une liste (dépenses ou revenus)");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("OCR Extracted Data");
            alert.setHeaderText("Parsed Receipt");
            String content = "Date: " + date + "\nDescription: " + description + "\nMontant: " + montant;
            if ("depenses".equals(currentView)) {
                content = "Type: " + extractType(cleaned) + "\n" + content;
            } else {
                content = "Source: " + extractSource(cleaned) + "\n" + content;
            }
            alert.setContentText(content);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("OCR failed", e.getMessage());
        }
    }

    @FXML
    private void loadRevenus() {
        lblTitle.setText("Gestion des Revenus");
        lblTableTitle.setText("Liste des Revenus");
        currentView = "revenus";

        // Afficher table, cacher KPIs
        tableSection.setVisible(true);
        tableSection.setManaged(true);
        kpiSection.setVisible(false);
        kpiSection.setManaged(false);

        btnAddDepense.setVisible(false);
        btnOCRScan.setVisible(true);
        btnAddRevenu.setVisible(true);
        setActiveButton(btnListRevenu);

        try {
            var revenus = revenuService.getAllRevenus();
            tableView.getColumns().clear();
            tableView.getItems().clear();

            TableColumn<Revenu, Long> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("idRevenu"));
            colId.setPrefWidth(60);

            TableColumn<Revenu, Double> colMontant = new TableColumn<>("Montant");
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setPrefWidth(100);
            colMontant.setCellFactory(column -> new TableCell<Revenu, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else {
                        setText(String.format("€%.2f", item));
                        setStyle("-fx-text-fill: #1a4d2e; -fx-font-weight: bold;");
                    }
                }
            });

            TableColumn<Revenu, String> colSource = new TableColumn<>("Source");
            colSource.setCellValueFactory(new PropertyValueFactory<>("source"));
            colSource.setPrefWidth(120);

            TableColumn<Revenu, String> colDesc = new TableColumn<>("Description");
            colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
            colDesc.setPrefWidth(200);
            colDesc.setCellFactory(column -> new TableCell<Revenu, String>() {
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

            TableColumn<Revenu, Date> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateRevenu"));
            colDate.setPrefWidth(100);

            TableColumn<Revenu, Void> colAction = new TableColumn<>("Actions");
            colAction.setPrefWidth(120);
            colAction.setCellFactory(column -> new TableCell<Revenu, Void>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setGraphic(null);
                    else {
                        HBox hbox = new HBox(8);
                        hbox.setAlignment(Pos.CENTER);
                        Revenu revenu = getTableView().getItems().get(getIndex());

                        StackPane viewBtn = createCircleButton("👁", "#1a4d2e");
                        viewBtn.setOnMouseClicked(e -> showRevenuDetails(revenu));

                        StackPane editBtn = createCircleButton("✏", "#f59e0b");
                        editBtn.setOnMouseClicked(e -> openEditRevenuWindow(revenu));

                        StackPane deleteBtn = createCircleButton("🗑", "#dc2626");
                        deleteBtn.setOnMouseClicked(e -> deleteRevenu(revenu));

                        hbox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                        setGraphic(hbox);
                    }
                }
            });

            String headerStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 12;";
            colId.setStyle(headerStyle);
            colMontant.setStyle(headerStyle);
            colSource.setStyle(headerStyle);
            colDesc.setStyle(headerStyle);
            colDate.setStyle(headerStyle);
            colAction.setStyle(headerStyle);

            tableView.getColumns().addAll(colId, colMontant, colSource, colDesc, colDate, colAction);
            tableView.setItems(FXCollections.observableArrayList(revenus));
            lblStatus.setText("📋 " + revenus.size() + " revenus chargés");

        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void showKPI() {
        lblTitle.setText("Indicateurs de Performance");
        lblTableTitle.setText(""); // cacher le titre de la table
        currentView = "kpi";

        // Cacher la table, afficher la section KPI
        tableSection.setVisible(false);
        tableSection.setManaged(false);
        kpiSection.setVisible(true);
        kpiSection.setManaged(true);

        btnAddDepense.setVisible(false);
        btnAddRevenu.setVisible(false);
        btnOCRScan.setVisible(false);
        setActiveButton(btnKPI);

        // Charger la vue KPI si ce n'est pas déjà fait
        if (kpiSection.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/KPIView.fxml"));
                Parent kpiRoot = loader.load();
                kpiSection.getChildren().add(kpiRoot);
            } catch (Exception e) {
                showError("Erreur", "Impossible de charger les KPIs : " + e.getMessage());
            }
        }
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

    private void showDepenseDetails(Depense depense) {
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

    private void showRevenuDetails(Revenu revenu) {
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

    private void openEditDepenseWindow(Depense depense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DepenseView.fxml"));
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
            updateDashboard();
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void openEditRevenuWindow(Revenu revenu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RevenuView.fxml"));
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
            updateDashboard();
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
                updateDashboard();
                lblStatus.setText("✅ Dépense supprimée");
            } catch (SQLException e) {
                showError("Erreur SQL", e.getMessage());
            }
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
                updateDashboard();
                lblStatus.setText("✅ Revenu supprimé");
            } catch (SQLException e) {
                showError("Erreur SQL", e.getMessage());
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private String extractDescriptionRevenu(String text) {
        Pattern pattern = Pattern.compile("(?i)DESCRIPTION\\s*:?\\s*(.*?)(?=\\s*(?:SOURCE|DATE|MONTANT|$))", Pattern.DOTALL);
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

    private String extractSource(String text) {
        Pattern pattern = Pattern.compile("(?i)(?:SOURCE|Source)\\s*:?\\s*(.*?)(?=\\s*(?:DATE|DESCRIPTION|MONTANT|$))", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group(1).trim();
        return "No Source";
    }

    private void updateExchangeRates() {
        try {
            double tndToEur = currencyService.getTndToEurRate();
            double tndToUsd = currencyService.getTndToUsdRate();
            double eurToTnd = 1.0 / tndToEur;
            double usdToTnd = 1.0 / tndToUsd;

            // Beautiful formatted display
            String ratesText = String.format(
                    "🇪🇺 1 EUR = %.4f TND  •  🇺🇸 1 USD = %.4f TND  •  🇹🇳 1 TND = €%.4f",
                    eurToTnd, usdToTnd, tndToEur
            );

            lblRates.setText(ratesText);
            lblRates.setStyle("-fx-text-fill: rgba(255,255,255,0.95); -fx-font-size: 14px; -fx-font-weight: 500;");

        } catch (Exception e) {
            lblRates.setText("⚠ Taux de change temporairement indisponibles");
            lblRates.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");
            e.printStackTrace();
        }
    }
    private void loadRecentTransactions() {
        try {
            // 1. Récupérer les 5 dernières dépenses et 5 derniers revenus via les services
            List<Depense> depenses = depenseService.getRecentDepenses(5);
            List<Revenu> revenus = revenuService.getRecentRevenus(5);

            // 2. Construire une liste brute d'objets [type, description, montant, date]
            List<Object[]> rawList = new ArrayList<>();
            for (Depense d : depenses) {
                rawList.add(new Object[]{
                        "Dépense",
                        d.getDescription() != null ? d.getDescription() : "",
                        d.getMontant(),
                        d.getDateDepense()
                });
            }
            for (Revenu r : revenus) {
                rawList.add(new Object[]{
                        "Revenu",
                        r.getDescription() != null ? r.getDescription() : "",
                        r.getMontant(),
                        r.getDateRevenu()
                });
            }

            // 3. Trier par date décroissante (index 3 = date)
            rawList.sort((a, b) -> ((Date) b[3]).compareTo((Date) a[3]));

            // 4. Garder seulement les 10 plus récentes
            if (rawList.size() > 10) {
                rawList = rawList.subList(0, 10);
            }

            // 5. Vider complètement la table et ses colonnes
            tableView.getColumns().clear();
            tableView.getItems().clear();

            // 6. Créer les colonnes spécifiques pour les transactions récentes
            TableColumn<Object[], String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0])
            );

            TableColumn<Object[], String> colDesc = new TableColumn<>("Description");
            colDesc.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1])
            );
            colDesc.setPrefWidth(250); // Ajustez si nécessaire

            TableColumn<Object[], Double> colMontant = new TableColumn<>("Montant");
            colMontant.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleDoubleProperty((Double) cellData.getValue()[2]).asObject()
            );
            colMontant.setCellFactory(column -> new TableCell<Object[], Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("€%.2f", item));
                        // Colorer selon le type (dépense en rouge, revenu en vert)
                        Object[] row = getTableView().getItems().get(getIndex());
                        if ("Dépense".equals(row[0])) {
                            setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #1a4d2e; -fx-font-weight: bold;");
                        }
                    }
                }
            });

            TableColumn<Object[], Date> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>((Date) cellData.getValue()[3])
            );

            // Style des en-têtes
            String headerStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 12;";
            colType.setStyle(headerStyle);
            colDesc.setStyle(headerStyle);
            colMontant.setStyle(headerStyle);
            colDate.setStyle(headerStyle);

            // 7. Ajouter les colonnes à la table
            tableView.getColumns().addAll(colType, colDesc, colMontant, colDate);

            // 8. Peupler la table avec les données
            ObservableList<Object[]> data = FXCollections.observableArrayList(rawList);
            tableView.setItems(data);

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les transactions récentes : " + e.getMessage());
            e.printStackTrace();
        }
    }}