package com.pi.controller;

import com.pi.service.MaintenanceService;
import com.pi.service.EquipementService;
import com.pi.model.Maintenance;
import com.pi.view.MaintenanceView;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final EquipementService equipementService;
    private final MaintenanceView maintenanceView;
    private final Scanner scanner;

    public MaintenanceController() {
        this.maintenanceService = new MaintenanceService();
        this.equipementService = new EquipementService();
        this.maintenanceView = new MaintenanceView();
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        boolean back = false;

        while (!back) {
            maintenanceView.showMenu();
            int choice = getIntInput("Choix: ");

            try {
                switch (choice) {
                    case 1:
                        handleAddMaintenance();
                        break;
                    case 2:
                        handleShowAllMaintenances();
                        break;
                    case 3:
                        handleShowByEquipement();
                        break;
                    case 4:
                        handleUpdateMaintenance();
                        break;
                    case 5:
                        handleChangeStatus();
                        break;
                    case 6:
                        handleDeleteMaintenance();
                        break;
                    case 7:
                        handleShowUpcoming();
                        break;
                    case 8:
                        handleShowStats();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        maintenanceView.showError("Choix invalide");
                }
            } catch (Exception e) {
                maintenanceView.showError(e.getMessage());
            }
        }
    }

    private void handleAddMaintenance() throws Exception {
        System.out.println("\n📅 PLANIFIER UNE MAINTENANCE");

        System.out.print("ID de l'équipement: ");
        int equipementId = Integer.parseInt(scanner.nextLine());

        System.out.print("Type (Préventive/Corrective): ");
        String type = scanner.nextLine();

        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Date maintenance (AAAA-MM-JJ): ");
        LocalDate dateMaintenance = LocalDate.parse(scanner.nextLine());

        System.out.print("Coût: ");
        double cout = Double.parseDouble(scanner.nextLine());

        System.out.print("Statut (Planifiée/Réalisée): ");
        String statut = scanner.nextLine();

        Maintenance maintenance = maintenanceService.planifierMaintenance(
                equipementId, type, description, dateMaintenance, cout, statut
        );

        maintenanceView.showMessage("✅ Maintenance planifiée ! ID: " + maintenance.getId());
    }

    private void handleShowAllMaintenances() {
        List<Maintenance> maintenances = maintenanceService.getAllMaintenances();
        maintenanceView.showMaintenancesList(maintenances);
    }

    private void handleShowByEquipement() throws Exception {
        int equipementId = getIntInput("ID de l'équipement: ");
        if (equipementId <= 0) return;

        List<Maintenance> maintenances = maintenanceService.getMaintenancesByEquipement(equipementId);
        maintenanceView.showMaintenancesList(maintenances);
    }

    private void handleUpdateMaintenance() throws Exception {
        int id = getIntInput("ID de la maintenance à modifier: ");
        if (id <= 0) return;

        System.out.println("\n✏️  MODIFICATION MAINTENANCE #" + id);
        System.out.println("Laissez vide pour garder la valeur actuelle");

        System.out.print("Nouvel ID équipement: ");
        String equipStr = scanner.nextLine();
        Integer equipementId = equipStr.isEmpty() ? null : Integer.parseInt(equipStr);

        System.out.print("Nouveau type: ");
        String type = scanner.nextLine();

        System.out.print("Nouvelle description: ");
        String description = scanner.nextLine();

        System.out.print("Nouvelle date (AAAA-MM-JJ): ");
        String dateStr = scanner.nextLine();
        LocalDate dateMaintenance = dateStr.isEmpty() ? null : LocalDate.parse(dateStr);

        System.out.print("Nouveau coût: ");
        String coutStr = scanner.nextLine();
        Double cout = coutStr.isEmpty() ? null : Double.parseDouble(coutStr);

        System.out.print("Nouveau statut: ");
        String statut = scanner.nextLine();

        Maintenance updated = maintenanceService.updateMaintenance(
                id, equipementId,
                type.isEmpty() ? null : type,
                description.isEmpty() ? null : description,
                dateMaintenance,
                cout,
                statut.isEmpty() ? null : statut
        );

        maintenanceView.showMessage("✅ Maintenance modifiée !");
    }

    private void handleChangeStatus() throws Exception {
        int id = getIntInput("ID de la maintenance: ");
        if (id <= 0) return;

        System.out.print("Nouveau statut (Planifiée/Réalisée): ");
        String newStatut = scanner.nextLine();

        maintenanceService.changerStatutMaintenance(id, newStatut);
        maintenanceView.showMessage("✅ Statut changé à : " + newStatut);
    }

    private void handleDeleteMaintenance() throws Exception {
        int id = getIntInput("ID de la maintenance à supprimer: ");
        if (id <= 0) return;

        System.out.print("Confirmer suppression? (OUI/non): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("OUI")) {
            maintenanceService.deleteMaintenance(id);
            maintenanceView.showMessage("✅ Maintenance supprimée !");
        } else {
            maintenanceView.showMessage("❌ Suppression annulée");
        }
    }

    private void handleShowUpcoming() {
        List<Maintenance> upcoming = maintenanceService.getUpcomingMaintenances();

        System.out.println("\n📅 MAINTENANCES À VENIR (7 PROCHAINS JOURS)");
        if (upcoming.isEmpty()) {
            System.out.println("Aucune maintenance à venir.");
        } else {
            maintenanceView.showMaintenancesList(upcoming);
        }
    }

    private void handleShowStats() {
        double totalCout = maintenanceService.getCoutTotalMaintenances();
        double coutMoyen = maintenanceService.getCoutMoyenMaintenance();
        int planifiees = maintenanceService.countMaintenancesByStatut("Planifiée");
        int realisees = maintenanceService.countMaintenancesByStatut("Réalisée");
        int preventives = maintenanceService.countMaintenancesByType("Préventive");
        int correctives = maintenanceService.countMaintenancesByType("Corrective");
        long aujourdhui = maintenanceService.getNombreMaintenancesAujourdhui();

        System.out.println("\n📊 STATISTIQUES DES MAINTENANCES");
        System.out.printf("💰 Coût total: %.2f DT\n", totalCout);
        System.out.printf("💰 Coût moyen: %.2f DT\n", coutMoyen);
        System.out.println("📅 Planifiées: " + planifiees);
        System.out.println("✅ Réalisées: " + realisees);
        System.out.println("🛡️  Préventives: " + preventives);
        System.out.println("🔧 Correctives: " + correctives);
        System.out.println("⚠️  Aujourd'hui: " + aujourdhui + " maintenance(s)");
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