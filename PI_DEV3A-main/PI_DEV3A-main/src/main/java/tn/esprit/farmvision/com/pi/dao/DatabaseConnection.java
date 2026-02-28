package tn.esprit.farmvision.com.pi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // ✅ CORRIGEZ LE NOM DE LA BASE (farmvision_db, pas farmvisonn)
    private static final String URL = "jdbc:mysql://localhost:3306/farmvision_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données établie !");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé !");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données !");
                System.err.println("Message : " + e.getMessage());
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

    // ✅ AJOUTEZ CETTE MÉTHODE
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}