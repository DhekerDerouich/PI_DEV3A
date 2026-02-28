package tn.esprit.farmvision.Finance.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PDFEndpointClient {
    private static final String API_KEY = "pdfe_live_6503c2024d4260436691646c4bfab0bfef1c";
    private static final String API_URL = "https://api.pdfendpoint.com/v1/convert";

    public static boolean convertHtmlToPdf(String html, String outputPath) {
        HttpClient client = HttpClient.newHttpClient();
        try {
            // 1. Première requête : demander la conversion
            JsonObject payload = new JsonObject();
            payload.addProperty("html", html);

            HttpRequest conversionRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Accept", "application/json") // On accepte du JSON pour récupérer l'URL
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<byte[]> conversionResponse = client.send(conversionRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (conversionResponse.statusCode() != 200) {
                System.err.println("Erreur conversion : " + conversionResponse.statusCode());
                return false;
            }

            // 2. Analyser la réponse JSON pour extraire l'URL
            String jsonResponse = new String(conversionResponse.body());
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (!json.get("success").getAsBoolean()) {
                System.err.println("Échec de la conversion : " + jsonResponse);
                return false;
            }

            String pdfUrl = json.getAsJsonObject("data").get("url").getAsString();
            System.out.println("URL du PDF obtenue : " + pdfUrl);

            // 3. Deuxième requête : télécharger le PDF depuis l'URL
            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(pdfUrl))
                    .GET()
                    .build();

            HttpResponse<byte[]> downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (downloadResponse.statusCode() != 200) {
                System.err.println("Erreur téléchargement PDF : " + downloadResponse.statusCode());
                return false;
            }

            // 4. Sauvegarder le fichier
            Files.write(Paths.get(outputPath), downloadResponse.body());
            System.out.println("PDF sauvegardé : " + outputPath);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}