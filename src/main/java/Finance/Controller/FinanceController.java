package Finance.Controller;

import Finance.model.Depense;
import Finance.model.Revenu;
import Finance.service.depenseService;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

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
    @FXML private Button btnAddRevenu;

    // Add sidebar buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnListDepense;
    @FXML private Button btnListRevenu;
    @FXML private Button btnRapport;

    private depenseService depenseService = new depenseService();
    private revenuService revenuService = new revenuService();
    private String currentView = "dashboard";

    // Sidebar button styles
    private final String ACTIVE_STYLE = "-fx-background-color: #2d6a4f; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 14 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #a8c5b5; -fx-background-radius: 8; -fx-padding: 14 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblStatus.setText("🟢 Système prêt");
        setActiveButton(btnDashboard);
        updateDashboard();
    }

    @FXML
    private void showDashboard() {
        lblTitle.setText("Tableau de Bord Financier");
        lblTableTitle.setText("Dernières Transactions");
        currentView = "dashboard";

        // Clear table
        tableView.getColumns().clear();
        tableView.getItems().clear();

        // Show both buttons
        btnAddDepense.setVisible(true);
        btnAddRevenu.setVisible(true);

        setActiveButton(btnDashboard);
        updateDashboard();
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

    // Method to set active button and deactivate others
    private void setActiveButton(Button activeBtn) {
        // Reset all buttons to inactive
        btnDashboard.setStyle(INACTIVE_STYLE);
        btnListDepense.setStyle(INACTIVE_STYLE);
        btnListRevenu.setStyle(INACTIVE_STYLE);
        btnRapport.setStyle(INACTIVE_STYLE);

        // Set the clicked button as active
        activeBtn.setStyle(ACTIVE_STYLE);

        // Update font weight for active button
        if (activeBtn == btnDashboard) {
            btnDashboard.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
        } else if (activeBtn == btnListDepense) {
            btnListDepense.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
        } else if (activeBtn == btnListRevenu) {
            btnListRevenu.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
        } else if (activeBtn == btnRapport) {
            btnRapport.setStyle(ACTIVE_STYLE + " -fx-font-weight: bold;");
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
        btnAddDepense.setVisible(true);
        btnAddRevenu.setVisible(false);

        setActiveButton(btnListDepense);

        try {
            var depenses = depenseService.getAllDepenses();

            tableView.getColumns().clear();
            tableView.getItems().clear();

            TableColumn colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("idDepense"));
            colId.setPrefWidth(60);

            TableColumn colMontant = new TableColumn<>("Montant");
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setPrefWidth(100);
            colMontant.setCellFactory(column -> new TableCell<Depense, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("€%.2f", item));
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            });

            TableColumn colType = new TableColumn<>("Catégorie");
            colType.setCellValueFactory(new PropertyValueFactory<>("typeDepense"));
            colType.setPrefWidth(120);

            TableColumn colDesc = new TableColumn<>("Description");
            colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));  // Changed from typeDepense
            colDesc.setPrefWidth(200);
            colDesc.setCellFactory(column -> new TableCell<Depense, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.trim().isEmpty()) {
                        setText("-");
                        setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                    } else {
                        // Truncate long descriptions
                        String displayText = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                        setText(displayText);
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            });

            TableColumn colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateDepense"));
            colDate.setPrefWidth(100);

            // ACTION COLUMN with CIRCULAR buttons
            TableColumn colAction = new TableColumn<>("Actions");
            colAction.setPrefWidth(120);
            colAction.setCellFactory(column -> new TableCell<Depense, Void>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(8);
                        hbox.setAlignment(Pos.CENTER);

                        StackPane viewBtn = createCircleButton("👁", "#1a4d2e");
                        viewBtn.setOnMouseClicked(e -> {
                            Depense depense = (Depense) getTableView().getItems().get(getIndex());
                            showDepenseDetails(depense);
                        });

                        StackPane editBtn = createCircleButton("✏", "#f59e0b");
                        editBtn.setOnMouseClicked(e -> {
                            Depense depense = (Depense) getTableView().getItems().get(getIndex());
                            openEditDepenseWindow(depense);
                        });

                        StackPane deleteBtn = createCircleButton("🗑", "#dc2626");
                        deleteBtn.setOnMouseClicked(e -> {
                            Depense depense = (Depense) getTableView().getItems().get(getIndex());
                            deleteDepense(depense);
                        });

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
    private void loadRevenus() {
        lblTitle.setText("Gestion des Revenus");
        lblTableTitle.setText("Liste des Revenus");
        currentView = "revenus";
        btnAddDepense.setVisible(false);
        btnAddRevenu.setVisible(true);

        setActiveButton(btnListRevenu);

        try {
            var revenus = revenuService.getAllRevenus();

            tableView.getColumns().clear();
            tableView.getItems().clear();

            TableColumn colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("idRevenu"));
            colId.setPrefWidth(60);

            TableColumn colMontant = new TableColumn<>("Montant");
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setPrefWidth(100);
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

            TableColumn colSource = new TableColumn<>("Source");
            colSource.setCellValueFactory(new PropertyValueFactory<>("source"));
            colSource.setPrefWidth(120);

            TableColumn colDesc = new TableColumn<>("Description");
            colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));  // Changed from source
            colDesc.setPrefWidth(200);
            colDesc.setCellFactory(column -> new TableCell<Revenu, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.trim().isEmpty()) {
                        setText("-");
                        setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                    } else {
                        // Truncate long descriptions
                        String displayText = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                        setText(displayText);
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            });

            TableColumn colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateRevenu"));
            colDate.setPrefWidth(100);

            TableColumn colAction = new TableColumn<>("Actions");
            colAction.setPrefWidth(120);
            colAction.setCellFactory(column -> new TableCell<Revenu, Void>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(8);
                        hbox.setAlignment(Pos.CENTER);

                        StackPane viewBtn = createCircleButton("👁", "#1a4d2e");
                        viewBtn.setOnMouseClicked(e -> {
                            Revenu revenu = (Revenu) getTableView().getItems().get(getIndex());
                            showRevenuDetails(revenu);
                        });

                        StackPane editBtn = createCircleButton("✏", "#f59e0b");
                        editBtn.setOnMouseClicked(e -> {
                            Revenu revenu = (Revenu) getTableView().getItems().get(getIndex());
                            openEditRevenuWindow(revenu);
                        });

                        StackPane deleteBtn = createCircleButton("🗑", "#dc2626");
                        deleteBtn.setOnMouseClicked(e -> {
                            Revenu revenu = (Revenu) getTableView().getItems().get(getIndex());
                            deleteRevenu(revenu);
                        });

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
    private void generateRapport() {
        lblTitle.setText("Statistiques & Rapports");
        lblStatus.setText("📊 Module en développement");
        setActiveButton(btnRapport);
    }

    private void showDepenseDetails(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Dépense");
        alert.setHeaderText("Dépense #" + depense.getIdDepense());

        String description = (depense.getDescription() != null && !depense.getDescription().trim().isEmpty())
                ? depense.getDescription()
                : "Aucune description";

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
                ? revenu.getDescription()
                : "Aucune description";

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
}