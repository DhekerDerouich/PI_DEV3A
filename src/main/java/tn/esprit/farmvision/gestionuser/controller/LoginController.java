package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;

// --------------------------------------------------------------------------
// MODE CONSOLE PUR : plus aucun lien avec FXML / JavaFX
// On simule juste la logique de login pour test backend
// --------------------------------------------------------------------------

public class LoginController {

    private final UtilisateurService service = new UtilisateurService();

    // ----------------------------------------------------------------------
    // Méthode principale : simule le login depuis la console
    // (appelée depuis MainApp.consoleLogin() par exemple)
    // ----------------------------------------------------------------------
    public Utilisateur consoleLogin(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Erreur : email et mot de passe obligatoires.");
            return null;
        }

        try {
            Utilisateur user = service.login(email.trim(), password);
            System.out.println("Connexion réussie ! Bienvenue " + user.getNomComplet() +
                    " (" + user.getClass().getSimpleName() + ")");
            return user;
        } catch (Exception e) {
            System.out.println("Erreur connexion : " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------------
    // Méthode pour simuler le clic sur "S'inscrire"
    // ----------------------------------------------------------------------
    public void consoleSignUpRequest() {
        System.out.println("Inscription demandée → retour au menu principal (option 1)");
        // Ici tu peux appeler directement la méthode inscription() de MainApp si tu veux
    }
}