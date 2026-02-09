package com.pi.view;

import com.pi.model.Equipement;
import java.util.List;

public class EquipementView {

    public void showMenu() {
        System.out.println("\n=== GESTION DES ÉQUIPEMENTS ===");
        System.out.println("1. ➕ Ajouter un équipement");
        System.out.println("2. 📋 Lister tous les équipements");
        System.out.println("3. 🔍 Voir un équipement par ID");
        System.out.println("4. ✏️  Modifier un équipement");
        System.out.println("5. 🗑️  Supprimer un équipement");
        System.out.println("0. ↩️  Retour au menu principal");
    }

    public void showEquipementsList(List<Equipement> equipements) {
        System.out.println("\n=== LISTE DES ÉQUIPEMENTS ===");

        if (equipements == null || equipements.isEmpty()) {
            System.out.println("Aucun équipement enregistré.");
            return;
        }

        System.out.println("┌─────┬──────────────────────┬────────────────┬──────────────┬──────────────┐");
        System.out.println("│ ID  │ Nom                  │ Type           │ État         │ Date Achat   │");
        System.out.println("├─────┼──────────────────────┼────────────────┼──────────────┼──────────────┤");

        for (Equipement e : equipements) {
            System.out.printf("│ %-3d │ %-20s │ %-14s │ %-12s │ %-12s │\n",
                    e.getId(),
                    truncate(e.getNom(), 20),
                    truncate(e.getType(), 14),
                    truncate(e.getEtat(), 12),
                    e.getDateAchat().toString()
            );
        }

        System.out.println("└─────┴──────────────────────┴────────────────┴──────────────┴──────────────┘");
        System.out.println("Total: " + equipements.size() + " équipement(s)");
    }

    public void showEquipementDetails(Equipement equipement) {
        System.out.println("\n=== DÉTAILS DE L'ÉQUIPEMENT ===");
        System.out.println("ID: " + equipement.getId());
        System.out.println("Nom: " + equipement.getNom());
        System.out.println("Type: " + equipement.getType());
        System.out.println("État: " + equipement.getEtat());
        System.out.println("Date d'achat: " + equipement.getDateAchat());
        System.out.println("Durée de vie estimée: " + equipement.getDureeVieEstimee() + " ans");
        System.out.println("Parcelle ID: " +
                (equipement.getParcelleId() != null ? equipement.getParcelleId() : "Non assigné"));
        System.out.println("Créé le: " + equipement.getCreatedAt());
        System.out.println("Mis à jour le: " + equipement.getUpdatedAt());
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