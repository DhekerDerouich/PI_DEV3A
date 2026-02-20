package com.pi.service.external;

import com.pi.model.Equipement;
import com.pi.model.Maintenance;
import com.pi.service.AlertesService;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour calculer l'empreinte carbone des équipements agricoles
 * Utilise l'API CO2Signal (gratuite, sans clé - 1000 requêtes/jour)
 */
public class CO2SignalService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, Double> cacheEmissions = new HashMap<>();

    // Facteurs d'émission par défaut (kg CO2/kWh) si l'API est indisponible
    private static final Map<String, Double> DEFAULT_EMISSION_FACTORS = Map.of(
            "FR", 0.055,  // France (nucléaire) - très bas
            "DE", 0.401,  // Allemagne
            "TN", 0.500,  // Tunisie (estimation)
            "US", 0.450,  // États-Unis
            "CN", 0.600,  // Chine
            "DEFAULT", 0.400 // Valeur par défaut
    );

    // Consommation moyenne des équipements agricoles (kWh/heure)
    private static final Map<String, Double> CONSOMMATION_EQUIPEMENTS = Map.of(
            "Tracteur", 40.0,
            "Moissonneuse", 60.0,
            "Pulvérisateur", 15.0,
            "Charrue", 25.0,
            "Semoir", 10.0,
            "DEFAULT", 20.0
    );

    /**
     * Récupère l'intensité carbone du réseau électrique pour un pays
     * @param countryCode Code pays (FR, TN, DE, etc.)
     * @return Intensité carbone en kg CO2/kWh
     */
    public double getCarbonIntensity(String countryCode) {
        // Vérifier le cache
        if (cacheEmissions.containsKey(countryCode)) {
            return cacheEmissions.get(countryCode);
        }

        try {
            String url = "https://api.co2signal.com/v1/latest?countryCode=" + countryCode;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                String body = response.body();

                // Extraction simple du JSON (sans bibliothèque JSON)
                if (body.contains("\"carbonIntensity\":")) {
                    String[] parts = body.split("\"carbonIntensity\":");
                    if (parts.length > 1) {
                        String value = parts[1].split(",")[0].trim();
                        double intensity = Double.parseDouble(value) / 1000.0; // Convertir en kg CO2/kWh
                        cacheEmissions.put(countryCode, intensity);
                        System.out.println("✅ CO2Signal: Intensité pour " + countryCode + " = " + intensity + " kg CO2/kWh");
                        return intensity;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur API CO2Signal: " + e.getMessage());
        }

        // Valeur par défaut si l'API échoue
        double defaultValue = DEFAULT_EMISSION_FACTORS.getOrDefault(
                countryCode,
                DEFAULT_EMISSION_FACTORS.get("DEFAULT")
        );
        cacheEmissions.put(countryCode, defaultValue);
        return defaultValue;
    }

    /**
     * Calcule l'empreinte carbone d'un équipement pour une durée d'utilisation
     * @param equipement L'équipement
     * @param heuresUtilisation Nombre d'heures d'utilisation
     * @param countryCode Code pays (FR, TN, etc.)
     * @return Émission CO2 en kg
     */
    public double calculerEmissionsEquipement(Equipement equipement, double heuresUtilisation, String countryCode) {
        double consommation = CONSOMMATION_EQUIPEMENTS.getOrDefault(
                equipement.getType(),
                CONSOMMATION_EQUIPEMENTS.get("DEFAULT")
        );

        double intensiteCarbone = getCarbonIntensity(countryCode);
        double emissions = consommation * heuresUtilisation * intensiteCarbone;

        return Math.round(emissions * 100.0) / 100.0; // Arrondi à 2 décimales
    }

    /**
     * Calcule l'empreinte carbone totale des maintenances du mois
     * @param maintenances Liste des maintenances
     * @param equipements Liste des équipements
     * @param countryCode Code pays
     * @return Émissions totales du mois
     */
    public double calculerEmissionsMensuelles(List<Maintenance> maintenances,
                                              List<Equipement> equipements,
                                              String countryCode) {
        double total = 0.0;
        LocalDate maintenant = LocalDate.now();

        // SOLUTION 1: Utiliser une boucle for-each classique au lieu de stream
        for (Maintenance m : maintenances) {
            // Ne compter que les maintenances du mois en cours
            if (m.getDateMaintenance().getMonth() == maintenant.getMonth() &&
                    m.getDateMaintenance().getYear() == maintenant.getYear()) {

                // Trouver l'équipement correspondant
                for (Equipement e : equipements) {
                    if (e.getId() == m.getEquipementId()) {
                        // Estimation: 2 heures d'utilisation pour la maintenance
                        total += calculerEmissionsEquipement(e, 2.0, countryCode);
                        break;
                    }
                }
            }
        }

        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Version alternative avec Stream (si vous préférez)
     */
    public double calculerEmissionsMensuellesStream(List<Maintenance> maintenances,
                                                    List<Equipement> equipements,
                                                    String countryCode) {
        LocalDate maintenant = LocalDate.now();

        // SOLUTION 2: Créer une Map d'équipements pour un accès plus rapide
        Map<Integer, Equipement> equipementMap = new HashMap<>();
        for (Equipement e : equipements) {
            equipementMap.put(e.getId(), e);
        }

        double total = maintenances.stream()
                .filter(m -> m.getDateMaintenance().getMonth() == maintenant.getMonth() &&
                        m.getDateMaintenance().getYear() == maintenant.getYear())
                .mapToDouble(m -> {
                    Equipement e = equipementMap.get(m.getEquipementId());
                    if (e != null) {
                        return calculerEmissionsEquipement(e, 2.0, countryCode);
                    }
                    return 0.0;
                })
                .sum();

        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Génère une alerte écologique si l'empreinte carbone est trop élevée
     * @param alertesService Service d'alertes
     * @param emissionsTotal Emissions totales
     */
    public void verifierEtAlerter(AlertesService alertesService, double emissionsTotal) {
        Platform.runLater(() -> {
            if (emissionsTotal > 100.0) {
                // Créer une alerte (à adapter selon votre système d'alertes)
                String type = emissionsTotal > 200 ? "WARNING" : "INFO";
                System.out.println("⚠️ ALERTE ENVIRONNEMENT - Empreinte carbone: " + emissionsTotal + " kg CO2");

                // Si vous voulez utiliser votre système d'alertes existant:
                // AlertesService.Alerte alerte = new AlertesService.Alerte(
                //     "🌱 Empreinte carbone élevée",
                //     String.format("Ce mois: %.2f kg CO2. Pensez à optimiser l'utilisation des équipements!", emissionsTotal),
                //     type,
                //     "ENVIRONNEMENT"
                // );
                // alertesService.ajouterAlerte(alerte);
            }
        });
    }

    /**
     * Obtient une estimation de la consommation selon le type d'équipement
     */
    public double getConsommationEstimee(String typeEquipement) {
        return CONSOMMATION_EQUIPEMENTS.getOrDefault(typeEquipement, CONSOMMATION_EQUIPEMENTS.get("DEFAULT"));
    }

    /**
     * Change le code pays (utile si vous voulez le rendre configurable)
     */
    public String getDefaultCountryCode() {
        return "TN"; // Tunisie par défaut
    }
}