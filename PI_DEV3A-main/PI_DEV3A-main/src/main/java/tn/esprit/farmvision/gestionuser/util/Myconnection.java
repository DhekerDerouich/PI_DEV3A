package tn.esprit.farmvision.gestionuser.util;  // ✅ Changement du package

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Myconnection {

    private static final String URL = "jdbc:mysql://localhost:3306/farmvision_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données établie !");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL non trouvé !");
                e.printStackTrace();
                throw new SQLException("Driver MySQL non trouvé", e);
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("✅ Connexion fermée");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}