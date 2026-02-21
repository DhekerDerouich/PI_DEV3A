package tn.esprit.farmvision.gestionParcelleEtCulture.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherAlert {
    private static int idCounter = 0;

    private int idAlert;
    private int idParcelle;
    private String parcelleLocalisation;
    private String alertType;
    private String severity;
    private String message;
    private double temperature;
    private String weatherCondition;
    private LocalDateTime timestamp;
    private boolean isRead;

    public WeatherAlert(int idParcelle, String parcelleLocalisation, String alertType,
                        String severity, String message, double temperature, String weatherCondition) {
        this.idAlert = ++idCounter;
        this.idParcelle = idParcelle;
        this.parcelleLocalisation = parcelleLocalisation;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters
    public int getIdAlert() { return idAlert; }
    public int getIdParcelle() { return idParcelle; }
    public String getParcelleLocalisation() { return parcelleLocalisation; }
    public String getAlertType() { return alertType; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public double getTemperature() { return temperature; }
    public String getWeatherCondition() { return weatherCondition; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return timestamp.format(formatter);
    }

    public String getRelativeTime() {
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(timestamp, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) return "à l'instant";
        if (seconds < 3600) return (seconds / 60) + " min";
        if (seconds < 86400) return (seconds / 3600) + "h";
        return (seconds / 86400) + "j";
    }
}