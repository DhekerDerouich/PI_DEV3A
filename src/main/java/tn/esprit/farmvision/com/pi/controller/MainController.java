package com.pi.controller;

import com.pi.service.EquipementService;
import com.pi.service.MaintenanceService;
import com.pi.model.Maintenance;
import java.util.Scanner;
import java.util.List;

public class MainController {
    private Scanner scanner;
    private EquipementController equipementController;
    private MaintenanceController maintenanceController;
    private EquipementService equipementService;
    private MaintenanceService maintenanceService;

    public MainController() {
        this.scanner = new Scanner(System.in);
        this.equipementController = new EquipementController();
        this.maintenanceController = new MaintenanceController();
        this.equipementService = new EquipementService();
        this.maintenanceService = new MaintenanceService();
    }

    public void start() {
        System.out.println("=======================================");
        System.out.println("  SYSTÈME DE GESTION AGRICOLE - PI    ");
        System.out.println("=======================================");

        boolean running = true;

        while (running) {
            showMainMenu();
            int choice = getIntInput("Votre choix: ");

            switch (choice) {
                case 1:
                    equipementController.showMenu();
                    break;
                case 2:
                    maintenanceController.showMenu();
                    break;
                case 3:
                    showDashboard();
                    break;
                case 0:
                    running = false;
                    System.out.println("\nAu revoir !");
                    break;
                default:
                    System.out.println("❌ Choix invalide");
            }
        }
        scanner.close();
    }

    private void showMainMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. 🚜 Gestion des Équipements");
        System.out.println("2. 🔧 Gestion des Maintenances");
        System.out.println("3. 📊 Tableau de bord");
        System.out.println("0. ❌ Quitter");
    }

    private void showDashboard() {
        System.out.println("\n=== TABLEAU DE BORD ===");

        // Statistiques équipements
        long totalEquipements = equipementService.getTotalEquipements();
        int fonctionnels = equipementService.countEquipementsByEtat("Fonctionnel");
        int enPanne = equipementService.countEquipementsByEtat("En panne");
        int enMaintenance = equipementService.countEquipementsByEtat("Maintenance");

        System.out.println("\n📊 ÉQUIPEMENTS");
        System.out.println("Total: " + totalEquipements);
        System.out.println("✅ Fonctionnels: " + fonctionnels);
        System.out.println("🔴 En panne: " + enPanne);
        System.out.println("🛠️  En maintenance: " + enMaintenance);

        // Statistiques maintenances
        double totalCout = maintenanceService.getCoutTotalMaintenances();
        int planifiees = maintenanceService.countMaintenancesByStatut("Planifiée");
        int realisees = maintenanceService.countMaintenancesByStatut("Réalisée");
        List<Maintenance> upcoming = maintenanceService.getUpcomingMaintenances();

        System.out.println("\n🔧 MAINTENANCES");
        System.out.printf("Coût total: %.2f DT\n", totalCout);
        System.out.println("📅 Planifiées: " + planifiees);
        System.out.println("✅ Réalisées: " + realisees);
        System.out.println("🚨 À venir (7j): " + upcoming.size());

        // Alertes
        System.out.println("\n⚠️  ALERTES");
        if (enPanne > 0) {
            System.out.println("🔴 " + enPanne + " équipement(s) en panne");
        }
        if (!upcoming.isEmpty()) {
            System.out.println("📅 " + upcoming.size() + " maintenance(s) à venir");
        }
        if (enPanne == 0 && upcoming.isEmpty()) {
            System.out.println("✅ Aucune alerte");
        }
    }

    private int getIntInput(String message) {
        System.out.print(message);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}