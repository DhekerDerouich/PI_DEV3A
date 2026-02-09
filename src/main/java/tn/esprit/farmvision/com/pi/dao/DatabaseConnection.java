package com.pi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/farmvision_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                // Vérifier si la connexion est réussie
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ Connexion à la base de données établie avec succès !");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé !");
                System.err.println("➡️ Assurez-vous d'avoir mysql-connector-java dans votre pom.xml");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données !");
                System.err.println("Message : " + e.getMessage());
                System.err.println("➡️ Vérifiez que :");
                System.err.println("   1. XAMPP est démarré (Apache et MySQL)");
                System.err.println("   2. Le port MySQL est 3306");
                System.err.println("   3. La base 'farmvision_db' existe");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("✅ Connexion fermée avec succès.");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}