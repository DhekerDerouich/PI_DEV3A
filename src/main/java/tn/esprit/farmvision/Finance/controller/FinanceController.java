package tn.esprit.farmvision.Finance.controller;

import tn.esprit.farmvision.Finance.model.Depense;
import tn.esprit.farmvision.Finance.model.Revenu;
import tn.esprit.farmvision.Finance.service.CurrencyService;
import tn.esprit.farmvision.Finance.service.depenseService;
import tn.esprit.farmvision.Finance.service.revenuService;
import tn.esprit.farmvision.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.ZoneId;
import java.util.*;
import java.net.URL;
import java.sql.SQLException;

@SuppressWarnings("unchecked")
public class FinanceController implements Initializable {

    // ========== ÉLÉMENTS DE L'INTERFACE ==========
    @FXML private Label lblTitle;
    @FXML private Label lblTotalDepense;
    @FXML private Label lblTotalRevenu;
    @FXML private Label lblProfit;
    @FXML private Label lblStatus;
    @FXML private Label lblRates;
    @FXML private TableView<Object[]> tableView; // pour les transactions récentes du dashboard

    // Sections
    @FXML private VBox dashboardSection;
    @FXML private VBox dynamicSection;
    @FXML private VBox kpiSection;

    // ========== SERVICES ==========
    private depenseService depenseService = new depenseService();
    private revenuService revenuService = new revenuService();
    private CurrencyService currencyService = new CurrencyService();

    // ========== VARIABLES D'ÉTAT ==========
    private String currentView = "dashboard";

    // ========== INITIALISATION ==========
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérification des droits
        if (!SessionManager.getInstance().isAdmin() && !SessionManager.getInstance().isResponsable()) {
            showError("Accès refusé", "Seuls les administrateurs et responsables peuvent accéder aux finances");
            return;
        }

        lblStatus.setText("🟢 Système prêt");
        updateDashboard();
        updateExchangeRates();

        // Initialiser l'affichage : dashboard par défaut
        showDashboard();
    }

    // ========== VUES PRINCIPALES ==========
    public void showDashboard() {
        lblTitle.setText("Tableau de Bord Financier");
        currentView = "dashboard";

        // Afficher la section dashboard, cacher les autres
        dashboardSection.setVisible(true);
        dashboardSection.setManaged(true);
        dynamicSection.setVisible(false);
        dynamicSection.setManaged(false);
        kpiSection.setVisible(false);
        kpiSection.setManaged(false);

        updateDashboard();
        loadRecentTransactions();
    }

    public void loadDepenses() {
        lblTitle.setText("Gestion des Dépenses");
        currentView = "depenses";

        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        kpiSection.setVisible(false);
        kpiSection.setManaged(false);
        dynamicSection.setVisible(true);
        dynamicSection.setManaged(true);

        loadViewInSection("/tn/esprit/farmvision/Finance/view/DepenseTableView.fxml", "depenses");
    }

    public void loadRevenus() {
        lblTitle.setText("Gestion des Revenus");
        currentView = "revenus";

        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        kpiSection.setVisible(false);
        kpiSection.setManaged(false);
        dynamicSection.setVisible(true);
        dynamicSection.setManaged(true);

        loadViewInSection("/tn/esprit/farmvision/Finance/view/RevenuTableView.fxml", "revenus");
    }

    public void showKPI() {
        lblTitle.setText("Indicateurs de Performance");
        currentView = "kpi";

        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        dynamicSection.setVisible(false);
        dynamicSection.setManaged(false);
        kpiSection.setVisible(true);
        kpiSection.setManaged(true);

        // Charger la vue KPI si ce n'est pas déjà fait
        if (kpiSection.getChildren().isEmpty()) {
            try {
                System.out.println("🔍 Chargement de KPIView.fxml...");

                String[] chemins = {
                        "/Finance/view/KPIView.fxml",
                        "/tn/esprit/farmvision/Finance/view/KPIView.fxml"
                };

                URL url = null;
                for (String chemin : chemins) {
                    url = getClass().getResource(chemin);
                    System.out.println("Test: " + chemin + " → " + (url != null ? "✅" : "❌"));
                    if (url != null) break;
                }

                if (url == null) {
                    showError("Erreur", "Fichier KPIView.fxml introuvable!");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(url);
                Parent kpiRoot = loader.load();
                System.out.println("✅ KPIView.fxml chargé, contrôleur: " + loader.getController());
                kpiSection.getChildren().add(kpiRoot);

            } catch (Exception e) {
                showError("Erreur", "Impossible de charger les KPIs : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ========== CHARGEMENT DYNAMIQUE DES VUES ==========
    private void loadViewInSection(String fxmlPath, String type) {
        try {
            // S'assurer que le chemin commence par /
            if (!fxmlPath.startsWith("/")) {
                fxmlPath = "/" + fxmlPath;
            }

            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showError("Erreur", "Fichier introuvable: " + fxmlPath);
                System.err.println("❌ Fichier non trouvé: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            // Passer la référence du contrôleur principal
            if (type.equals("depenses")) {
                DepenseTableController controller = loader.getController();
                controller.setMainController(this);
            } else if (type.equals("revenus")) {
                RevenuTableController controller = loader.getController();
                controller.setMainController(this);
            }

            dynamicSection.getChildren().clear();
            dynamicSection.getChildren().add(view);

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger la vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== TRANSACTIONS RÉCENTES (TABLEAU DE BORD) ==========
    private void loadRecentTransactions() {
        try {
            List<Depense> depenses = depenseService.getRecentDepenses(5);
            List<Revenu> revenus = revenuService.getRecentRevenus(5);

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

            rawList.sort((a, b) -> ((Date) b[3]).compareTo((Date) a[3]));

            if (rawList.size() > 10) {
                rawList = rawList.subList(0, 10);
            }

            tableView.getColumns().clear();
            tableView.getItems().clear();

            TableColumn<Object[], String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0])
            );

            TableColumn<Object[], String> colDesc = new TableColumn<>("Description");
            colDesc.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1])
            );
            colDesc.setPrefWidth(250);

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

            String headerStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 12;";
            colType.setStyle(headerStyle);
            colDesc.setStyle(headerStyle);
            colMontant.setStyle(headerStyle);
            colDate.setStyle(headerStyle);

            tableView.getColumns().addAll(colType, colDesc, colMontant, colDate);
            ObservableList<Object[]> data = FXCollections.observableArrayList(rawList);
            tableView.setItems(data);

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les transactions récentes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== MISE À JOUR DES DONNÉES DU DASHBOARD ==========
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

    // ========== TAUX DE CHANGE ==========
    private void updateExchangeRates() {
        try {
            double tndToEur = currencyService.getTndToEurRate();
            double tndToUsd = currencyService.getTndToUsdRate();
            double eurToTnd = 1.0 / tndToEur;
            double usdToTnd = 1.0 / tndToUsd;

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

    // ========== FENÊTRES MODALES (FORMULAIRES) ==========
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

    // ========== RAFRAÎCHISSEMENT DES VUES ==========
    private void refreshCurrentView() {
        if ("depenses".equals(currentView)) {
            loadDepenses();
        } else if ("revenus".equals(currentView)) {
            loadRevenus();
        } else if ("dashboard".equals(currentView)) {
            showDashboard();
        }
    }

    // Méthode appelée par les sous-contrôleurs pour rafraîchir le dashboard
    public void refreshDashboard() {
        updateDashboard();
        if ("dashboard".equals(currentView)) {
            loadRecentTransactions();
        }
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