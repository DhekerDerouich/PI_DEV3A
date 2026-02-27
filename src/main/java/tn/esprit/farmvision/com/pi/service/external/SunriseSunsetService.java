package tn.esprit.farmvision.com.pi.service.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour obtenir les heures de lever et coucher du soleil
 * Utilise l'API Sunrise-Sunset (gratuite, sans clé)
 */
public class SunriseSunsetService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, SunriseSunsetData> cache = new HashMap<>();

    // Coordonnées par défaut (Tunis)
    private static final double DEFAULT_LAT = 36.8065;
    private static final double DEFAULT_LNG = 10.1815;

    // Formatteurs de date
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Classe interne pour stocker les données de lever/coucher
     */
    public static class SunriseSunsetData {
        private final LocalTime sunrise;
        private final LocalTime sunset;
        private final LocalTime solarNoon;
        private final int dayLength; // en secondes
        private final LocalDate date;

        public SunriseSunsetData(LocalTime sunrise, LocalTime sunset,
                                 LocalTime solarNoon, int dayLength, LocalDate date) {
            this.sunrise = sunrise;
            this.sunset = sunset;
            this.solarNoon = solarNoon;
            this.dayLength = dayLength;
            this.date = date;
        }

        public LocalTime getSunrise() { return sunrise; }
        public LocalTime getSunset() { return sunset; }
        public LocalTime getSolarNoon() { return solarNoon; }
        public int getDayLengthSeconds() { return dayLength; }

        public String getDayLengthFormatted() {
            long hours = dayLength / 3600;
            long minutes = (dayLength % 3600) / 60;
            return String.format("%dh %02dm", hours, minutes);
        }

        public LocalDate getDate() { return date; }

        @Override
        public String toString() {
            return String.format("☀️ Lever: %s | Coucher: %s | Durée: %s",
                    sunrise.format(DateTimeFormatter.ofPattern("HH:mm")),
                    sunset.format(DateTimeFormatter.ofPattern("HH:mm")),
                    getDayLengthFormatted());
        }
    }

    /**
     * Récupère les données de lever/coucher pour une date et des coordonnées
     * @param lat Latitude
     * @param lng Longitude
     * @param date Date (peut être null pour aujourd'hui)
     * @return SunriseSunsetData ou valeurs par défaut si erreur
     */
    public SunriseSunsetData getSunriseSunset(double lat, double lng, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String cacheKey = lat + "," + lng + "," + date.toString();

        // Vérifier le cache
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        try {
            String url = String.format(
                    "https://api.sunrise-sunset.org/json?lat=%f&lng=%f&date=%s&formatted=0",
                    lat, lng, date.format(dateFormatter)
            );

            System.out.println("🌐 Appel API Sunrise: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "FarmVision-App")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                String body = response.body();
                System.out.println("✅ Réponse API reçue pour le " + date);

                // Extraire les données
                SunriseSunsetData data = parseResponse(body, date);
                if (data != null) {
                    cache.put(cacheKey, data);
                    return data;
                } else {
                    System.err.println("❌ Impossible de parser la réponse pour le " + date);
                }
            } else {
                System.err.println("❌ Erreur API - Code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur API Sunrise-Sunset pour le " + date + ": " + e.getMessage());
            // e.printStackTrace(); // Décommentez pour voir le stack trace complet
        }

        // Retourner des données par défaut si l'API échoue
        SunriseSunsetData defaultData = getDefaultData(date);
        cache.put(cacheKey, defaultData);
        return defaultData;
    }

    /**
     * Version simplifiée avec les coordonnées par défaut (Tunis)
     */
    public SunriseSunsetData getSunriseSunsetToday() {
        return getSunriseSunset(DEFAULT_LAT, DEFAULT_LNG, LocalDate.now());
    }

    /**
     * Version simplifiée pour une date spécifique
     */
    public SunriseSunsetData getSunriseSunsetForDate(LocalDate date) {
        return getSunriseSunset(DEFAULT_LAT, DEFAULT_LNG, date);
    }

    /**
     * Données par défaut quand l'API n'est pas disponible
     */
    private SunriseSunsetData getDefaultData(LocalDate date) {
        // Valeurs par défaut raisonnables pour la Tunisie
        LocalTime defaultSunrise;
        LocalTime defaultSunset;
        int defaultDayLength;

        // Ajuster selon la saison
        int month = date.getMonthValue();
        if (month >= 4 && month <= 9) { // Été: jours longs
            defaultSunrise = LocalTime.of(5, 30);
            defaultSunset = LocalTime.of(19, 0);
            defaultDayLength = 13 * 3600 + 30 * 60; // 13h30
        } else if (month >= 10 || month <= 3) { // Hiver: jours courts
            defaultSunrise = LocalTime.of(7, 0);
            defaultSunset = LocalTime.of(17, 30);
            defaultDayLength = 10 * 3600 + 30 * 60; // 10h30
        } else { // Printemps/Automne
            defaultSunrise = LocalTime.of(6, 15);
            defaultSunset = LocalTime.of(18, 15);
            defaultDayLength = 12 * 3600; // 12h
        }

        return new SunriseSunsetData(
                defaultSunrise,
                defaultSunset,
                LocalTime.of(12, 0), // Midi solaire approximatif
                defaultDayLength,
                date
        );
    }

    /**
     * Parse la réponse JSON
     */
    private SunriseSunsetData parseResponse(String body, LocalDate date) {
        try {
            if (!body.contains("\"results\":")) {
                return null;
            }

            // Extraire sunrise
            String sunriseStr = extractField(body, "sunrise");
            // Extraire sunset
            String sunsetStr = extractField(body, "sunset");
            // Extraire solar_noon
            String solarNoonStr = extractField(body, "solar_noon");
            // Extraire day_length
            String dayLengthStr = extractField(body, "day_length");

            if (sunriseStr != null && sunsetStr != null && dayLengthStr != null) {
                try {
                    // Les dates sont au format ISO 8601 avec timezone
                    // Exemple: 2025-02-20T06:45:23+00:00
                    LocalTime sunrise = LocalTime.parse(sunriseStr.substring(11, 19));
                    LocalTime sunset = LocalTime.parse(sunsetStr.substring(11, 19));
                    LocalTime solarNoon = solarNoonStr != null ?
                            LocalTime.parse(solarNoonStr.substring(11, 19)) : LocalTime.of(12, 0);
                    int dayLength = Integer.parseInt(dayLengthStr);

                    return new SunriseSunsetData(
                            sunrise, sunset, solarNoon, dayLength, date
                    );
                } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                    System.err.println("❌ Erreur parsing time: " + e.getMessage());
                    System.err.println("sunriseStr: " + sunriseStr);
                    System.err.println("sunsetStr: " + sunsetStr);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur générale parsing Sunrise: " + e.getMessage());
        }
        return null;
    }

    private String extractField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"";
        if (!json.contains(pattern)) {
            pattern = "\"" + fieldName + "\":";
            if (!json.contains(pattern)) {
                return null;
            }
        }

        try {
            String[] parts = json.split(pattern);
            if (parts.length > 1) {
                String value = parts[1].split("\"")[0];
                return value;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur extraction champ " + fieldName + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Vérifie s'il fait jour à une heure donnée
     */
    public boolean isDaylight(LocalDateTime dateTime, double lat, double lng) {
        SunriseSunsetData data = getSunriseSunset(lat, lng, dateTime.toLocalDate());
        if (data != null) {
            LocalTime time = dateTime.toLocalTime();
            return time.isAfter(data.getSunrise()) && time.isBefore(data.getSunset());
        }
        // Par défaut : considérer jour entre 6h et 18h
        LocalTime time = dateTime.toLocalTime();
        return time.isAfter(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(18, 0));
    }

    /**
     * Recommande la meilleure période pour les travaux extérieurs
     */
    public String getRecommendedWorkHours(LocalDate date) {
        SunriseSunsetData data = getSunriseSunsetForDate(date);
        if (data != null) {
            LocalTime start = data.getSunrise().plusHours(1);
            LocalTime end = data.getSunset().minusHours(1);
            return String.format("%s - %s",
                    start.format(DateTimeFormatter.ofPattern("HH:mm")),
                    end.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        return "08:00 - 17:00";
    }
}