package tn.esprit.farmvision.gestionuser.model;

/**
 * Représente un Agriculteur utilisateur de FarmVision.
 */
public class Agriculteur extends Utilisateur {

    private String telephone;
    private String adresse;

    public Agriculteur() {
        super();
    }

    public Agriculteur(String nom, String prenom, String email, String password,
                       String telephone, String adresse) {
        super(nom, prenom, email, password);
        this.telephone = telephone;
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return "Agriculteur{" +
                super.toString() +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                '}';
    }
}