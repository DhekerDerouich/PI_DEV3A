package tn.esprit.farmvision.com.pi.controller;

import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.com.pi.dao.DatabaseConnection;
import tn.esprit.farmvision.com.pi.service.AlertesService;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatus;
    @FXML private Label alerteCount;
    @FXML private Button alertesButton;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    @FXML private ListView<AlertesService.Alerte> alertesListView;
    @FXML private Label compteurAlertes;

    // Éléments du menu Finance
    @FXML private VBox financeSubMenu;
    @FXML private Button financeBtn;
    @FXML private Button financeDashboardBtn;
    @FXML private Button financeDepensesBtn;
    @FXML private Button financeRevenusBtn;
    @FXML private Button financeKPIBtn;
    @FXML private Label financeArrow;

    // Éléments du menu Stock & Marketplace
    @FXML private VBox stockSubMenu;
    @FXML private Button stockBtn;
    @FXML private Button stockGestionBtn;
    @FXML private Button stockMarketplaceBtn;
    @FXML private Label stockArrow;

    private boolean financeMenuOpen = false;
    private boolean stockMenuOpen = false;

    private AlertesService alertesService = new AlertesService();
    private ObservableList<AlertesService.Alerte> alertesObservables = FXCollections.observableArrayList();
    private String filtreActuel = "TOUTES";
    private Timer timer;

    private final String[] FXML_PATHS = {
            "/com/pi/view/",
            "/tn/esprit/farmvision/com/pi/view/",
            "/fxml/",
            "/tn/esprit/farmvision/resources1/fxml/",
            "/resources1/fxml/",
            "/finance/view/",
            "/Finance/view/",
            "/gestionstock/",
            "/"
    };

    @FXML
    public void initialize() {
        try {
            System.out.println("=== Initialisation du MainController ===");

            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                String role = currentUser.getClass().getSimpleName();
                String roleDisplay = switch(role) {
                    case "Administrateur" -> "Administrateur";
                    case "ResponsableExploitation" -> "Responsable";
                    case "Agriculteur" -> "Agriculteur";
                    default -> role;
                };

                if (userNameLabel != null) {
                    userNameLabel.setText(currentUser.getNomComplet());
                }
                if (userRoleLabel != null) {
                    userRoleLabel.setText(roleDisplay + " • En ligne");
                }

                System.out.println("👤 Connecté: " + currentUser.getNomComplet() + " (" + roleDisplay + ")");
            }

            setupListView();
            chargerAlertes();
            demarrerRafraichissementAutomatique();

            if (DatabaseConnection.testConnection()) {
                connectionStatus.setText("✅ Connecté à la base");
                connectionStatus.setStyle("-fx-text-fill: #2ecc71;");
            } else {
                connectionStatus.setText("❌ Non connecté");
                connectionStatus.setStyle("-fx-text-fill: #e74c3c;");
            }

            statusLabel.setText("Prêt");
            showEquipementView();

            // Initialiser les sous-menus comme fermés
            financeSubMenu.setVisible(false);
            financeSubMenu.setManaged(false);
            stockSubMenu.setVisible(false);
            stockSubMenu.setManaged(false);

        } catch (Exception e) {
            System.err.println("❌ Erreur dans initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== GESTION DU MENU FINANCE ==========

    @FXML
    private void toggleFinanceMenu() {
        financeMenuOpen = !financeMenuOpen;
        financeSubMenu.setVisible(financeMenuOpen);
        financeSubMenu.setManaged(financeMenuOpen);
        financeArrow.setText(financeMenuOpen ? "▲" : "▼");

        if (financeMenuOpen) {
            financeSubMenu.setOpacity(0);
            financeSubMenu.setTranslateY(-10);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), financeSubMenu);
            ft.setFromValue(0);
            ft.setToValue(1);

            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(200), financeSubMenu);
            tt.setFromY(-10);
            tt.setToY(0);

            javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
            pt.play();
        }
    }

    @FXML
    private void showFinanceMain() {
        loadFinanceView("MainView.fxml", "Tableau de Bord Financier");
    }

    @FXML
    private void showFinanceDepenses() {
        loadFinanceView("DepenseView.fxml", "Gestion des Dépenses");
    }

    @FXML
    private void showFinanceRevenus() {
        loadFinanceView("RevenuView.fxml", "Gestion des Revenus");
    }

    @FXML
    private void showFinanceKPI() {
        loadFinanceView("KPIView.fxml", "Statistiques Financières");
    }

    private void loadFinanceView(String fxmlFile, String title) {
        try {
            String[] chemins = {
                    "/finance/view/" + fxmlFile,
                    "/Finance/view/" + fxmlFile,
                    "/view/" + fxmlFile
            };

            URL url = null;
            for (String chemin : chemins) {
                url = getClass().getResource(chemin);
                if (url != null) break;
            }

            if (url == null) {
                showError("Erreur", "Fichier " + fxmlFile + " introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            statusLabel.setText(title);

        } catch (Exception e) {
            showError("Erreur de chargement", e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== GESTION DU MENU STOCK & MARKETPLACE ==========

    @FXML
    private void toggleStockMenu() {
        stockMenuOpen = !stockMenuOpen;
        stockSubMenu.setVisible(stockMenuOpen);
        stockSubMenu.setManaged(stockMenuOpen);
        stockArrow.setText(stockMenuOpen ? "▲" : "▼");

        if (stockMenuOpen) {
            stockSubMenu.setOpacity(0);
            stockSubMenu.setTranslateY(-10);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), stockSubMenu);
            ft.setFromValue(0);
            ft.setToValue(1);

            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(200), stockSubMenu);
            tt.setFromY(-10);
            tt.setToY(0);

            javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
            pt.play();
        }
    }

    @FXML
    private void showGestionStock() {
        loadViewInContentPane("gestion_stock.fxml", "Gestion des Stocks");
    }

    @FXML
    private void showMarketplace() {
        loadViewInContentPane("gestion_marketplace.fxml", "Marketplace");
    }

    // ========== MÉTHODE UNIFIÉE POUR CHARGER LES VUES ==========

    private void loadViewInContentPane(String fxmlFile, String title) {
        try {
            System.out.println("📦 Chargement de: " + fxmlFile + " dans le contentPane");

            String[] chemins = {
                    "/tn/esprit/farmvision/resources1/fxml/" + fxmlFile,
                    "/resources1/fxml/" + fxmlFile,
                    "/fxml/" + fxmlFile,
                    "/" + fxmlFile
            };

            URL url = null;
            for (String chemin : chemins) {
                url = getClass().getResource(chemin);
                System.out.println("Test: " + chemin + " → " + (url != null ? "✅" : "❌"));
                if (url != null) break;
            }

            if (url == null) {
                showError("Erreur", "Fichier " + fxmlFile + " introuvable!");
                return;
            }

            System.out.println("✅ Fichier trouvé: " + url);

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            statusLabel.setText(title);

        } catch (Exception e) {
            showError("Erreur de chargement", e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== GESTION DES ALERTES ==========

    private void setupListView() {
        if (alertesListView == null) return;
        alertesListView.setItems(alertesObservables);

        alertesListView.setCellFactory(listView -> new ListCell<AlertesService.Alerte>() {
            @Override
            protected void updateItem(AlertesService.Alerte alerte, boolean empty) {
                super.updateItem(alerte, empty);

                if (empty || alerte == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    vbox.setStyle("-fx-padding: 10; -fx-background-color: " + getCouleurFond(alerte) + "; -fx-background-radius: 5;");

                    HBox header = new HBox(10);
                    Label icon = new Label(getIcone(alerte));
                    Label titre = new Label(alerte.getTitre());
                    titre.setStyle("-fx-font-weight: bold;");

                    Region spacer = new Region();
                    spacer.setPrefWidth(Double.MAX_VALUE);
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label date = new Label(alerte.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
                    date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");

                    header.getChildren().addAll(icon, titre, spacer, date);

                    Label message = new Label(alerte.getMessage());
                    message.setWrapText(true);
                    message.setStyle("-fx-font-size: 12px;");

                    vbox.getChildren().addAll(header, message);

                    if (alerte.getAction() != null) {
                        Button actionBtn = new Button("Voir détails");
                        actionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                        actionBtn.setOnAction(e -> alerte.getAction().run());

                        HBox buttonBox = new HBox(actionBtn);
                        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        vbox.getChildren().add(buttonBox);
                    }

                    setGraphic(vbox);
                }
            }

            private String getCouleurFond(AlertesService.Alerte alerte) {
                switch (alerte.getType()) {
                    case "URGENT": return "#ffebee";
                    case "WARNING": return "#fff3e0";
                    default: return "#ffffff";
                }
            }

            private String getIcone(AlertesService.Alerte alerte) {
                switch (alerte.getType()) {
                    case "URGENT": return "🔴";
                    case "WARNING": return "⚠️";
                    default: return "ℹ️";
                }
            }
        });
    }

    private void chargerAlertes() {
        List<AlertesService.Alerte> toutesAlertes = alertesService.getToutesLesAlertes();

        List<AlertesService.Alerte> filtrees = toutesAlertes.stream()
                .filter(a -> correspondFiltre(a))
                .collect(Collectors.toList());

        alertesObservables.setAll(filtrees);
        if (compteurAlertes != null) {
            compteurAlertes.setText(String.valueOf(toutesAlertes.size()));
        }

        if (toutesAlertes.stream().anyMatch(a -> "URGENT".equals(a.getType()))) {
            afficherNotificationSysteme();
        }
    }

    private boolean correspondFiltre(AlertesService.Alerte alerte) {
        switch (filtreActuel) {
            case "URGENTES":
                return "URGENT".equals(alerte.getType());
            case "MAINTENANCES":
                return alerte.getTitre().contains("Maintenance");
            default:
                return true;
        }
    }

    private void afficherNotificationSysteme() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Alertes urgentes");
            alert.setHeaderText("Des actions urgentes requièrent votre attention");
            alert.setContentText("Consultez le centre de notifications pour plus de détails.");
            alert.showAndWait();
        });
    }

    private void demarrerRafraichissementAutomatique() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> chargerAlertes());
            }
        }, 60000, 60000);
    }

    @FXML
    private void rafraichir() {
        chargerAlertes();
    }

    @FXML
    private void filtrerToutes() {
        filtreActuel = "TOUTES";
        chargerAlertes();
    }

    @FXML
    private void filtrerUrgentes() {
        filtreActuel = "URGENTES";
        chargerAlertes();
    }

    @FXML
    private void filtrerMaintenances() {
        filtreActuel = "MAINTENANCES";
        chargerAlertes();
    }

    @FXML
    private void toutMarquerLu() {
        if (compteurAlertes != null) {
            compteurAlertes.setText("0");
        }
    }

    // ========== NAVIGATION PRINCIPALE ==========

    @FXML
    private void showDashboard() {
        contentPane.getChildren().clear();
        statusLabel.setText("Accueil");

        // Fermer les sous-menus
        financeMenuOpen = false;
        financeSubMenu.setVisible(false);
        financeSubMenu.setManaged(false);
        financeArrow.setText("▼");

        stockMenuOpen = false;
        stockSubMenu.setVisible(false);
        stockSubMenu.setManaged(false);
        stockArrow.setText("▼");
    }

    @FXML
    private void showEquipementView() {
        loadView("equipement.fxml", "Gestion des équipements");
    }

    @FXML
    private void showMaintenanceView() {
        loadView("maintenance.fxml", "Gestion des maintenances");
    }

    @FXML
    private void openCalendrier() {
        try {
            URL url = getFXMLResource("CalendrierMaintenance.fxml");
            if (url == null) {
                showError("Erreur", "Fichier CalendrierMaintenance.fxml introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
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

    @FXML
    private void openAlertes() {
        try {
            URL url = getFXMLResource("AlertesView.fxml");
            if (url == null) {
                showError("Erreur", "Fichier AlertesView.fxml introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
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

    private void loadView(String fxmlFile, String statusText) {
        try {
            URL resource = getFXMLResource(fxmlFile);

            if (resource == null) {
                showError("Fichier introuvable", "Le fichier " + fxmlFile + " n'existe pas.");
                return;
            }

            System.out.println("Chargement de : " + resource.toExternalForm());

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            statusLabel.setText(statusText);

            // Fermer les sous-menus
            financeMenuOpen = false;
            financeSubMenu.setVisible(false);
            financeSubMenu.setManaged(false);
            financeArrow.setText("▼");

            stockMenuOpen = false;
            stockSubMenu.setVisible(false);
            stockSubMenu.setManaged(false);
            stockArrow.setText("▼");

        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            showError("Erreur de chargement",
                    "Erreur lors du chargement de " + fxmlFile + ":\n" + cause.getMessage());
            e.printStackTrace();
        }
    }

    private URL getFXMLResource(String fileName) {
        for (String path : FXML_PATHS) {
            URL url = getClass().getResource(path + fileName);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    // ========== DÉCONNEXION ==========

    @FXML
    private void logout() {
        try {
            System.out.println("🚪 Déconnexion en cours...");

            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            Stage stage = (Stage) contentPane.getScene().getWindow();
            SessionManager.getInstance().saveWindowState(
                    stage.getWidth(), stage.getHeight(),
                    stage.isMaximized(), stage.isFullScreen(),
                    stage.getX(), stage.getY()
            );

            SessionManager.getInstance().logout();

            URL loginUrl = getFXMLResource("Login.fxml");
            if (loginUrl == null) {
                loginUrl = getClass().getResource("/fxml/Login.fxml");
            }

            if (loginUrl != null) {
                Parent root = FXMLLoader.load(loginUrl);
                stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
                stage.show();
                System.out.println("✅ Déconnexion réussie");
            } else {
                System.err.println("❌ Fichier Login.fxml introuvable");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de se déconnecter: " + e.getMessage());
        }
    }

    // ========== UTILITAIRES ==========

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void mettreAJourCompteurAlertes() {
        try {
            if (alerteCount == null) {
                return;
            }

            AlertesService alertesService = new AlertesService();
            int nbAlertes = alertesService.getToutesLesAlertes().size();
            alerteCount.setText(String.valueOf(nbAlertes));

            if (nbAlertes > 0) {
                alerteCount.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
                if (alertesButton != null) {
                    alertesButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-background-radius: 20;");
                }
            } else {
                alerteCount.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");
                if (alertesButton != null) {
                    alertesButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-background-radius: 20;");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur dans mettreAJourCompteurAlertes: " + e.getMessage());
        }
    }

    // Méthodes pour compatibilité
    @FXML
    private void openFinance() {
        toggleFinanceMenu();
        showFinanceMain();
    }
}