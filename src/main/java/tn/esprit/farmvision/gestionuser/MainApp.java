package tn.esprit.farmvision.gestionuser;

import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.model.ResponsableExploitation;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;

import java.util.List;
import java.util.Scanner;

public class MainApp {

    private static final UtilisateurService service = new UtilisateurService();
    private static final Scanner scanner = new Scanner(System.in);
    private static Utilisateur currentUser = null; // Utilisateur connecté (null = déconnecté)

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   TEST CONSOLE - Gestion Utilisateurs   ");
        System.out.println("=====================================");

        while (true) {
            printMenu();
            int choice = readInt("Choix : ");

            switch (choice) {
                case 1 -> inscription();
                case 2 -> connexion();
                case 3 -> afficherTous();
                case 4 -> supprimer();
                case 5 -> valider();
                case 6 -> modifierSimple();
                case 7 -> logout();
                case 0 -> {
                    System.out.println("Au revoir !");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\nMenu :");
        System.out.println("1. Inscription (Sign Up)");
        System.out.println("2. Connexion (Login)");
        System.out.println("3. Afficher tous les utilisateurs");
        System.out.println("4. Supprimer un utilisateur (par ID) " + (currentUser != null && "Administrateur".equals(currentUser.getClass().getSimpleName()) ? "" : "[Admin requis]"));
        System.out.println("5. Valider un utilisateur (par ID) " + (currentUser != null && "Administrateur".equals(currentUser.getClass().getSimpleName()) ? "" : "[Admin requis]"));
        System.out.println("6. Modifier un utilisateur (nom/prénom) " + (currentUser != null && "Administrateur".equals(currentUser.getClass().getSimpleName()) ? "" : "[Admin requis]"));
        System.out.println("7. Déconnexion");
        System.out.println("0. Quitter");
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Entrez un nombre valide : ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static boolean isAdminConnected() {
        return currentUser != null && currentUser.getClass().getSimpleName().equals("Administrateur");
    }

    private static void inscription() {
        System.out.println("\n--- Inscription ---");
        String nom = readString("Nom : ");
        String prenom = readString("Prénom : ");
        String email = readString("Email : ");
        String password = readString("Mot de passe : ");
        int role = readInt("Rôle (1 = Agriculteur, 2 = Responsable) : ");

        Utilisateur user;
        if (role == 1) {
            String tel = readString("Téléphone : ");
            String adresse = readString("Adresse : ");
            user = new Agriculteur(nom, prenom, email, password, tel, adresse);
        } else {
            String matricule = readString("Matricule : ");
            user = new ResponsableExploitation(nom, prenom, email, password, matricule);
        }

        try {
            service.register(user);
            System.out.println("Inscription réussie ! ID = " + user.getId() + " (en attente validation)");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void connexion() {
        System.out.println("\n--- Connexion ---");
        String email = readString("Email : ");
        String password = readString("Mot de passe : ");

        try {
            Utilisateur user = service.login(email, password);
            currentUser = user;
            System.out.println("Connecté ! Bienvenue " + user.getNomComplet() + " (" + user.getClass().getSimpleName() + ")");
        } catch (Exception e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
    }

    private static void afficherTous() {
        System.out.println("\n--- Liste des utilisateurs ---");
        List<Utilisateur> users = service.getAll();
        if (users.isEmpty()) {
            System.out.println("Aucun utilisateur.");
            return;
        }
        for (Utilisateur u : users) {
            System.out.println(u.getId() + " | " + u.getNomComplet() + " | " + u.getEmail() +
                    " | " + u.getClass().getSimpleName() + " | Activé : " + u.isActivated());
        }
    }

    private static void supprimer() {
        if (!isAdminConnected()) {
            System.out.println("Vous devez être connecté en tant qu'administrateur pour supprimer.");
            return;
        }
        int id = readInt("ID à supprimer : ");
        service.delete(id);
        System.out.println("Supprimé (si existait).");
    }

    private static void valider() {
        if (!isAdminConnected()) {
            System.out.println("Vous devez être connecté en tant qu'administrateur pour valider.");
            return;
        }
        int id = readInt("ID à valider : ");
        service.validerUtilisateur(id);
        System.out.println("Validé !");
    }

    private static void modifierSimple() {
        if (!isAdminConnected()) {
            System.out.println("Vous devez être connecté en tant qu'administrateur pour modifier.");
            return;
        }
        int id = readInt("ID à modifier : ");

        Utilisateur user = service.getAll().stream()
                .filter(u -> u.getId() == id)
                .findFirst().orElse(null);

        if (user == null) {
            System.out.println("Utilisateur non trouvé.");
            return;
        }

        String newNom = readString("Nouveau nom (" + user.getNom() + ") : ");
        if (!newNom.isEmpty()) user.setNom(newNom);

        String newPrenom = readString("Nouveau prénom (" + user.getPrenom() + ") : ");
        if (!newPrenom.isEmpty()) user.setPrenom(newPrenom);

        service.update(user);
        System.out.println("Modifié !");
    }

    private static void logout() {
        currentUser = null;
        System.out.println("Déconnecté.");
    }
}