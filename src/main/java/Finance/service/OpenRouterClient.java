package Finance.service;



import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenRouterClient {
    // Remplacez par votre clé API OpenRouter
    private static final String API_KEY = "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";
    // URL de base de l'API OpenRouter
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    // Nom du modèle Arcee AI (à ajuster selon la documentation OpenRouter)
    private static final String MODEL_NAME = "openai/gpt-oss-120b";

    public static String askAI(String prompt) throws Exception {
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
                .header("HTTP-Referer", "http://localhost") // requis par OpenRouter
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