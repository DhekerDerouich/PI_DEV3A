package util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties props = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Fichier config.properties introuvable dans resources !");
            } else {
                props.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}