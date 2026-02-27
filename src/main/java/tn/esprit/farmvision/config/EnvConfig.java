package tn.esprit.farmvision.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utilitaire pour charger les variables d'environnement depuis .env
 */
public class EnvConfig {

    private static final String ENV_FILE = ".env";
    private static Properties properties = null;

    /**
     * Charge le fichier .env
     */
    private static void loadEnv() {
        if (properties != null) return;

        properties = new Properties();

        // Chercher .env dans le répertoire de travail
        Path envPath = Paths.get(ENV_FILE);

        if (Files.exists(envPath)) {
            try (InputStream input = Files.newInputStream(envPath)) {
                properties.load(input);
                System.out.println("✅ Fichier .env chargé depuis: " + envPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("⚠️ Erreur chargement .env: " + e.getMessage());
            }
        } else {
            // Chercher dans le classpath (target/classes)
            try (InputStream input = EnvConfig.class.getClassLoader().getResourceAsStream(ENV_FILE)) {
                if (input != null) {
                    properties.load(input);
                    System.out.println("✅ Fichier .env chargé depuis classpath");
                } else {
                    System.err.println("⚠️ Fichier .env non trouvé!");
                }
            } catch (IOException e) {
                System.err.println("⚠️ Erreur chargement .env: " + e.getMessage());
            }
        }
    }

    /**
     * Récupère une variable d'environnement
     */
    public static String get(String key) {
        loadEnv();
        return properties != null ? properties.getProperty(key) : null;
    }

    /**
     * Récupère une variable avec valeur par défaut
     */
    public static String get(String key, String defaultValue) {
        loadEnv();
        String value = properties != null ? properties.getProperty(key) : null;
        return value != null ? value : defaultValue;
    }

    /**
     * Récupère une variable (lève une exception si non trouvée)
     */
    public static String getRequired(String key) {
        loadEnv();
        String value = properties != null ? properties.getProperty(key) : null;
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("❌ Variable d'environnement manquante: " + key);
        }
        return value;
    }
}