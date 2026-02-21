package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AIChatService {

    // Gemini API endpoint
    //updating path
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private static final String API_KEY = "AIzaSyDczEXmunBGFSIYfLSm8znUptcgQl46cMc";
    private final HttpClient httpClient;
    private final Gson gson;
    private boolean useFallbackMode = false;

    public AIChatService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public String getAgriculturalAdvice(String userMessage, List<Parcelle> parcelles) {
        // If we're in fallback mode, use local responses
        if (useFallbackMode) {
            return getFallbackResponse(userMessage, parcelles);
        }

        try {
            String context = buildParcelContext(parcelles);

            String prompt = String.format(
                    "Tu es un expert agricole spécialisé dans le conseil aux agriculteurs tunisiens. " +
                            "Utilise les données météorologiques et des parcelles suivantes pour donner des conseils pertinents:\n\n" +
                            "%s\n\n" +
                            "Question de l'agriculteur: %s\n\n" +
                            "Réponse (en français, pratique et adaptée au climat tunisien):",
                    context, userMessage
            );

            // Build Gemini API request body
            JsonObject requestBody = new JsonObject();

            // Create contents array with the prompt
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Add generation config for better responses
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("maxOutputTokens", 2048);
            generationConfig.addProperty("topP", 0.95);
            generationConfig.addProperty("topK", 40);
            requestBody.add("generationConfig", generationConfig);

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "?key=" + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            } else {
                // Check for quota exceeded or other errors
                if (response.statusCode() == 429 || response.statusCode() == 403) {
                    useFallbackMode = true;
                    return "⚠️ **Mode hors ligne activé (limite API atteinte)**\n\n" +
                            getFallbackResponse(userMessage, parcelles);
                } else {
                    System.err.println("API Error " + response.statusCode() + ": " + response.body());
                    return "❌ Erreur API: " + response.statusCode() + "\n\n" +
                            getFallbackResponse(userMessage, parcelles);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            useFallbackMode = true;
            return "⚠️ **Mode hors ligne activé (erreur de connexion)**\n\n" +
                    getFallbackResponse(userMessage, parcelles);
        }
    }

    private String parseGeminiResponse(String jsonResponse) {
        try {
            JsonObject response = gson.fromJson(jsonResponse, JsonObject.class);

            // Navigate to the text content in Gemini's response structure
            return response.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text")
                    .getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Désolé, je n'ai pas pu comprendre la réponse de l'API.";
        }
    }

    private String buildParcelContext(List<Parcelle> parcelles) {
        if (parcelles == null || parcelles.isEmpty()) {
            return "Aucune parcelle disponible pour le moment.";
        }

        StringBuilder context = new StringBuilder("📊 **DONNÉES DE VOS PARCELLES:**\n\n");

        for (int i = 0; i < parcelles.size(); i++) {
            Parcelle p = parcelles.get(i);
            context.append(String.format(
                    "🌾 **Parcelle %d:**\n" +
                            "   • Localisation: %s\n" +
                            "   • Surface: %.2f m²\n" +
                            "   • Température: %.1f°C\n" +
                            "   • Conditions: %s\n\n",
                    i + 1, p.getLocalisation(), p.getSurface(), p.getTemperature(), p.getWeather()
            ));
        }

        // Add weather analysis
        context.append("📈 **ANALYSE CLIMATIQUE:**\n");
        context.append(analyzeWeatherPatterns(parcelles));

        return context.toString();
    }

    private String analyzeWeatherPatterns(List<Parcelle> parcelles) {
        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(0);

        boolean hasRain = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        p.getWeather().toLowerCase().contains("rain"));

        boolean hasSun = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        (p.getWeather().toLowerCase().contains("clear") ||
                                p.getWeather().toLowerCase().contains("sun")));

        boolean hasClouds = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        p.getWeather().toLowerCase().contains("cloud"));

        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("🌡️ Température moyenne: **%.1f°C**\n", avgTemp));

        if (avgTemp > 30) {
            analysis.append("🔥 **Attention:** Fortes chaleurs - irrigation nécessaire\n");
        } else if (avgTemp > 25) {
            analysis.append("☀️ **Température chaude** - Bonne pour les cultures d'été\n");
        } else if (avgTemp > 15) {
            analysis.append("🌤️ **Température douce** - Idéale pour la plupart des cultures\n");
        } else if (avgTemp > 5) {
            analysis.append("🍂 **Température fraîche** - Cultures d'automne/hiver\n");
        } else {
            analysis.append("❄️ **Attention:** Risque de gel - protégez les cultures\n");
        }

        if (hasRain) {
            analysis.append("🌧️ **Pluie détectée** - Vérifiez le drainage, réduisez l'irrigation\n");
        }

        if (hasSun) {
            analysis.append("☀️ **Bon ensoleillement** - Favorable à la photosynthèse\n");
        }

        if (hasClouds && !hasRain) {
            analysis.append("☁️ **Temps nuageux** - Bon pour les semis et transplantations\n");
        }

        return analysis.toString();
    }



    private String getFallbackResponse(String userMessage, List<Parcelle> parcelles) {
        // Analyze weather data
        String weatherAnalysis = analyzeWeatherPatterns(parcelles);

        // Simple keyword-based responses
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("maladie") || lowerMessage.contains("maladies")) {
            return getDiseaseAdvice(parcelles);
        } else if (lowerMessage.contains("irrig") || lowerMessage.contains("eau") || lowerMessage.contains("arros")) {
            return getIrrigationAdvice(parcelles);
        } else if (lowerMessage.contains("engrais") || lowerMessage.contains("fertil")) {
            return getFertilizerAdvice(parcelles);
        } else if (lowerMessage.contains("gel") || lowerMessage.contains("froid")) {
            return getFrostAdvice(parcelles);
        } else if (lowerMessage.contains("culture") || lowerMessage.contains("planter")) {
            return getCropAdvice(parcelles);
        } else if (lowerMessage.contains("réc") || lowerMessage.contains("recolte")) {
            return getHarvestAdvice(parcelles);
        } else {
            return "🌾 **Conseil agricole personnalisé**\n\n" +
                    "Basé sur vos données actuelles:\n" +
                    weatherAnalysis + "\n\n" +
                    "Pour des conseils plus spécifiques, posez-moi des questions sur:\n" +
                    "• L'irrigation 💧\n" +
                    "• Les maladies des plantes 🌿\n" +
                    "• Les engrais et fertilisants 🧪\n" +
                    "• La protection contre le gel ❄️\n" +
                    "• Les cultures recommandées 🌽\n" +
                    "• La période de récolte 📅";
        }
    }

    private String getDiseaseAdvice(List<Parcelle> parcelles) {
        if (parcelles == null || parcelles.isEmpty()) {
            return "Aucune donnée de parcelle disponible pour analyser les risques de maladies.";
        }

        boolean hasRain = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        p.getWeather().toLowerCase().contains("rain"));

        boolean hasHighHumidity = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        (p.getWeather().toLowerCase().contains("mist") ||
                                p.getWeather().toLowerCase().contains("fog")));

        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(15);

        StringBuilder advice = new StringBuilder();
        advice.append("🌧️ **Analyse des risques de maladies:**\n\n");

        if (hasRain || hasHighHumidity) {
            advice.append("⚠️ **Risque modéré à élevé**\n");
            advice.append("Les conditions humides favorisent:\n");
            advice.append("• Mildiou - Traitez avec du cuivre\n");
            advice.append("• Oïdium - Aérez les cultures\n");
            advice.append("• Rouille - Évitez l'excès d'azote\n\n");
            advice.append("✅ **Recommandations:**\n");
            advice.append("• Inspectez vos cultures quotidiennement\n");
            advice.append("• Traitez préventivement avec des fongicides biologiques\n");
            advice.append("• Évitez d'arroser le feuillage\n");
        } else {
            advice.append("✅ **Risque faible**\n");
            advice.append("Les conditions actuelles sont favorables.\n");
            advice.append("Maintenez une surveillance normale.\n");
        }

        if (avgTemp > 25) {
            advice.append("\n🌡️ **Température élevée:** Surveillez l'oïdium et les acariens.");
        } else if (avgTemp < 10) {
            advice.append("\n❄️ **Température basse:** Risque de gelée blanche et de maladies fongiques.");
        }

        return advice.toString();
    }

    private String getIrrigationAdvice(List<Parcelle> parcelles) {
        if (parcelles == null || parcelles.isEmpty()) {
            return "Aucune donnée météo disponible pour conseiller sur l'irrigation.";
        }

        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(15);

        boolean hasRain = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        p.getWeather().toLowerCase().contains("rain"));

        boolean hasSun = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        (p.getWeather().toLowerCase().contains("clear") ||
                                p.getWeather().toLowerCase().contains("sun")));

        StringBuilder advice = new StringBuilder();
        advice.append("💧 **Conseils d'irrigation:**\n\n");

        if (hasRain) {
            advice.append("🌧️ **Pluie détectée** - Réduisez ou stoppez l'irrigation aujourd'hui\n");
            advice.append("Vérifiez le drainage pour éviter l'excès d'eau\n");
        } else if (avgTemp > 28 && hasSun) {
            advice.append("🔥 **Fortes chaleurs** - Augmentez l'irrigation\n");
            advice.append("Arrosez tôt le matin ou le soir pour limiter l'évaporation\n");
            advice.append("Quantité recommandée: 15-20L/m²\n");
        } else if (avgTemp > 20) {
            advice.append("🌤️ **Conditions normales** - Irrigation modérée\n");
            advice.append("Quantité recommandée: 8-12L/m² tous les 2-3 jours\n");
        } else {
            advice.append("❄️ **Températures fraîches** - Réduisez l'irrigation\n");
            advice.append("Risque de pourriture des racines, espacez les arrosages\n");
        }

        return advice.toString();
    }

    private String getFertilizerAdvice(List<Parcelle> parcelles) {
        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(15);

        StringBuilder advice = new StringBuilder();
        advice.append("🧪 **Recommandations d'engrais:**\n\n");

        if (avgTemp > 20) {
            advice.append("🌱 **Saison de croissance active**\n");
            advice.append("• Utilisez un engrais équilibré NPK (10-10-10)\n");
            advice.append("• Appliquez toutes les 2-3 semaines\n");
            advice.append("• Privilégiez les engrais organiques (compost, fumier)\n");
        } else {
            advice.append("❄️ **Croissance ralentie**\n");
            advice.append("• Réduisez la fertilisation azotée\n");
            advice.append("• Maintenez un apport en potassium pour la résistance au froid\n");
            advice.append("• Utilisez des engrais à libération lente\n");
        }

        return advice.toString();
    }

    private String getFrostAdvice(List<Parcelle> parcelles) {
        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(15);

        if (avgTemp > 10) {
            return "❄️ **Pas de risque de gel actuellement.**\n" +
                    "La température moyenne est de " + String.format("%.1f", avgTemp) + "°C, ce qui est sûr pour vos cultures.";
        }

        return "❄️ **Risque de gel détecté!**\n\n" +
                "✅ **Actions recommandées:**\n" +
                "• Couvrez les cultures sensibles avec des voiles d'hivernage\n" +
                "• Arrosez légèrement avant le gel (l'eau libère de la chaleur)\n" +
                "• Paillez le sol pour protéger les racines\n" +
                "• Récoltez les fruits et légumes matures\n" +
                "• Pour les cultures en pot, rentrez-les à l'abri";
    }

    private String getCropAdvice(List<Parcelle> parcelles) {
        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(15);

        boolean hasRain = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        p.getWeather().toLowerCase().contains("rain"));

        StringBuilder advice = new StringBuilder();
        advice.append("🌽 **Cultures recommandées pour la saison:**\n\n");

        if (avgTemp > 25) {
            advice.append("☀️ **Cultures d'été:**\n");
            advice.append("• Tomates 🍅\n");
            advice.append("• Poivrons 🌶️\n");
            advice.append("• Aubergines 🍆\n");
            advice.append("• Courgettes 🥒\n");
            advice.append("• Melons 🍈\n");
        } else if (avgTemp > 15) {
            advice.append("🌸 **Cultures de printemps/automne:**\n");
            advice.append("• Laitues 🥬\n");
            advice.append("• Épinards 🌿\n");
            advice.append("• Carottes 🥕\n");
            advice.append("• Pommes de terre 🥔\n");
            advice.append("• Haricots verts\n");
        } else {
            advice.append("❄️ **Cultures d'hiver:**\n");
            advice.append("• Choux 🥬\n");
            advice.append("• Poireaux\n");
            advice.append("• Oignons 🧅\n");
            advice.append("• Ail 🧄\n");
            advice.append("• Fèves\n");
        }

        if (hasRain) {
            advice.append("\n🌧️ **Avec la pluie actuelle:**\n");
            advice.append("Profitez-en pour préparer le sol et planter si les températures le permettent.\n");
        }

        return advice.toString();
    }

    private String getHarvestAdvice(List<Parcelle> parcelles) {
        double avgTemp = parcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(15);

        boolean hasRain = parcelles.stream()
                .anyMatch(p -> p.getWeather() != null &&
                        p.getWeather().toLowerCase().contains("rain"));

        StringBuilder advice = new StringBuilder();
        advice.append("📅 **Conseils de récolte:**\n\n");

        if (hasRain) {
            advice.append("🌧️ **Évitez de récolter sous la pluie**\n");
            advice.append("Les fruits humides se conservent moins bien.\n");
            advice.append("Attendez une période sèche si possible.\n\n");
        }

        if (avgTemp > 25) {
            advice.append("☀️ **Récoltez tôt le matin**\n");
            advice.append("Les légumes-feuilles montent rapidement en graines par forte chaleur.\n");
            advice.append("Vérifiez quotidiennement les cultures sensibles.\n");
        } else if (avgTemp < 10) {
            advice.append("❄️ **Protégez les récoltes du gel**\n");
            advice.append("Récoltez avant les premières gelées les cultures sensibles.\n");
        }

        return advice.toString();
    }
}