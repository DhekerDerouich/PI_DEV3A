package com.pi;

import com.pi.controller.MainController;
import com.pi.dao.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("=======================================");
        System.out.println("  SYSTÈME DE GESTION AGRICOLE - PI    ");
        System.out.println("=======================================");

        // Tester la connexion à la base
        try {
            if (DatabaseConnection.getConnection() != null) {
                System.out.println("✅ Connexion BD établie");

                // Lancer le contrôleur principal
                MainController controller = new MainController();
                controller.start();
            } else {
                System.out.println("❌ Impossible de se connecter à la base");
            }
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}