package tn.esprit.farmvision;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.dao.UtilisateurDAO;

import java.io.*;
import java.util.Properties;

/**
 * 🔐 Gestionnaire de session avec PERSISTANCE
 * Sauvegarde la session localement pour reconnexion automatique
 * Comme Facebook/Instagram !
 */
public class SessionManager {

    // Instance unique (Singleton)
    private static SessionManager instance;

    // Utilisateur connecté
    private Utilisateur currentUser;

    // Fichier de sauvegarde de session
    private static final String SESSION_FILE = "farmvision_session.properties";

    // DAO pour récupérer l'utilisateur
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // Constructeur privé
    private SessionManager() {
    }

    // Instance unique
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ========== NOUVELLE FONCTIONNALITÉ : SESSION PERSISTANTE ==========

    /**
     * 💾 Sauvegarde la session dans un fichier local
     * Appelé après login réussi
     */
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        System.out.println("✅ Session ouverte pour : " + user.getNomComplet() + " (" + user.getClass().getSimpleName() + ")");

        // ✅ SAUVEGARDER LA SESSION LOCALEMENT
        saveSessionToFile(user);
    }

    /**
     * 💾 Sauvegarde l'ID et l'email dans un fichier
     */
    private void saveSessionToFile(Utilisateur user) {
        try {
            Properties props = new Properties();
            props.setProperty("userId", String.valueOf(user.getId()));
            props.setProperty("userEmail", user.getEmail());
            props.setProperty("lastLogin", String.valueOf(System.currentTimeMillis()));

            // Sauvegarder dans le dossier de l'application
            File sessionFile = new File(SESSION_FILE);
            FileOutputStream fos = new FileOutputStream(sessionFile);
            props.store(fos, "FarmVision Session - Ne pas partager ce fichier");
            fos.close();

            System.out.println("💾 Session sauvegardée localement");

        } catch (IOException e) {
            System.err.println("⚠️ Erreur sauvegarde session : " + e.getMessage());
        }
    }

    /**
     * 🔄 Restaure la session depuis le fichier
     * Appelé au démarrage de l'application
     * @return true si session restaurée, false sinon
     */
    public boolean restoreSessionFromFile() {
        try {
            File sessionFile = new File(SESSION_FILE);

            if (!sessionFile.exists()) {
                System.out.println("ℹ️ Aucune session sauvegardée");
                return false;
            }

            // Lire le fichier
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(sessionFile);
            props.load(fis);
            fis.close();

            // Récupérer les infos
            String userIdStr = props.getProperty("userId");
            String userEmail = props.getProperty("userEmail");
            String lastLoginStr = props.getProperty("lastLogin");

            if (userIdStr == null || userEmail == null) {
                System.out.println("⚠️ Session invalide");
                deleteSessionFile();
                return false;
            }

            int userId = Integer.parseInt(userIdStr);
            long lastLogin = Long.parseLong(lastLoginStr);

            // Vérifier si la session n'est pas trop ancienne (30 jours max)
            long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() - lastLogin > thirtyDaysInMillis) {
                System.out.println("⚠️ Session expirée (plus de 30 jours)");
                deleteSessionFile();
                return false;
            }

            // Récupérer l'utilisateur depuis la base de données
            Utilisateur user = utilisateurDAO.findByEmail(userEmail);

            if (user == null) {
                System.out.println("⚠️ Utilisateur introuvable");
                deleteSessionFile();
                return false;
            }

            if (!user.isActivated()) {
                System.out.println("⚠️ Compte désactivé");
                deleteSessionFile();
                return false;
            }

            // ✅ SESSION VALIDE - RESTAURER
            this.currentUser = user;
            System.out.println("✅ Session restaurée : " + user.getNomComplet());

            // Mettre à jour la date de dernière connexion
            saveSessionToFile(user);

            return true;

        } catch (Exception e) {
            System.err.println("⚠️ Erreur restauration session : " + e.getMessage());
            deleteSessionFile();
            return false;
        }
    }

    /**
     * 🗑️ Supprime le fichier de session
     * Appelé lors du logout
     */
    private void deleteSessionFile() {
        try {
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists()) {
                sessionFile.delete();
                System.out.println("🗑️ Fichier de session supprimé");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur suppression session : " + e.getMessage());
        }
    }

    /**
     * 🚪 Déconnexion avec suppression de la session
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("🚪 Déconnexion de : " + currentUser.getNomComplet());
        }
        currentUser = null;
        deleteSessionFile();
    }

    // ========== MÉTHODES EXISTANTES ==========

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public String getCurrentUserRole() {
        if (currentUser == null) return "Aucun";
        return currentUser.getClass().getSimpleName();
    }
}