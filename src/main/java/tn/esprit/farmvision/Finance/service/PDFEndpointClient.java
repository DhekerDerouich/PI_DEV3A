package tn.esprit.farmvision.Finance.service;



import tn.esprit.farmvision.config.EnvConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PDFEndpointClient {

    private static final String API_KEY = EnvConfig.get("PDF_API_KEY");
    private static final String API_URL = "https://api.pdfendpoint.com/v1/convert";

    static {
        if (API_KEY == null) {
            System.err.println("⚠️ Clé API PDF manquante dans .env");
        } else {
            System.out.println("✅ PDF Endpoint configuré");
        }
    }

    public static boolean convertHtmlToPdf(String html, String outputPath) {
        if (API_KEY == null) {
            System.err.println("❌ Impossible de convertir: clé API PDF manquante");
            return false;
        }

        HttpClient client = HttpClient.newHttpClient();
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("html", html);

            HttpRequest conversionRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<byte[]> conversionResponse = client.send(conversionRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (conversionResponse.statusCode() != 200) {
                System.err.println("Erreur conversion : " + conversionResponse.statusCode());
                return false;
            }

            String jsonResponse = new String(conversionResponse.body());
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (!json.get("success").getAsBoolean()) {
                System.err.println("Échec de la conversion : " + jsonResponse);
                return false;
            }

            String pdfUrl = json.getAsJsonObject("data").get("url").getAsString();
            System.out.println("URL du PDF obtenue : " + pdfUrl);

            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(pdfUrl))
                    .GET()
                    .build();

            HttpResponse<byte[]> downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (downloadResponse.statusCode() != 200) {
                System.err.println("Erreur téléchargement PDF : " + downloadResponse.statusCode());
                return false;
            }

            Files.write(Paths.get(outputPath), downloadResponse.body());
            System.out.println("PDF sauvegardé : " + outputPath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}