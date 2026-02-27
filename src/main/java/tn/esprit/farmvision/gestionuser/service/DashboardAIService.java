package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.model.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service AI pour prédictions et recommandations intelligentes
 * Utilise des algorithmes de Machine Learning simplifiés
 */
public class DashboardAIService {

    private final UtilisateurService utilisateurService;

    public DashboardAIService() {
        this.utilisateurService = new UtilisateurService();
    }

    // ========================
    // PRÉDICTIONS DE CROISSANCE
    // ========================

    /**
     * Prédit le nombre d'utilisateurs dans N jours en utilisant la régression linéaire
     */
    public GrowthPrediction predictGrowth(int daysAhead) {
        List<Utilisateur> allUsers = utilisateurService.getAll();

        // Récupérer les données des 30 derniers jours
        Map<LocalDate, Integer> historicalData = getLast30DaysData(allUsers);

        // Calculer la tendance (régression linéaire simple)
        double growthRate = calculateGrowthRate(historicalData);

        // Prédire
        int currentTotal = allUsers.size();
        int predictedTotal = (int) (currentTotal + (growthRate * daysAhead));
        int predictedNew = (int) (growthRate * daysAhead);

        GrowthPrediction prediction = new GrowthPrediction();
        prediction.currentTotal = currentTotal;
        prediction.predictedTotal = Math.max(currentTotal, predictedTotal);
        prediction.predictedNewUsers = Math.max(0, predictedNew);
        prediction.growthRate = growthRate;
        prediction.daysAhead = daysAhead;
        prediction.confidence = calculateConfidence(historicalData);

        return prediction;
    }

    /**
     * Récupère les données des 30 derniers jours
     */
    private Map<LocalDate, Integer> getLast30DaysData(List<Utilisateur> users) {
        Map<LocalDate, Integer> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Initialiser avec 0
        for (int i = 29; i >= 0; i--) {
            data.put(today.minusDays(i), 0);
        }

        // Compter les inscriptions par jour
        for (Utilisateur user : users) {
            LocalDate userDate = user.getDateCreation()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (data.containsKey(userDate)) {
                data.put(userDate, data.get(userDate) + 1);
            }
        }

        return data;
    }

    /**
     * Calcule le taux de croissance moyen (régression linéaire simplifiée)
     */
    private double calculateGrowthRate(Map<LocalDate, Integer> data) {
        if (data.isEmpty()) return 0;

        List<Integer> values = new ArrayList<>(data.values());

        // Calculer la moyenne des 15 premiers jours vs 15 derniers jours
        double firstHalfAvg = values.subList(0, 15).stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        double secondHalfAvg = values.subList(15, 30).stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        // Taux de croissance par jour
        return (secondHalfAvg - firstHalfAvg) / 15;
    }

    /**
     * Calcule le niveau de confiance de la prédiction (0-100%)
     */
    private double calculateConfidence(Map<LocalDate, Integer> data) {
        if (data.isEmpty()) return 0;

        List<Integer> values = new ArrayList<>(data.values());

        // Calculer la variance (stabilité des données)
        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);

        // Plus la variance est faible, plus la confiance est élevée
        double maxVariance = 100; // Variance maximale acceptée
        double confidence = 100 - Math.min((variance / maxVariance) * 100, 100);

        return Math.max(50, confidence); // Minimum 50% de confiance
    }

    // ========================
    // DÉTECTION D'ANOMALIES
    // ========================

    /**
     * Détecte les anomalies dans les inscriptions
     */
    public List<Anomaly> detectAnomalies() {
        List<Anomaly> anomalies = new ArrayList<>();
        List<Utilisateur> allUsers = utilisateurService.getAll();

        // Anomalie 1: Trop d'inscriptions en attente
        long pendingCount = allUsers.stream().filter(u -> !u.isActivated()).count();
        double pendingRate = (pendingCount * 100.0) / allUsers.size();

        if (pendingRate > 30) {
            Anomaly anomaly = new Anomaly();
            anomaly.type = "HIGH_PENDING_RATE";
            anomaly.severity = "HIGH";
            anomaly.title = "⚠️ Taux élevé de comptes en attente";
            anomaly.description = String.format("%.1f%% des comptes sont en attente de validation", pendingRate);
            anomaly.recommendation = "Validez les comptes en attente pour améliorer l'expérience utilisateur";
            anomalies.add(anomaly);
        }

        // Anomalie 2: Inscriptions multiples depuis la même adresse (possible spam)
        Map<String, Long> addressCount = allUsers.stream()
                .filter(u -> u instanceof Agriculteur)
                .map(u -> ((Agriculteur) u).getAdresse())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()));

        for (Map.Entry<String, Long> entry : addressCount.entrySet()) {
            if (entry.getValue() > 5) {
                Anomaly anomaly = new Anomaly();
                anomaly.type = "SUSPICIOUS_REGISTRATIONS";
                anomaly.severity = "MEDIUM";
                anomaly.title = "🔍 Inscriptions suspectes détectées";
                anomaly.description = entry.getValue() + " comptes avec la même adresse : " + entry.getKey();
                anomaly.recommendation = "Vérifiez ces comptes pour détecter d'éventuels abus";
                anomalies.add(anomaly);
                break; // Une seule alerte pour cette anomalie
            }
        }

        // Anomalie 3: Baisse soudaine des inscriptions
        Map<LocalDate, Integer> last7Days = getLast7DaysData(allUsers);
        double avgLast7 = last7Days.values().stream().mapToInt(Integer::intValue).average().orElse(0);

        Map<LocalDate, Integer> previous7Days = getPrevious7DaysData(allUsers);
        double avgPrevious7 = previous7Days.values().stream().mapToInt(Integer::intValue).average().orElse(0);

        if (avgPrevious7 > 0 && avgLast7 < avgPrevious7 * 0.5) {
            Anomaly anomaly = new Anomaly();
            anomaly.type = "REGISTRATION_DROP";
            anomaly.severity = "MEDIUM";
            anomaly.title = "📉 Baisse des inscriptions";
            anomaly.description = "Les inscriptions ont chuté de " +
                    String.format("%.0f%%", ((avgPrevious7 - avgLast7) / avgPrevious7) * 100);
            anomaly.recommendation = "Lancez une campagne marketing pour stimuler les inscriptions";
            anomalies.add(anomaly);
        }

        return anomalies;
    }

    private Map<LocalDate, Integer> getLast7DaysData(List<Utilisateur> users) {
        Map<LocalDate, Integer> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            data.put(today.minusDays(i), 0);
        }

        for (Utilisateur user : users) {
            LocalDate userDate = user.getDateCreation()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (data.containsKey(userDate)) {
                data.put(userDate, data.get(userDate) + 1);
            }
        }

        return data;
    }

    private Map<LocalDate, Integer> getPrevious7DaysData(List<Utilisateur> users) {
        Map<LocalDate, Integer> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 13; i >= 7; i--) {
            data.put(today.minusDays(i), 0);
        }

        for (Utilisateur user : users) {
            LocalDate userDate = user.getDateCreation()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (data.containsKey(userDate)) {
                data.put(userDate, data.get(userDate) + 1);
            }
        }

        return data;
    }

    // ========================
    // RECOMMANDATIONS INTELLIGENTES
    // ========================

    /**
     * Génère des recommandations basées sur l'analyse des données
     */
    public List<Recommendation> generateRecommendations() {
        List<Recommendation> recommendations = new ArrayList<>();
        List<Utilisateur> allUsers = utilisateurService.getAll();

        // Recommandation 1: Validation des comptes
        long pendingCount = allUsers.stream().filter(u -> !u.isActivated()).count();
        if (pendingCount > 0) {
            Recommendation rec = new Recommendation();
            rec.priority = pendingCount > 10 ? "HIGH" : "MEDIUM";
            rec.category = "ACTIVATION";
            rec.title = "Validez les comptes en attente";
            rec.description = pendingCount + " compte(s) attendent votre validation";
            rec.actionLabel = "Valider maintenant";
            rec.expectedImpact = "Améliore l'expérience utilisateur et l'adoption";
            recommendations.add(rec);
        }

        // Recommandation 2: Équilibre des types d'utilisateurs
        long agriculteurs = allUsers.stream().filter(u -> u instanceof Agriculteur).count();
        long responsables = allUsers.stream().filter(u -> u instanceof ResponsableExploitation).count();

        if (agriculteurs > 0 && responsables < agriculteurs * 0.2) {
            Recommendation rec = new Recommendation();
            rec.priority = "LOW";
            rec.category = "GROWTH";
            rec.title = "Recruter plus de Responsables d'Exploitation";
            rec.description = "Ratio actuel : " + agriculteurs + " agriculteurs vs " + responsables + " responsables";
            rec.actionLabel = "Campagne ciblée";
            rec.expectedImpact = "Diversifie votre base utilisateurs";
            recommendations.add(rec);
        }

        // Recommandation 3: Engagement
        GrowthPrediction prediction = predictGrowth(30);
        if (prediction.growthRate < 0.5) {
            Recommendation rec = new Recommendation();
            rec.priority = "MEDIUM";
            rec.category = "MARKETING";
            rec.title = "Stimulez les inscriptions";
            rec.description = "La croissance ralentit : " + String.format("%.1f", prediction.growthRate) + " utilisateurs/jour";
            rec.actionLabel = "Lancer campagne";
            rec.expectedImpact = "Accélère la croissance de +50%";
            recommendations.add(rec);
        }

        return recommendations;
    }

    // ========================
    // INSIGHTS AUTOMATIQUES
    // ========================

    /**
     * Génère des insights intelligents sur les données
     */
    public List<Insight> generateInsights() {
        List<Insight> insights = new ArrayList<>();
        List<Utilisateur> allUsers = utilisateurService.getAll();

        // Insight 1: Jour de la semaine le plus populaire
        Map<String, Long> dayOfWeekStats = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getDateCreation().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .getDayOfWeek()
                                .toString(),
                        Collectors.counting()
                ));

        String mostPopularDay = dayOfWeekStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        if (!mostPopularDay.equals("N/A")) {
            Insight insight = new Insight();
            insight.title = "📅 Jour le plus populaire";
            insight.value = translateDayOfWeek(mostPopularDay);
            insight.description = "La majorité des inscriptions ont lieu ce jour";
            insight.icon = "📊";
            insights.add(insight);
        }

        // Insight 2: Temps moyen avant activation
        double avgActivationDays = allUsers.stream()
                .filter(Utilisateur::isActivated)
                .mapToLong(u -> ChronoUnit.DAYS.between(
                        u.getDateCreation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LocalDate.now()
                ))
                .filter(days -> days < 30) // Exclure les outliers
                .average()
                .orElse(0);

        if (avgActivationDays > 0) {
            Insight insight = new Insight();
            insight.title = "⏱️ Temps d'activation moyen";
            insight.value = avgActivationDays + " jours";
            insight.description = "Délai moyen entre inscription et validation";
            insight.icon = "⚡";
            insights.add(insight);
        }

        // Insight 3: Taux de conversion
        double conversionRate = (allUsers.stream().filter(Utilisateur::isActivated).count() * 100.0) / allUsers.size();
        Insight insight = new Insight();
        insight.title = "✅ Taux de conversion";
        insight.value = String.format("%.1f%%", conversionRate);
        insight.description = "Pourcentage d'utilisateurs validés";
        insight.icon = "📈";
        insights.add(insight);

        return insights;
    }

    private String translateDayOfWeek(String day) {
        switch (day) {
            case "MONDAY": return "Lundi";
            case "TUESDAY": return "Mardi";
            case "WEDNESDAY": return "Mercredi";
            case "THURSDAY": return "Jeudi";
            case "FRIDAY": return "Vendredi";
            case "SATURDAY": return "Samedi";
            case "SUNDAY": return "Dimanche";
            default: return day;
        }
    }

    // ========================
    // CLASSES DE DONNÉES
    // ========================

    public static class GrowthPrediction {
        public int currentTotal;
        public int predictedTotal;
        public int predictedNewUsers;
        public double growthRate;
        public int daysAhead;
        public double confidence;
    }

    public static class Anomaly {
        public String type;
        public String severity; // HIGH, MEDIUM, LOW
        public String title;
        public String description;
        public String recommendation;
    }

    public static class Recommendation {
        public String priority; // HIGH, MEDIUM, LOW
        public String category;
        public String title;
        public String description;
        public String actionLabel;
        public String expectedImpact;
    }

    public static class Insight {
        public String title;
        public String value;
        public String description;
        public String icon;
    }
}