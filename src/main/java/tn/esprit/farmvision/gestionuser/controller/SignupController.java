package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.model.ResponsableExploitation;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import org.mindrot.jbcrypt.BCrypt;

// --------------------------------------------------------------------------
// MODE CONSOLE PUR - PLUS AUCUN LIEN AVEC FXML / JAVAFX
// On simule inscription et modification depuis console (appelée par tn.esprit.farmvision.gestionuser.MainApp)
// --------------------------------------------------------------------------

public class SignupController {

    private final UtilisateurService service = new UtilisateurService();
    private Utilisateur userToEdit = null;

    // ----------------------------------------------------------------------
    // Méthode principale : simule l'inscription depuis la console
    // Appelée depuis tn.esprit.farmvision.gestionuser.MainApp.consoleSignUp()
    // ----------------------------------------------------------------------
    public void consoleSignUp() {
        System.out.println("\n--- Inscription ---");

        String nom = readString("Nom : ");
        String prenom = readString("Prénom : ");
        String email = readString("Email : ");
        String password = readString("Mot de passe : ");
        String role = readString("Rôle (Agriculteur ou Responsable) : ");

        // Contrôles de saisie simples (comme dans la version FXML)
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || role == null) {
            System.out.println("Erreur : tous les champs obligatoires doivent être remplis.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Erreur : email invalide (doit contenir @ et .)");
            return;
        }

        if (password.length() < 6) {
            System.out.println("Erreur : mot de passe trop court (minimum 6 caractères)");
            return;
        }

        try {
            Utilisateur newUser;
            if ("Agriculteur".equalsIgnoreCase(role)) {
                String telephone = readString("Téléphone (8 chiffres) : ");
                String adresse = readString("Adresse : ");

                if (telephone.length() != 8 || !telephone.matches("\\d+")) {
                    System.out.println("Erreur : téléphone doit être 8 chiffres");
                    return;
                }

                newUser = new Agriculteur(nom, prenom, email, password, telephone, adresse);
            } else {
                String matricule = readString("Matricule : ");
                if (matricule.isEmpty()) {
                    System.out.println("Erreur : matricule obligatoire pour Responsable");
                    return;
                }
                newUser = new ResponsableExploitation(nom, prenom, email, password, matricule);
            }

            service.register(newUser);
            System.out.println("Inscription réussie ! ID = " + newUser.getId());
            System.out.println("Compte créé en attente de validation par l'admin.");

        } catch (Exception e) {
            System.out.println("Erreur inscription : " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------------
    // Méthode pour simuler la modification depuis la console
    // Appelée depuis tn.esprit.farmvision.gestionuser.MainApp.consoleUpdateUser(int id)
    // ----------------------------------------------------------------------
    public void consoleUpdateUser(int userId) {
        // Recherche simple de l'utilisateur par ID
        Utilisateur user = service.getAll().stream()
                .filter(u -> u.getId() == userId)
                .findFirst().orElse(null);

        if (user == null) {
            System.out.println("Erreur : utilisateur ID " + userId + " non trouvé.");
            return;
        }

        System.out.println("\n--- Modification utilisateur ID " + userId + " ---");
        System.out.println("Laissez vide pour ne pas changer");

        String nom = readString("Nouveau nom (" + user.getNom() + ") : ");
        if (!nom.isEmpty()) user.setNom(nom);

        String prenom = readString("Nouveau prénom (" + user.getPrenom() + ") : ");
        if (!prenom.isEmpty()) user.setPrenom(prenom);

        String email = readString("Nouvel email (" + user.getEmail() + ") : ");
        if (!email.isEmpty()) {
            if (!email.contains("@") || !email.contains(".")) {
                System.out.println("Erreur : email invalide");
                return;
            }
            user.setEmail(email);
        }

        String password = readString("Nouveau mot de passe (vide = inchangé) : ");
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                System.out.println("Erreur : mot de passe trop court");
                return;
            }
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        }

        if (user instanceof Agriculteur a) {
            String tel = readString("Nouveau téléphone (" + a.getTelephone() + ") : ");
            if (!tel.isEmpty()) a.setTelephone(tel);

            String adresse = readString("Nouvelle adresse (" + a.getAdresse() + ") : ");
            if (!adresse.isEmpty()) a.setAdresse(adresse);
        } else if (user instanceof ResponsableExploitation r) {
            String matricule = readString("Nouveau matricule (" + r.getMatricule() + ") : ");
            if (!matricule.isEmpty()) r.setMatricule(matricule);
        }

        try {
            service.update(user);
            System.out.println("Modification réussie !");
        } catch (Exception e) {
            System.out.println("Erreur modification : " + e.getMessage());
        }
    }

    // Méthode utilitaire console
    private String readString(String prompt) {
        System.out.print(prompt);
        return new java.util.Scanner(System.in).nextLine().trim();
    }

    // Méthode pour afficher un message d'erreur console
    private void showError(String msg) {
        System.out.println("ERREUR : " + msg);
    }

    // Méthode pour afficher un message de succès console
    private void showSuccess(String msg) {
        System.out.println("SUCCÈS : " + msg);
    }
}