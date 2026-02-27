package tn.esprit.farmvision.Finance.service;



import tn.esprit.farmvision.config.EnvConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenRouterClient {

    private static final String API_KEY = EnvConfig.get("OPENROUTER_API_KEY");
    private static final String MODEL_NAME = EnvConfig.get("OPENROUTER_MODEL", "openai/gpt-oss-120b");
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    static {
        if (API_KEY == null) {
            System.err.println("⚠️ Clé API OpenRouter manquante dans .env");
        } else {
            System.out.println("✅ OpenRouter AI configuré");
        }
    }

    public static String askAI(String prompt) throws Exception {
        if (API_KEY == null) {
            return "❌ Configuration API OpenRouter manquante";
        }

        HttpClient client = HttpClient.newHttpClient();

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject payload = new JsonObject();
        payload.addProperty("model", MODEL_NAME);
        payload.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .header("HTTP-Referer", "http://localhost")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } else {
            return "Erreur API : " + response.statusCode() + "\n" + response.body();
        }
    }
}