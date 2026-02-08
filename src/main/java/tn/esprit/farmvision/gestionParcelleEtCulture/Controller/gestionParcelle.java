package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;


import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;

import tn.esprit.farmvision.gestionParcelleEtCulture.service.ParcelleService;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.CultureService;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class gestionParcelle {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // On utilise TES services existants
        ParcelleService parcelleService = new ParcelleService();
        CultureService cultureService = new CultureService();

        int choix = -1;

        while (choix != 0) {

            System.out.println("\n===== GESTION PARCELLE & CULTURE =====");

            System.out.println("1. Ajouter une parcelle");
            System.out.println("2. Afficher les parcelles");
            System.out.println("3. Supprimer une parcelle");

            System.out.println("4. Ajouter une culture");
            System.out.println("5. Afficher les cultures");
            System.out.println("6. Supprimer une culture");

            System.out.println("0. Quitter");

            System.out.print("Votre choix : ");
            choix = sc.nextInt();
            sc.nextLine(); // vider buffer

            try {

                switch (choix) {

                    // ===== PARCELLE =====

                    case 1:
                        System.out.print("Surface : ");
                        float surface = sc.nextFloat();
                        sc.nextLine();

                        System.out.print("Localisation : ");
                        String localisation = sc.nextLine();

                        parcelleService.ajouter(
                                new Parcelle(0, surface, localisation)
                        );

                        System.out.println("✅ Parcelle ajoutée");
                        break;

                    case 2:
                        List<Parcelle> parcelles =
                                parcelleService.afficher();

                        parcelles.forEach(p ->
                                System.out.println(
                                        p.getIdParcelle() + " | " +
                                                p.getSurface() + " | " +
                                                p.getLocalisation()
                                )
                        );
                        break;

                    case 3:
                        System.out.print("ID parcelle à supprimer : ");
                        int idP = sc.nextInt();

                        parcelleService.supprimer(idP);

                        System.out.println("🗑 Parcelle supprimée");
                        break;

                    // ===== CULTURE =====

                    case 4:
                        System.out.print("Nom culture : ");
                        String nom = sc.nextLine();

                        System.out.print("Type culture : ");
                        String type = sc.nextLine();

                        System.out.print("Date semis (YYYY-MM-DD) : ");
                        Date semis = Date.valueOf(sc.nextLine());

                        System.out.print("Date récolte (YYYY-MM-DD) : ");
                        Date recolte = Date.valueOf(sc.nextLine());

                        cultureService.ajouter(
                                new Culture(0, nom, type, semis, recolte)
                        );

                        System.out.println("✅ Culture ajoutée");
                        break;

                    case 5:
                        List<Culture> cultures =
                                cultureService.afficher();

                        cultures.forEach(c ->
                                System.out.println(
                                        c.getIdCulture() + " | " +
                                                c.getNomCulture() + " | " +
                                                c.getTypeCulture() + " | " +
                                                c.getDateSemis() + " | " +
                                                c.getDateRecolte()
                                )
                        );
                        break;

                    case 6:
                        System.out.print("ID culture à supprimer : ");
                        int idC = sc.nextInt();

                        cultureService.supprimer(idC);

                        System.out.println("🗑 Culture supprimée");
                        break;

                    case 0:
                        System.out.println("Fin du programme");
                        break;

                    default:
                        System.out.println("Choix invalide");
                }

            } catch (Exception e) {
                System.err.println("Erreur : " + e.getMessage());
            }
        }

        sc.close();
    }
}
