package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.dao.UtilisateurDAO;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UtilisateurService {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // ========================
    //  INSCRIPTION (REGISTER)
    // ========================
    public void register(Utilisateur user) throws Exception {
        // 1. Vérifier si l'email existe déjà
        if (utilisateurDAO.findByEmail(user.getEmail()) != null) {
            throw new Exception("Cet email est déjà utilisé.");
        }

        // 2. Hasher le mot de passe
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        user.setPassword(hashedPassword);

        // 3. Par défaut : en attente
        user.setActivated(false);

        // 4. Sauvegarder
        utilisateurDAO.save(user);
    }

    // ========================
    //     CONNEXION (LOGIN)
    // ========================
    public Utilisateur login(String email, String password) throws Exception {
        Utilisateur user = utilisateurDAO.findByEmail(email);

        if (user == null) {
            throw new Exception("Email ou mot de passe incorrect.");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new Exception("Email ou mot de passe incorrect.");
        }

        if (!user.isActivated()) {
            throw new Exception("Votre compte est en attente de validation par l'administrateur.");
        }

        return user;
    }

    // ========================
    //        CRUD
    // ========================
    public List<Utilisateur> getAll() {
        return utilisateurDAO.getAll();
    }

    public boolean delete(int id) {
        try {
            utilisateurDAO.delete(id);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur ID " + id + " : " + e.getMessage());
            e.printStackTrace(); // pour debug (tu peux enlever en prod)
            return false;
        }
    }

    public boolean validerUtilisateur(int id) {
        try {
            utilisateurDAO.valider(id);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation de l'utilisateur ID " + id + " : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void update(Utilisateur user) {
        utilisateurDAO.update(user);
    }
}