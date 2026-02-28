package tn.esprit.farmvision.gestionstock.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/farmvision_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            System.out.println("🔄 [DatabaseConnection] Tentative de connexion à MySQL...");
            System.out.println("   URL: " + URL);
            System.out.println("   User: " + USER);

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ [DatabaseConnection] Driver chargé");

            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ [DatabaseConnection] Connexion réussie !\n");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ [DatabaseConnection] Driver MySQL non trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ [DatabaseConnection] Erreur de connexion !");
            System.err.println("   Code: " + e.getErrorCode());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            System.out.println("🔄 [DatabaseConnection] Création de l'instance...");
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("🔄 [DatabaseConnection] Reconnexion...");
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}