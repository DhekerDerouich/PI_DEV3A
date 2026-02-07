package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.model.*;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;

import java.util.List;

public class GestionUsersController {

    private final UtilisateurService service = new UtilisateurService();

    // Simulation console (plus de FXML/TableView)
    public void afficherGestion() {
        System.out.println("\n=== Gestion des Utilisateurs (Console) ===");
        List<Utilisateur> users = service.getAll();

        if (users.isEmpty()) {
            System.out.println("Aucun utilisateur.");
            return;
        }

        System.out.println("ID | Nom | Prénom | Email | Rôle | Activé | Spécifique");
        for (Utilisateur u : users) {
            String spec = "";
            if (u instanceof Agriculteur a) spec = a.getTelephone() + " / " + a.getAdresse();
            else if (u instanceof ResponsableExploitation r) spec = r.getMatricule();
            else if (u instanceof Administrateur a) spec = a.getMatricule();

            System.out.println(u.getId() + " | " + u.getNom() + " | " + u.getPrenom() + " | " + u.getEmail() +
                    " | " + u.getClass().getSimpleName() + " | " + u.isActivated() + " | " + spec);
        }
    }

    public void supprimer(int id) {
        service.delete(id);
        System.out.println("Supprimé (si existait).");
    }

    public void valider(int id) {
        service.validerUtilisateur(id);
        System.out.println("Validé !");
    }

    // Ajoute ici les autres actions (ajout, modification) si besoin
}