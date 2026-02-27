package tn.esprit.farmvision.Finance.service;



import tn.esprit.farmvision.config.EnvConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyService {

    private static final String API_KEY = EnvConfig.get("CURRENCY_API_KEY");
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/TND";

    private double eurRate = -1;
    private double usdRate = -1;
    private long lastFetch = 0;
    private static final long CACHE_TIME = 3600 * 1000;

    static {
        if (API_KEY == null) {
            System.err.println("⚠️ Clé API Currency manquante dans .env");
        } else {
            System.out.println("✅ Currency API configurée");
        }
    }

    private void fetchRates() throws Exception {
        if (API_KEY == null) {
            throw new Exception("Clé API Currency non configurée");
        }

        long now = System.currentTimeMillis();
        if (now - lastFetch < CACHE_TIME && eurRate > 0) return;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur API, code : " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject rates = json.getAsJsonObject("conversion_rates");
        eurRate = rates.get("EUR").getAsDouble();
        usdRate = rates.get("USD").getAsDouble();
        lastFetch = now;
    }

    public double getTndToEurRate() throws Exception {
        fetchRates();
        return eurRate;
    }

    public double getTndToUsdRate() throws Exception {
        fetchRates();
        return usdRate;
    }
}