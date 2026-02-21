package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.WeatherAlert;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {

    private static NotificationService instance;
    private Map<Integer, Stage> activeNotifications = new HashMap<>();

    private NotificationService() {}

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Show a push notification for weather alert
     */
    public void showNotification(WeatherAlert alert) {
        Platform.runLater(() -> {
            // Don't show duplicate
            if (activeNotifications.containsKey(alert.getIdAlert())) {
                return;
            }

            // Play sound
            java.awt.Toolkit.getDefaultToolkit().beep();

            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.TRANSPARENT);
            notificationStage.setAlwaysOnTop(true);

            activeNotifications.put(alert.getIdAlert(), notificationStage);

            VBox content = createNotificationContent(alert, notificationStage);

            Scene scene = new Scene(content);
            scene.setFill(Color.TRANSPARENT);
            notificationStage.setScene(scene);

            // Position in top-right corner
            notificationStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getMaxX() - 360);
            notificationStage.setY(20);

            notificationStage.show();

            // Auto close after 6 seconds
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(6), e -> closeNotification(alert.getIdAlert()))
            );
            timeline.setCycleCount(1);
            timeline.play();
        });
    }

    /**
     * Create notification UI
     */
    private VBox createNotificationContent(WeatherAlert alert, Stage stage) {
        VBox container = new VBox(10);
        container.setStyle(getStyleForSeverity(alert.getSeverity()));
        container.setMaxWidth(340);
        container.setPadding(new javafx.geometry.Insets(12));

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        String severityIcon = getSeverityIcon(alert.getSeverity());
        Label iconLabel = new Label(severityIcon);
        iconLabel.setStyle("-fx-font-size: 20;");

        Label titleLabel = new Label(alert.getSeverity());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: white;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> closeNotification(alert.getIdAlert()));

        header.getChildren().addAll(iconLabel, titleLabel, spacer, closeBtn);

        // Content
        VBox content = new VBox(5);

        Label parcelleLabel = new Label("📍 " + alert.getParcelleLocalisation());
        parcelleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: white;");

        Label messageLabel = new Label(alert.getMessage());
        messageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: white;");
        messageLabel.setWrapText(true);

        Label tempLabel = new Label(String.format("🌡️ %.1f°C • %s", alert.getTemperature(), alert.getWeatherCondition()));
        tempLabel.setStyle("-fx-font-size: 11; -fx-text-fill: white; -fx-opacity: 0.9;");

        content.getChildren().addAll(parcelleLabel, messageLabel, tempLabel);
        container.getChildren().addAll(header, content);

        return container;
    }

    /**
     * Close a specific notification
     */
    private void closeNotification(int alertId) {
        Stage stage = activeNotifications.remove(alertId);
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Get style based on severity
     */
    private String getStyleForSeverity(String severity) {
        String baseStyle = "-fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);";

        switch (severity) {
            case "ROUGE":
                return baseStyle + " -fx-background-color: #dc2626;";
            case "ORANGE":
                return baseStyle + " -fx-background-color: #ea580c;";
            case "JAUNE":
                return baseStyle + " -fx-background-color: #ca8a04;";
            default:
                return baseStyle + " -fx-background-color: #4b5563;";
        }
    }

    /**
     * Get icon for severity
     */
    private String getSeverityIcon(String severity) {
        switch (severity) {
            case "ROUGE": return "🔴";
            case "ORANGE": return "🟠";
            case "JAUNE": return "🟡";
            default: return "⚪";
        }
    }
}