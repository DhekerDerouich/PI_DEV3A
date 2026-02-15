package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.dao.UtilisateurDAO;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UtilisateurService {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public void register(Utilisateur user) throws Exception {
        if (utilisateurDAO.findByEmail(user.getEmail()) != null) {
            throw new Exception("Cet email est déjà utilisé.");
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        user.setPassword(hashedPassword);
        user.setActivated(false);

        utilisateurDAO.save(user);
    }

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

    public List<Utilisateur> getAll() {
        return utilisateurDAO.getAll();
    }


    public boolean delete(int id) {
        try {
            utilisateurDAO.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validerUtilisateur(int id) {
        try {
            utilisateurDAO.valider(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void update(Utilisateur user) {
        utilisateurDAO.update(user);
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Réinitialiser le mot de passe d'un utilisateur
     */
    public boolean resetPassword(int userId, String newPassword) {
        try {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            utilisateurDAO.resetPassword(userId, hashedPassword);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}