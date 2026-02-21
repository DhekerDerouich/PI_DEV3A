package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.WeatherAlert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class WeatherAlertService {

    private static WeatherAlertService instance;
    private List<WeatherAlert> alerts = new CopyOnWriteArrayList<>();
    private NotificationService notificationService;

    // Alert thresholds
    private static final double TEMP_EXTREME_HOT = 35.0;  // >35°C - Red alert
    private static final double TEMP_HOT = 30.0;          // >30°C - Orange alert
    private static final double TEMP_COLD = 6.0;          // <5°C - Orange alert now testing with 6
    private static final double TEMP_EXTREME_COLD = 0.0;   // <0°C - Red alert

    private WeatherAlertService() {
        notificationService = NotificationService.getInstance();
    }

    public static synchronized WeatherAlertService getInstance() {
        if (instance == null) {
            instance = new WeatherAlertService();
        }
        return instance;
    }

    /**
     * Check all parcelles for bad weather
     */
    public List<WeatherAlert> checkAllParcelles(List<Parcelle> parcelles) {
        List<WeatherAlert> newAlerts = new ArrayList<>();

        for (Parcelle p : parcelles) {
            WeatherAlert alert = checkParcelleWeather(p);
            if (alert != null) {
                // Check if similar alert already exists in last hour
                if (!hasSimilarRecentAlert(alert)) {
                    alerts.add(0, alert); // Add to beginning of list
                    newAlerts.add(alert);

                    // Show push notification
                    notificationService.showNotification(alert);

                    System.out.println("🔔 New alert: " + alert.getSeverity() + " - " + alert.getMessage());
                }
            }
        }

        // Keep only last 100 alerts
        if (alerts.size() > 100) {
            alerts = new CopyOnWriteArrayList<>(alerts.subList(0, 100));
        }

        return newAlerts;
    }

    /**
     * Check a single parcelle for bad weather
     */
    private WeatherAlert checkParcelleWeather(Parcelle p) {
        double temp = p.getTemperature();
        String weather = p.getWeather() != null ? p.getWeather().toLowerCase() : "";

        // Skip if no valid temperature
        if (temp == 0 && "N/A".equals(p.getWeather())) {
            return null;
        }

        // Check for extreme heat
        if (temp >= TEMP_EXTREME_HOT) {
            return new WeatherAlert(
                    p.getIdParcelle(),
                    p.getLocalisation(),
                    "CHALEUR EXTRÊME",
                    "ROUGE",
                    String.format("🔥 %.1f°C! Arrosage urgent recommandé", temp),
                    temp,
                    p.getWeather()
            );
        }

        // Check for high heat
        if (temp >= TEMP_HOT) {
            return new WeatherAlert(
                    p.getIdParcelle(),
                    p.getLocalisation(),
                    "CHALEUR ÉLEVÉE",
                    "ORANGE",
                    String.format("⚠️ %.1f°C. Surveillez l'hydratation", temp),
                    temp,
                    p.getWeather()
            );
        }

        // Check for freezing
        if (temp <= TEMP_EXTREME_COLD) {
            return new WeatherAlert(
                    p.getIdParcelle(),
                    p.getLocalisation(),
                    "GEL",
                    "ROUGE",
                    String.format("❄️ %.1f°C! Protégez vos cultures", temp),
                    temp,
                    p.getWeather()
            );
        }

        // Check for cold
        if (temp <= TEMP_COLD) {
            return new WeatherAlert(
                    p.getIdParcelle(),
                    p.getLocalisation(),
                    "FROID",
                    "ORANGE",
                    String.format("🌡️ %.1f°C. Risque pour cultures fragiles", temp),
                    temp,
                    p.getWeather()
            );
        }

        // Check for storms
        if (weather.contains("thunderstorm") || weather.contains("storm")) {
            return new WeatherAlert(
                    p.getIdParcelle(),
                    p.getLocalisation(),
                    "ORAGE",
                    "ROUGE",
                    "⛈️ Orage détecté! Mettez le matériel à l'abri",
                    temp,
                    p.getWeather()
            );
        }

        // Check for heavy rain
        if (weather.contains("rain") && weather.contains("heavy")) {
            return new WeatherAlert(
                    p.getIdParcelle(),
                    p.getLocalisation(),
                    "PLUIE INTENSE",
                    "JAUNE",
                    "🌧️ Forte pluie. Surveillez l'humidité",
                    temp,
                    p.getWeather()
            );
        }

        return null;
    }

    /**
     * Check if similar alert was created in the last hour
     */
    private boolean hasSimilarRecentAlert(WeatherAlert newAlert) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        return alerts.stream().anyMatch(alert ->
                alert.getIdParcelle() == newAlert.getIdParcelle() &&
                        alert.getAlertType().equals(newAlert.getAlertType()) &&
                        alert.getSeverity().equals(newAlert.getSeverity()) &&
                        alert.getTimestamp().isAfter(oneHourAgo)
        );
    }

    /**
     * Get all alerts (most recent first)
     */
    public List<WeatherAlert> getAllAlerts() {
        return new ArrayList<>(alerts);
    }

    /**
     * Get unread alerts
     */
    public List<WeatherAlert> getUnreadAlerts() {
        return alerts.stream()
                .filter(alert -> !alert.isRead())
                .collect(Collectors.toList());
    }

    /**
     * Mark alert as read
     */
    public void markAsRead(int alertId) {
        alerts.stream()
                .filter(alert -> alert.getIdAlert() == alertId)
                .findFirst()
                .ifPresent(alert -> alert.setRead(true));
    }

    /**
     * Mark all alerts as read
     */
    public void markAllAsRead() {
        alerts.forEach(alert -> alert.setRead(true));
    }

    /**
     * Clear all alerts
     */
    public void clearAllAlerts() {
        alerts.clear();
    }
}