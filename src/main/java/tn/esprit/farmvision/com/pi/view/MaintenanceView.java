package com.pi.view;

import com.pi.model.Maintenance;
import java.util.List;

public class MaintenanceView {

    public void showMenu() {
        System.out.println("\n=== GESTION DES MAINTENANCES ===");
        System.out.println("1. 📅 Planifier une maintenance");
        System.out.println("2. 📋 Historique des maintenances");
        System.out.println("3. 🔍 Maintenances par équipement");
        System.out.println("4. ✏️  Modifier une maintenance");
        System.out.println("5. 🔄 Changer le statut");
        System.out.println("6. 🗑️  Supprimer une maintenance");
        System.out.println("0. ↩️  Retour au menu principal");
    }

    public void showMaintenancesList(List<Maintenance> maintenances) {
        System.out.println("\n=== LISTE DES MAINTENANCES ===");

        if (maintenances == null || maintenances.isEmpty()) {
            System.out.println("Aucune maintenance enregistrée.");
            return;
        }

        System.out.println("┌─────┬─────────────┬────────────────┬──────────────┬──────────────┬──────────┐");
        System.out.println("│ ID  │ Équipement  │ Type           │ Statut       │ Date         │ Coût     │");
        System.out.println("├─────┼─────────────┼────────────────┼──────────────┼──────────────┼──────────┤");

        for (Maintenance m : maintenances) {
            System.out.printf("│ %-3d │ %-11d │ %-14s │ %-12s │ %-12s │ %-8.2f │\n",
                    m.getId(),
                    m.getEquipementId(),
                    truncate(m.getTypeMaintenance(), 14),
                    truncate(m.getStatut(), 12),
                    m.getDateMaintenance().toString(),
                    m.getCout()
            );
        }

        System.out.println("└─────┴─────────────┴────────────────┴──────────────┴──────────────┴──────────┘");
        System.out.println("Total: " + maintenances.size() + " maintenance(s)");
    }

    public void showMaintenanceDetails(Maintenance maintenance) {
        System.out.println("\n=== DÉTAILS DE LA MAINTENANCE ===");
        System.out.println("ID: " + maintenance.getId());
        System.out.println("Équipement ID: " + maintenance.getEquipementId());
        System.out.println("Type: " + maintenance.getTypeMaintenance());
        System.out.println("Description: " + maintenance.getDescription());
        System.out.println("Date: " + maintenance.getDateMaintenance());
        System.out.println("Coût: " + maintenance.getCout() + " DT");
        System.out.println("Statut: " + maintenance.getStatut());
        System.out.println("Créée le: " + maintenance.getCreatedAt());
    }

    public void showMessage(String message) {
        System.out.println("\n" + message);
    }

    public void showError(String error) {
        System.err.println("\n❌ " + error);
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}