package tn.esprit.farmvision.gestionuser.model;

/**
 * Représente un Administrateur de la plateforme.
 */
public class Administrateur extends Utilisateur {

    private String matricule;

    public Administrateur() {
        super();
    }

    public Administrateur(String nom, String prenom, String email, String password, String matricule) {
        super(nom, prenom, email, password);
        this.matricule = matricule;
        this.setActivated(true);  // Les admins sont actifs par défaut
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    @Override
    public String toString() {
        return "Administrateur{" +
                super.toString() +
                ", matricule='" + matricule + '\'' +
                '}';
    }

    // Exemple de méthode spécifique au rôle
    public void validerCompte(Utilisateur user) {
        // Logique métier : on pourra l'implémenter plus tard dans le service
        user.setActivated(true);
    }
}