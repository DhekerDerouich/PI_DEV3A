package Finance.Controller;

import Finance.model.Depense;
import Finance.model.Revenu;
import Finance.service.depenseService;
import Finance.service.revenuService;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


public class FinanceController {

    public static void main(String[] args) {


        depenseService depenseService = new depenseService();
        revenuService revenuService = new revenuService();
        Scanner sc = new Scanner(System.in);

        int choix = -1;

        while (choix != 0) {
            System.out.println("\n===== GESTION FINANCIERE =====");
            System.out.println("1. Ajouter une dépense");
            System.out.println("2. Afficher toutes les dépenses");
            System.out.println("3. Supprimer une dépense");
            System.out.println("4. Chercher une dépense par ID");

            System.out.println("5. Ajouter un revenu");
            System.out.println("6. Afficher tous les revenus");
            System.out.println("7. Supprimer un revenu");
            System.out.println("8. Chercher un revenu par ID");

            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");
            choix = sc.nextInt();
            sc.nextLine(); // clear buffer

            try {
                switch (choix) {

                    // ===== DEPENSE =====
                    case 1:
                        System.out.print("Montant : ");
                        double montant = sc.nextDouble();
                        sc.nextLine();

                        System.out.print("Type de dépense : ");
                        String type = sc.nextLine();

                        System.out.print("Date (YYYY-MM-DD) : ");
                        Date dateDep = Date.valueOf(sc.nextLine());

                        depenseService.ajouterDepense(
                                new Depense(montant, type, dateDep)
                        );
                        System.out.println("✅ Dépense ajoutée");
                        break;

                    case 2:
                        List<Depense> depenses = depenseService.getAllDepenses();
                        depenses.forEach(System.out::println);
                        break;

                    case 3:
                        System.out.print("ID dépense à supprimer : ");
                        depenseService.deleteDepense(sc.nextLong());
                        System.out.println("Dépense supprimée");
                        break;

                    case 4:
                        System.out.print("ID dépense : ");
                        Depense d = depenseService.getDepenseById(sc.nextLong());
                        System.out.println(d != null ? d : "Dépense introuvable");
                        break;

                    // ===== REVENU =====
                    case 5:
                        System.out.print("Montant : ");
                        double m = sc.nextDouble();
                        sc.nextLine();

                        System.out.print("Source : ");
                        String source = sc.nextLine();

                        System.out.print("Date (YYYY-MM-DD) : ");
                        Date dateRev = Date.valueOf(sc.nextLine());

                        revenuService.ajouterRevenu(
                                new Revenu(m, source, dateRev)
                        );
                        System.out.println("Revenu ajouté");
                        break;

                    case 6:
                        List<Revenu> revenus = revenuService.getAllRevenus();
                        revenus.forEach(System.out::println);
                        break;

                    case 7:
                        System.out.print("ID revenu à supprimer : ");
                        revenuService.deleteRevenu(sc.nextLong());
                        System.out.println("Revenu supprimé");
                        break;

                    case 8:
                        System.out.print("ID revenu : ");
                        Revenu r = revenuService.getRevenuById(sc.nextLong());
                        System.out.println(r != null ? r : "Revenu introuvable");
                        break;

                    case 0:
                        System.out.println("Fin du programme");
                        break;

                    default:
                        System.out.println("Choix invalide");
                }

            } catch (SQLException e) {
                System.err.println("Erreur SQL : " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur saisie : " + e.getMessage());
            }
        }

        sc.close();
    }
}