package tn.esprit.farmvision;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.dao.UtilisateurDAO;

import java.io.*;
import java.util.Properties;

public class SessionManager {

    private static SessionManager instance;
    private Utilisateur currentUser;
    private static final String SESSION_FILE = "farmvision_session.properties";
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // ✅ NOUVEAU: Clés pour sauvegarder l'état de la fenêtre
    private static final String KEY_WINDOW_WIDTH = "window.width";
    private static final String KEY_WINDOW_HEIGHT = "window.height";
    private static final String KEY_WINDOW_MAXIMIZED = "window.maximized";
    private static final String KEY_WINDOW_FULLSCREEN = "window.fullscreen";
    private static final String KEY_WINDOW_X = "window.x";
    private static final String KEY_WINDOW_Y = "window.y";

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ========== GESTION UTILISATEUR ==========

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        System.out.println("✅ Session ouverte pour : " + user.getNomComplet() + " (" + user.getClass().getSimpleName() + ")");
        saveSessionToFile(user);
    }

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

    public boolean isAdmin() {
        return currentUser != null && currentUser.getClass().getSimpleName().equals("Administrateur");
    }

    public boolean isResponsable() {
        return currentUser != null && currentUser.getClass().getSimpleName().equals("ResponsableExploitation");
    }

    public boolean isAgriculteur() {
        return currentUser != null && currentUser.getClass().getSimpleName().equals("Agriculteur");
    }

    // ========== SAUVEGARDE SESSION UTILISATEUR ==========

    private void saveSessionToFile(Utilisateur user) {
        try {
            Properties props = new Properties();
            props.setProperty("userId", String.valueOf(user.getId()));
            props.setProperty("userEmail", user.getEmail());
            props.setProperty("lastLogin", String.valueOf(System.currentTimeMillis()));

            File sessionFile = new File(SESSION_FILE);
            FileOutputStream fos = new FileOutputStream(sessionFile);
            props.store(fos, "FarmVision Session - Ne pas partager ce fichier");
            fos.close();

            System.out.println("💾 Session utilisateur sauvegardée");

        } catch (IOException e) {
            System.err.println("⚠️ Erreur sauvegarde session : " + e.getMessage());
        }
    }

    public boolean restoreSessionFromFile() {
        try {
            File sessionFile = new File(SESSION_FILE);

            if (!sessionFile.exists()) {
                System.out.println("ℹ️ Aucune session sauvegardée");
                return false;
            }

            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(sessionFile);
            props.load(fis);
            fis.close();

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

            long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() - lastLogin > thirtyDaysInMillis) {
                System.out.println("⚠️ Session expirée (plus de 30 jours)");
                deleteSessionFile();
                return false;
            }

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

            this.currentUser = user;
            System.out.println("✅ Session restaurée : " + user.getNomComplet());
            saveSessionToFile(user);

            return true;

        } catch (Exception e) {
            System.err.println("⚠️ Erreur restauration session : " + e.getMessage());
            deleteSessionFile();
            return false;
        }
    }

    // ========== ✅ NOUVEAU: SAUVEGARDE ÉTAT FENÊTRE ==========

    /**
     * 💾 Sauvegarde l'état de la fenêtre
     */
    public void saveWindowState(double width, double height, boolean maximized, boolean fullscreen, double x, double y) {
        try {
            Properties props = new Properties();

            // Charger d'abord le fichier existant pour ne pas perdre les infos utilisateur
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists()) {
                FileInputStream fis = new FileInputStream(sessionFile);
                props.load(fis);
                fis.close();
            }

            // Ajouter/Mettre à jour les infos de fenêtre
            props.setProperty(KEY_WINDOW_WIDTH, String.valueOf(width));
            props.setProperty(KEY_WINDOW_HEIGHT, String.valueOf(height));
            props.setProperty(KEY_WINDOW_MAXIMIZED, String.valueOf(maximized));
            props.setProperty(KEY_WINDOW_FULLSCREEN, String.valueOf(fullscreen));
            props.setProperty(KEY_WINDOW_X, String.valueOf(x));
            props.setProperty(KEY_WINDOW_Y, String.valueOf(y));

            FileOutputStream fos = new FileOutputStream(sessionFile);
            props.store(fos, "FarmVision Session - Ne pas partager ce fichier");
            fos.close();

            System.out.println("💾 État fenêtre sauvegardé: " +
                    (fullscreen ? "PLEIN ÉCRAN" : (maximized ? "MAXIMISÉ" : width + "x" + height)));

        } catch (IOException e) {
            System.err.println("⚠️ Erreur sauvegarde état fenêtre : " + e.getMessage());
        }
    }

    /**
     * 🔄 Restaure l'état de la fenêtre
     * @return WindowState ou null si non trouvé
     */
    public WindowState restoreWindowState() {
        try {
            File sessionFile = new File(SESSION_FILE);
            if (!sessionFile.exists()) {
                return null;
            }

            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(sessionFile);
            props.load(fis);
            fis.close();

            // Vérifier si les clés existent
            if (!props.containsKey(KEY_WINDOW_WIDTH)) {
                return null;
            }

            WindowState state = new WindowState();
            state.width = Double.parseDouble(props.getProperty(KEY_WINDOW_WIDTH, "1300"));
            state.height = Double.parseDouble(props.getProperty(KEY_WINDOW_HEIGHT, "800"));
            state.maximized = Boolean.parseBoolean(props.getProperty(KEY_WINDOW_MAXIMIZED, "false"));
            state.fullscreen = Boolean.parseBoolean(props.getProperty(KEY_WINDOW_FULLSCREEN, "false"));
            state.x = Double.parseDouble(props.getProperty(KEY_WINDOW_X, "100"));
            state.y = Double.parseDouble(props.getProperty(KEY_WINDOW_Y, "100"));

            System.out.println("🔄 État fenêtre restauré: " +
                    (state.fullscreen ? "PLEIN ÉCRAN" : (state.maximized ? "MAXIMISÉ" : state.width + "x" + state.height)));

            return state;

        } catch (Exception e) {
            System.err.println("⚠️ Erreur restauration état fenêtre : " + e.getMessage());
            return null;
        }
    }

    /**
     * 🗑️ Supprime le fichier de session
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
     * 🚪 Déconnexion - Garde l'état de la fenêtre mais supprime l'utilisateur
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("🚪 Déconnexion de : " + currentUser.getNomComplet());
        }
        currentUser = null;
        // ✅ NE PAS supprimer le fichier, seulement l'utilisateur
        // On garde les préférences de fenêtre
        try {
            Properties props = new Properties();
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists()) {
                FileInputStream fis = new FileInputStream(sessionFile);
                props.load(fis);
                fis.close();

                // Supprimer les infos utilisateur mais garder les infos fenêtre
                props.remove("userId");
                props.remove("userEmail");
                props.remove("lastLogin");

                FileOutputStream fos = new FileOutputStream(sessionFile);
                props.store(fos, "FarmVision Session - Préférences fenêtre uniquement");
                fos.close();
                System.out.println("💾 Préférences fenêtre conservées");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors du logout : " + e.getMessage());
        }
    }

    // ========== CLASSE INTERNE POUR L'ÉTAT DE LA FENÊTRE ==========

    public static class WindowState {
        public double width;
        public double height;
        public boolean maximized;
        public boolean fullscreen;
        public double x;
        public double y;
    }
}