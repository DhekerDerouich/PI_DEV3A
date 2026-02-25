package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.dao.UtilisateurDAO;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.util.GoogleAuthUtil.GoogleUserInfo;
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

        if (user.getPassword() == null) {
            throw new Exception("Ce compte utilise Google. Connectez-vous avec Google.");
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
        // Vérifier si le mot de passe a changé
        Utilisateur existingUser = null;
        try {
            existingUser = utilisateurDAO.findByEmail(user.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (existingUser != null && !user.getPassword().equals(existingUser.getPassword())) {
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            user.setPassword(hashedPassword);
        }

        utilisateurDAO.update(user);
    }

    // ✅ NOUVELLE MÉTHODE: Mise à jour sans changer le mot de passe
    public void updateWithoutPassword(Utilisateur user) {
        utilisateurDAO.updateWithoutPassword(user);
    }

    // ✅ NOUVELLE MÉTHODE: Mise à jour du mot de passe uniquement
    public void updatePassword(int userId, String newPassword) {
        try {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            utilisateurDAO.resetPassword(userId, hashedPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    // ✅ NOUVELLE MÉTHODE: Trouver par email
    public Utilisateur findByEmail(String email) {
        try {
            return utilisateurDAO.findByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Utilisateur loginWithGoogle(GoogleUserInfo info) throws Exception {
        if (info == null || info.email == null) {
            throw new Exception("Informations Google invalides");
        }

        Utilisateur user = utilisateurDAO.findByEmail(info.email);

        if (user == null) {
            System.out.println("📝 Création automatique du compte pour : " + info.email);

            user = new Agriculteur();
            user.setEmail(info.email);
            user.setNom(info.getNom().isEmpty() ? "Google User" : info.getNom());
            user.setPrenom(info.getPrenom().isEmpty() ? "Google" : info.getPrenom());
            user.setPassword(null);
            user.setActivated(false);

            utilisateurDAO.save(user);

            throw new Exception(
                    "✅ Compte Google créé avec succès !\n\n" +
                            "Votre compte est en attente de validation par un administrateur.\n" +
                            "Vous recevrez un email une fois activé."
            );
        }

        System.out.println("✅ Utilisateur Google existant : " + user.getNomComplet());

        if (!user.isActivated()) {
            throw new Exception(
                    "⏳ Votre compte est en attente de validation.\n\n" +
                            "Un administrateur doit d'abord activer votre compte."
            );
        }

        return user;
    }
}