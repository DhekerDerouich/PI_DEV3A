package com.pi.controller;

import com.pi.service.EquipementService;
import com.pi.model.Equipement;
import com.pi.view.EquipementView;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class EquipementController {

    private final EquipementService equipementService;
    private final EquipementView equipementView;
    private final Scanner scanner;

    public EquipementController() {
        this.equipementService = new EquipementService();
        this.equipementView = new EquipementView();
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        boolean back = false;

        while (!back) {
            equipementView.showMenu();
            int choice = getIntInput("Choix: ");

            try {
                switch (choice) {
                    case 1:
                        handleAddEquipement();
                        break;
                    case 2:
                        handleShowAllEquipements();
                        break;
                    case 3:
                        handleShowEquipementById();
                        break;
                    case 4:
                        handleUpdateEquipement();
                        break;
                    case 5:
                        handleDeleteEquipement();
                        break;
                    case 6:
                        handleSearchEquipements();
                        break;
                    case 7:
                        handleShowStats();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        equipementView.showError("Choix invalide");
                }
            } catch (Exception e) {
                equipementView.showError(e.getMessage());
            }
        }
    }

    private void handleAddEquipement() throws Exception {
        System.out.println("\n➕ NOUVEL ÉQUIPEMENT");

        System.out.print("Nom: ");
        String nom = scanner.nextLine();

        System.out.print("Type: ");
        String type = scanner.nextLine();

        System.out.print("État (Fonctionnel/En panne/Maintenance): ");
        String etat = scanner.nextLine();

        System.out.print("Date achat (AAAA-MM-JJ): ");
        LocalDate dateAchat = LocalDate.parse(scanner.nextLine());

        System.out.print("Durée vie estimée (années): ");
        int dureeVie = Integer.parseInt(scanner.nextLine());

        System.out.print("ID parcelle (0 si aucun): ");
        int parcelleId = Integer.parseInt(scanner.nextLine());

        Equipement equipement = equipementService.addEquipement(
                nom, type, etat, dateAchat, dureeVie,
                parcelleId == 0 ? null : parcelleId
        );

        equipementView.showMessage("✅ Équipement ajouté ! ID: " + equipement.getId());
    }

    private void handleShowAllEquipements() {
        List<Equipement> equipements = equipementService.getAllEquipements();
        equipementView.showEquipementsList(equipements);
    }

    private void handleShowEquipementById() throws Exception {
        int id = getIntInput("ID de l'équipement: ");
        if (id <= 0) return;

        Equipement equipement = equipementService.getEquipementById(id);
        equipementView.showEquipementDetails(equipement);
    }

    private void handleUpdateEquipement() throws Exception {
        int id = getIntInput("ID de l'équipement à modifier: ");
        if (id <= 0) return;

        System.out.println("\n✏️  MODIFICATION ÉQUIPEMENT #" + id);
        System.out.println("Laissez vide pour garder la valeur actuelle");

        System.out.print("Nouveau nom: ");
        String nom = scanner.nextLine();

        System.out.print("Nouveau type: ");
        String type = scanner.nextLine();

        System.out.print("Nouvel état: ");
        String etat = scanner.nextLine();

        System.out.print("Nouvelle date achat (AAAA-MM-JJ): ");
        String dateStr = scanner.nextLine();
        LocalDate dateAchat = dateStr.isEmpty() ? null : LocalDate.parse(dateStr);

        System.out.print("Nouvelle durée vie: ");
        String dureeStr = scanner.nextLine();
        Integer dureeVie = dureeStr.isEmpty() ? null : Integer.parseInt(dureeStr);

        System.out.print("Nouvel ID parcelle (0 pour aucun): ");
        String parcelleStr = scanner.nextLine();
        Integer parcelleId = parcelleStr.isEmpty() ? null :
                (Integer.parseInt(parcelleStr) == 0 ? null : Integer.parseInt(parcelleStr));

        Equipement updated = equipementService.updateEquipement(
                id,
                nom.isEmpty() ? null : nom,
                type.isEmpty() ? null : type,
                etat.isEmpty() ? null : etat,
                dateAchat,
                dureeVie,
                parcelleId
        );

        equipementView.showMessage("✅ Équipement modifié !");
    }

    private void handleDeleteEquipement() throws Exception {
        int id = getIntInput("ID de l'équipement à supprimer: ");
        if (id <= 0) return;

        System.out.print("Confirmer suppression? (OUI/non): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("OUI")) {
            equipementService.deleteEquipement(id);
            equipementView.showMessage("✅ Équipement supprimé !");
        } else {
            equipementView.showMessage("❌ Suppression annulée");
        }
    }

    private void handleSearchEquipements() {
        System.out.print("Terme de recherche: ");
        String searchTerm = scanner.nextLine();

        List<Equipement> results = equipementService.searchEquipements(searchTerm);
        equipementView.showEquipementsList(results);
    }

    private void handleShowStats() {
        long total = equipementService.getTotalEquipements();
        int fonctionnels = equipementService.countEquipementsByEtat("Fonctionnel");
        int enPanne = equipementService.countEquipementsByEtat("En panne");
        int enMaintenance = equipementService.countEquipementsByEtat("Maintenance");
        double pourcentage = equipementService.getPourcentageEquipementsFonctionnels();

        System.out.println("\n📊 STATISTIQUES DES ÉQUIPEMENTS");
        System.out.println("Total d'équipements: " + total);
        System.out.println("✅ Fonctionnels: " + fonctionnels + " (" + String.format("%.1f", pourcentage) + "%)");
        System.out.println("🔴 En panne: " + enPanne);
        System.out.println("🛠️  En maintenance: " + enMaintenance);
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