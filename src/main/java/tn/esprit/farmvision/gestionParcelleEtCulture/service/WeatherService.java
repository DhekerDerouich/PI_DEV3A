package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import org.json.JSONObject;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {
    // Emoji constants
    private static final String TRASH = "🗑️";
    private static final String UPDATE = "🔄";
    private static final String SUCCESS = "✅";
    private static final String ERROR = "❌";
    private static final String WEATHER = "🌡️";
    private static final String WARNING = "⚠️";
    private static final String SAVE = "💾";
    private static final String EDIT = "✏️";

    private static final String API_KEY = "10f4b8f78ff1854d402feade0f123605";

    public static void fillWeather(Parcelle parcelle) {
        try {
            System.out.println(UPDATE + " Fetching weather data for " + parcelle.getLocalisation());

            String location = parcelle.getLocalisation().replace(" ", "%20");
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + location
                    + "&appid=" + API_KEY + "&units=metric";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());

                double temp = json.getJSONObject("main").getDouble("temp");
                String weather = json.getJSONArray("weather").getJSONObject(0).getString("main");

                parcelle.setTemperature(temp);
                parcelle.setWeather(weather);

                System.out.println(SUCCESS + " Weather data retrieved - " + getWeatherEmoji(weather) + " " + temp + "°C");
                System.out.println(SAVE + " Data saved to parcelle");

            } else {
                System.out.println(ERROR + " Failed to fetch weather data (Code: " + responseCode + ")");
                System.out.println(WARNING + " Using default values");

                parcelle.setTemperature(0);
                parcelle.setWeather("N/A");
            }

        } catch (Exception e) {
            System.out.println(ERROR + " Weather service exception: " + e.getMessage());
            e.printStackTrace();
            parcelle.setTemperature(0);
            parcelle.setWeather("N/A");
        }
    }

    private static String getWeatherEmoji(String weather) {
        switch(weather.toLowerCase()) {
            case "clear": return "☀️";
            case "clouds": return "☁️";
            case "rain": return "🌧️";
            case "thunderstorm": return "⛈️";
            case "snow": return "❄️";
            case "mist":
            case "fog": return "🌫️";
            default: return "🌡️";
        }
    }
}