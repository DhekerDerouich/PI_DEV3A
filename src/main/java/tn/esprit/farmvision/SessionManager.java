package tn.esprit.farmvision;  // ← ton package racine (change si tu as un autre)

import tn.esprit.farmvision.gestionuser.model.Utilisateur;

public class SessionManager {

    // Instance unique (pattern Singleton)
    private static SessionManager instance;

    // L'utilisateur actuellement connecté
    private Utilisateur currentUser;

    // Constructeur privé pour empêcher l'instanciation directe
    private SessionManager() {
    }

    // Méthode pour récupérer l'instance unique
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Récupérer l'utilisateur connecté
    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    // Définir l'utilisateur après login réussi
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        System.out.println("Session ouverte pour : " + user.getNomComplet() + " (" + user.getClass().getSimpleName() + ")");
    }

    // Déconnexion (logout)
    public void logout() {
        if (currentUser != null) {
            System.out.println("Déconnexion de : " + currentUser.getNomComplet());
        }
        currentUser = null;
    }

    // Vérifier si quelqu'un est connecté
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Récupérer directement l'ID (pratique pour les filtres user_id)
    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    // Récupérer le rôle (ex. "Administrateur", "Agriculteur", ...)
    public String getCurrentUserRole() {
        if (currentUser == null) return "Aucun";
        return currentUser.getClass().getSimpleName();
    }
}