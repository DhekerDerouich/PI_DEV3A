package tn.esprit.farmvision.gestionuser.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.DashboardAIService;
import tn.esprit.farmvision.gestionuser.service.DashboardAIService.*;
import tn.esprit.farmvision.gestionuser.service.DashboardStatsService;
import tn.esprit.farmvision.gestionuser.service.DashboardStatsService.DashboardStats;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import tn.esprit.farmvision.gestionuser.service.EmailService;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {

    // Header
    @FXML private Label lblWelcome;
    @FXML private BorderPane rootPane;

    // Cartes statistiques
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblPendingUsers;
    @FXML private Label lblNewUsers;
    @FXML private Label lblAgriculteurs;
    @FXML private Label lblResponsables;
    @FXML private Label lblAdmins;
    @FXML private Label lblActivationRate;

    // Graphiques
    @FXML private PieChart pieChartDistribution;
    @FXML private LineChart<String, Number> lineChartTrend;

    // Listes
    @FXML private ListView<String> listRecentUsers;
    @FXML private ListView<String> listPendingUsers;

    // ========== ÉLÉMENTS AI ==========
    @FXML private Label lblPredict7Days;
    @FXML private Label lblPredict30Days;
    @FXML private Label lblAIConfidence;
    @FXML private Label lblGrowthTrend;
    @FXML private ListView<String> listAnomalies;
    @FXML private ListView<String> listRecommendations;
    @FXML private HBox boxInsights;

    private DashboardStatsService statsService;
    private DashboardAIService aiService;

    @FXML
    private void initialize() {
        // Vérifier les permissions admin
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Administrateur)) {
            lblWelcome.setText("⛔ Accès refusé - Réservé aux administrateurs");
            lblWelcome.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }

        lblWelcome.setText("👋 Bienvenue " + currentUser.getNomComplet());

        // Animation d'entrée
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
        }

        // Initialiser les services
        statsService = new DashboardStatsService();
        aiService = new DashboardAIService();

        // Charger les statistiques et l'AI
        loadAllData();
    }

    /**
     * Charge toutes les données (Stats + AI)
     */
    private void loadAllData() {
        lblWelcome.setText("🔄 Chargement des données...");

        new Thread(() -> {
            try {
                // 1. Statistiques classiques
                DashboardStats stats = statsService.getMainStats();
                Map<String, Integer> distribution = statsService.getUserDistribution();
                Map<String, Integer> trend = statsService.getRegistrationTrend();
                List<Utilisateur> recentUsers = statsService.getRecentUsers(5);
                List<Utilisateur> pendingUsers = statsService.getPendingUsers();
                double activationRate = statsService.getActivationRate();

                // 2. AI - Prédictions
                GrowthPrediction prediction7 = aiService.predictGrowth(7);
                GrowthPrediction prediction30 = aiService.predictGrowth(30);

                // 3. AI - Anomalies et Recommandations
                List<Anomaly> anomalies = aiService.detectAnomalies();
                List<Recommendation> recommendations = aiService.generateRecommendations();
                List<Insight> insights = aiService.generateInsights();

                // Mettre à jour l'UI
                Platform.runLater(() -> {
                    Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
                    lblWelcome.setText("👋 Bienvenue " + currentUser.getNomComplet());

                    // Stats classiques
                    updateStatsCards(stats);
                    updateDistributionChart(distribution);
                    updateTrendChart(trend);
                    updateRecentUsersList(recentUsers);
                    updatePendingUsersList(pendingUsers);
                    updateActivationRate(activationRate);

                    // AI
                    updateAIPredictions(prediction7, prediction30);
                    updateAnomalies(anomalies);
                    updateRecommendations(recommendations);
                    updateInsights(insights);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblWelcome.setText("❌ Erreur de chargement");
                });
            }
        }).start();
    }

    // ========================
    // STATS CLASSIQUES
    // ========================

    private void updateStatsCards(DashboardStats stats) {
        lblTotalUsers.setText(String.valueOf(stats.totalUsers));
        lblActiveUsers.setText(String.valueOf(stats.activeUsers));
        lblPendingUsers.setText(String.valueOf(stats.pendingUsers));
        lblNewUsers.setText(String.valueOf(stats.newUsersLast7Days));
        lblAgriculteurs.setText(String.valueOf(stats.totalAgriculteurs));
        lblResponsables.setText(String.valueOf(stats.totalResponsables));
        lblAdmins.setText(String.valueOf(stats.totalAdmins));
    }

    private void updateDistributionChart(Map<String, Integer> distribution) {
        pieChartDistribution.getData().clear();

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            pieChartDistribution.getData().add(slice);
        }

        pieChartDistribution.getData().forEach(data -> {
            if (data.getName().contains("Agriculteurs")) {
                data.getNode().setStyle("-fx-pie-color: #27ae60;");
            } else if (data.getName().contains("Responsables")) {
                data.getNode().setStyle("-fx-pie-color: #3498db;");
            } else if (data.getName().contains("Administrateurs")) {
                data.getNode().setStyle("-fx-pie-color: #e74c3c;");
            }
        });
    }

    private void updateTrendChart(Map<String, Integer> trend) {
        lineChartTrend.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");

        int count = 0;
        for (Map.Entry<String, Integer> entry : trend.entrySet()) {
            String dateLabel = (count % 5 == 0) ? entry.getKey().substring(5) : "";
            series.getData().add(new XYChart.Data<>(dateLabel, entry.getValue()));
            count++;
        }

        lineChartTrend.getData().add(series);
        lineChartTrend.setCreateSymbols(true);
    }

    private void updateRecentUsersList(List<Utilisateur> users) {
        listRecentUsers.getItems().clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (users.isEmpty()) {
            listRecentUsers.getItems().add("Aucune inscription récente");
        } else {
            for (Utilisateur user : users) {
                String userType = user.getClass().getSimpleName();
                String emoji = getEmojiForUserType(userType);
                String status = user.isActivated() ? "✅" : "⏳";

                String item = String.format("%s %s %s - %s (%s)",
                        emoji, status, user.getNomComplet(), userType,
                        dateFormat.format(user.getDateCreation())
                );

                listRecentUsers.getItems().add(item);
            }
        }
    }

    private void updatePendingUsersList(List<Utilisateur> users) {
        listPendingUsers.getItems().clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (users.isEmpty()) {
            listPendingUsers.getItems().add("✅ Aucun compte en attente");
        } else {
            for (Utilisateur user : users) {
                String userType = user.getClass().getSimpleName();
                String emoji = getEmojiForUserType(userType);

                String item = String.format("%s ⏳ %s - %s (%s)",
                        emoji, user.getNomComplet(), userType,
                        dateFormat.format(user.getDateCreation())
                );

                listPendingUsers.getItems().add(item);
            }
        }
    }

    private void updateActivationRate(double rate) {
        lblActivationRate.setText(String.format("%.1f%%", rate));

        if (rate >= 80) {
            lblActivationRate.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        } else if (rate >= 50) {
            lblActivationRate.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
        } else {
            lblActivationRate.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        }
    }

    // ========================
    // AI - PRÉDICTIONS
    // ========================

    private void updateAIPredictions(GrowthPrediction pred7, GrowthPrediction pred30) {
        // Prédiction 7 jours
        lblPredict7Days.setText("+" + pred7.predictedNewUsers);

        // Prédiction 30 jours
        lblPredict30Days.setText("+" + pred30.predictedNewUsers);

        // Confiance
        lblAIConfidence.setText(String.format("%.0f%%", pred30.confidence));

        // Tendance
        String trendText;
        String trendEmoji;

        if (pred30.growthRate > 1) {
            trendText = "Croissance forte";
            trendEmoji = "📈";
        } else if (pred30.growthRate > 0.3) {
            trendText = "Croissance stable";
            trendEmoji = "📊";
        } else if (pred30.growthRate > 0) {
            trendText = "Croissance faible";
            trendEmoji = "📉";
        } else {
            trendText = "Stagnation";
            trendEmoji = "⚠️";
        }

        lblGrowthTrend.setText(String.format("%s Tendance: %s (%.2f utilisateurs/jour)",
                trendEmoji, trendText, pred30.growthRate));
    }

    // ========================
    // AI - ANOMALIES
    // ========================

    private void updateAnomalies(List<Anomaly> anomalies) {
        listAnomalies.getItems().clear();

        if (anomalies.isEmpty()) {
            listAnomalies.getItems().add("✅ Aucune anomalie détectée");
        } else {
            for (Anomaly anomaly : anomalies) {
                String severityEmoji = getSeverityEmoji(anomaly.severity);
                String item = String.format("%s %s\n   %s\n   💡 %s",
                        severityEmoji,
                        anomaly.title,
                        anomaly.description,
                        anomaly.recommendation
                );
                listAnomalies.getItems().add(item);
            }
        }
    }

    private String getSeverityEmoji(String severity) {
        switch (severity) {
            case "HIGH": return "🔴";
            case "MEDIUM": return "🟡";
            case "LOW": return "🟢";
            default: return "⚪";
        }
    }

    // ========================
    // AI - RECOMMANDATIONS
    // ========================

    private void updateRecommendations(List<Recommendation> recommendations) {
        listRecommendations.getItems().clear();

        if (recommendations.isEmpty()) {
            listRecommendations.getItems().add("✅ Tout est optimal");
        } else {
            for (Recommendation rec : recommendations) {
                String priorityEmoji = getPriorityEmoji(rec.priority);
                String item = String.format("%s %s\n   %s\n   📊 Impact: %s",
                        priorityEmoji,
                        rec.title,
                        rec.description,
                        rec.expectedImpact
                );
                listRecommendations.getItems().add(item);
            }
        }
    }

    private String getPriorityEmoji(String priority) {
        switch (priority) {
            case "HIGH": return "🔥";
            case "MEDIUM": return "⚡";
            case "LOW": return "💡";
            default: return "ℹ️";
        }
    }

    // ========================
    // AI - INSIGHTS
    // ========================

    private void updateInsights(List<Insight> insights) {
        boxInsights.getChildren().clear();

        for (Insight insight : insights) {
            VBox insightCard = createInsightCard(insight);
            boxInsights.getChildren().add(insightCard);
        }
    }

    private VBox createInsightCard(Insight insight) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-min-width: 180;"
        );

        Label iconLabel = new Label(insight.icon);
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label valueLabel = new Label(insight.value);
        valueLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label titleLabel = new Label(insight.title);
        titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: white;" +
                        "-fx-opacity: 0.9;"
        );
        titleLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);

        return card;
    }

    // ========================
    // UTILITAIRES
    // ========================

    private String getEmojiForUserType(String userType) {
        switch (userType) {
            case "Agriculteur": return "🌾";
            case "ResponsableExploitation": return "👔";
            case "Administrateur": return "⚙️";
            default: return "👤";
        }
    }

    // ========================
    // ACTIONS
    // ========================

    @FXML
    private void refreshStats() {
        loadAllData();
    }

    @FXML
    private void ouvrirGestionUsers() {
        navigateToGestionUsers(false);
    }

    @FXML
    private void validerComptes() {
        navigateToGestionUsers(true);
    }

    @FXML
    private void ouvrirProfil() {
        navigateToPage("/fxml/Profile.fxml", "FarmVision - Mon Profil");
    }

    @FXML
    private void logout() {
        SessionManager.getInstance().logout();
        navigateToPage("/fxml/Login.fxml", "FarmVision - Connexion");
    }

    // ========================
    // NAVIGATION
    // ========================

    private void navigateToGestionUsers(boolean filterPendingOnly) {
        try {
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GestionUsers.fxml"));
            Parent root = loader.load();

            GestionUsersControllerFX controller = loader.getController();
            if (filterPendingOnly) {
                controller.showPendingAccountsOnly();
            }

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1200,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle("FarmVision - " + (filterPendingOnly ? "Comptes en Attente" : "Gestion des Utilisateurs"));

            if (isMaximized) {
                stage.setMaximized(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToPage(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1200,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle(title);

            if (isMaximized) {
                stage.setMaximized(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}