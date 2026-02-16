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
            System.out.println("7. Modifier une parcelle");
            System.out.println("8. Modifier une culture");
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
                    // ========== MODIFIER PARCELLE (avec affichage des valeurs actuelles) ==========
                    case 7:
                        System.out.print("ID de la parcelle à modifier : ");
                        int idParcelleModif = sc.nextInt();
                        sc.nextLine();

                        // 1. Récupérer la parcelle existante
                        Parcelle existingParcelle = parcelleService.getById(idParcelleModif);
                        if (existingParcelle == null) {
                            System.out.println("❌ Aucune parcelle trouvée avec cet ID.");
                            break;
                        }

                        // 2. Afficher les valeurs actuelles
                        System.out.println("\n--- Valeurs actuelles ---");
                        System.out.println("Surface      : " + existingParcelle.getSurface());
                        System.out.println("Localisation : " + existingParcelle.getLocalisation());
                        System.out.println("--------------------------");

                        // 3. Nouvelle surface (laisser vide pour conserver)
                        System.out.print("Nouvelle surface (laisser vide pour garder " + existingParcelle.getSurface() + ") : ");
                        String surfaceInput = sc.nextLine();
                        float nouvelleSurface;
                        if (surfaceInput.isEmpty()) {
                            nouvelleSurface = existingParcelle.getSurface();
                        } else {
                            nouvelleSurface = Float.parseFloat(surfaceInput);
                        }

                        // 4. Nouvelle localisation (laisser vide pour conserver)
                        System.out.print("Nouvelle localisation (laisser vide pour garder \"" + existingParcelle.getLocalisation() + "\") : ");
                        String nouvelleLocalisation = sc.nextLine();
                        if (nouvelleLocalisation.isEmpty()) {
                            nouvelleLocalisation = existingParcelle.getLocalisation();
                        }

                        // 5. Créer l'objet modifié et appeler le service
                        Parcelle parcelleModifiee = new Parcelle(
                                idParcelleModif,
                                nouvelleSurface,
                                nouvelleLocalisation
                        );
                        parcelleService.modifier(parcelleModifiee);
                        System.out.println("✏️ Parcelle modifiée avec succès !");
                        break;

                    // ========== MODIFIER CULTURE (avec affichage des valeurs actuelles) ==========
                    case 8:
                        System.out.print("ID de la culture à modifier : ");
                        int idCultureModif = sc.nextInt();
                        sc.nextLine();

                        // 1. Récupérer la culture existante
                        Culture existingCulture = cultureService.getById(idCultureModif);
                        if (existingCulture == null) {
                            System.out.println("❌ Aucune culture trouvée avec cet ID.");
                            break;
                        }

                        // 2. Afficher les valeurs actuelles
                        System.out.println("\n--- Valeurs actuelles ---");
                        System.out.println("Nom     : " + existingCulture.getNomCulture());
                        System.out.println("Type    : " + existingCulture.getTypeCulture());
                        System.out.println("Semis   : " + existingCulture.getDateSemis());
                        System.out.println("Récolte : " + existingCulture.getDateRecolte());
                        System.out.println("--------------------------");

                        // 3. Nouveau nom
                        System.out.print("Nouveau nom (laisser vide pour garder \"" + existingCulture.getNomCulture() + "\") : ");
                        String newNom = sc.nextLine();
                        if (newNom.isEmpty()) {
                            newNom = existingCulture.getNomCulture();
                        }

                        // 4. Nouveau type
                        System.out.print("Nouveau type (laisser vide pour garder \"" + existingCulture.getTypeCulture() + "\") : ");
                        String newType = sc.nextLine();
                        if (newType.isEmpty()) {
                            newType = existingCulture.getTypeCulture();
                        }

                        // 5. Nouvelle date de semis
                        System.out.print("Nouvelle date de semis (YYYY-MM-DD, laisser vide pour garder " + existingCulture.getDateSemis() + ") : ");
                        String semisInput = sc.nextLine();
                        Date newSemis;
                        if (semisInput.isEmpty()) {
                            newSemis = existingCulture.getDateSemis();
                        } else {
                            newSemis = Date.valueOf(semisInput);
                        }

                        // 6. Nouvelle date de récolte
                        System.out.print("Nouvelle date de récolte (YYYY-MM-DD, laisser vide pour garder " + existingCulture.getDateRecolte() + ") : ");
                        String recolteInput = sc.nextLine();
                        Date newRecolte;
                        if (recolteInput.isEmpty()) {
                            newRecolte = existingCulture.getDateRecolte();
                        } else {
                            newRecolte = Date.valueOf(recolteInput);
                        }

                        // 7. Créer l'objet modifié et appeler le service
                        Culture cultureModifiee = new Culture(
                                idCultureModif,
                                newNom,
                                newType,
                                newSemis,
                                newRecolte
                        );
                        cultureService.modifier(cultureModifiee);
                        System.out.println("✏️ Culture modifiée avec succès !");
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
