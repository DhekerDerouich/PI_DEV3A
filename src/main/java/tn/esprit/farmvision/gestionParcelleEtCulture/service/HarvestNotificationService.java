package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;
import tn.esprit.farmvision.gestionParcelleEtCulture.util.GrowthStatus;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HarvestNotificationService {  // Renamed from NotificationService

    private final CultureService cultureService;
    private final CultureStatusService statusService;
    private final Timer notificationTimer;
    //private final SmsService smsService;

    public HarvestNotificationService() {
        this.cultureService = new CultureService();
        this.statusService = new CultureStatusService();
        this.notificationTimer = new Timer(true);
       // this.smsService = new SmsService();
        startNotificationScheduler();
    }

    private void startNotificationScheduler() {
        // Check every hour
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkHarvestNotifications();
            }
        }, 0, 60 * 60 * 1000); // 1 hour
    }

    private void checkHarvestNotifications() {
        try {
            List<Culture> cultures = cultureService.afficher();

            for (Culture culture : cultures) {
                GrowthStatus status = statusService.calculateGrowthStatus(culture);

                // Check for tomorrow's harvest
                if (statusService.isHarvestTomorrow(culture)) {
                    sendHarvestReminder(culture, "demain");
                }

                // Check for today's harvest
                if (statusService.isHarvestToday(culture)) {
                    sendHarvestReminder(culture, "aujourd'hui");
                }

                // Check if harvest is within 3 days and status is READY
                if (statusService.isHarvestWithinDays(culture, 3) && status == GrowthStatus.READY) {
                    long daysUntil = statusService.getDaysUntilHarvest(culture);
                    String when = daysUntil == 0 ? "aujourd'hui" :
                            (daysUntil == 1 ? "demain" : "dans " + daysUntil + " jours");
                    sendHarvestReminder(culture, when);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHarvestReminder(Culture culture, String when) {
        String title = "🔔 Rappel de récolte";
        String message = String.format(
                "La culture '%s' (%s) est prête à être récoltée %s!",
                culture.getNomCulture(),
                culture.getTypeCulture(),
                when
        );

        // Send in-app notification
        Platform.runLater(() -> showInAppNotification(title, message));

        // Send SMS
       // smsService.sendSms("+216XXXXXXXX", message);
    }

    private void showInAppNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the notification
        alert.getDialogPane().setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #22c55e; " +
                        "-fx-border-width: 2;"
        );

        // Auto-close after 5 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            }
        }, 5000);

        alert.show();
    }

    // Method to stop the scheduler when application closes
    public void shutdown() {
        notificationTimer.cancel();
    }
}