package Finance.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyService {
    private static final String API_KEY = "7b7fc1466e2cb0fedcb8f521"; // Remplace par ta vraie clé
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/TND";

    private double eurRate = -1; // 1 TND = ? EUR
    private double usdRate = -1; // 1 TND = ? USD
    private long lastFetch = 0;
    private static final long CACHE_TIME = 3600 * 1000; // 1 heure

    private void fetchRates() throws Exception {
        long now = System.currentTimeMillis();
        if (now - lastFetch < CACHE_TIME && eurRate > 0) return;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) throw new Exception("Erreur API, code : " + response.statusCode());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject rates = json.getAsJsonObject("conversion_rates");
        eurRate = rates.get("EUR").getAsDouble();
        usdRate = rates.get("USD").getAsDouble();
        lastFetch = now;
    }

    /**
     * Retourne le taux de conversion de TND vers EUR (1 TND = ? EUR)
     */
    public double getTndToEurRate() throws Exception {
        fetchRates();
        return eurRate;
    }

    /**
     * Retourne le taux de conversion de TND vers USD (1 TND = ? USD)
     */
    public double getTndToUsdRate() throws Exception {
        fetchRates();
        return usdRate;
    }
}