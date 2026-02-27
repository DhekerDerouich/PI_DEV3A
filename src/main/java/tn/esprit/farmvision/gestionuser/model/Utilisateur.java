package tn.esprit.farmvision.gestionuser.model;

import java.util.Date;

/**
 * Classe abstraite de base pour tous les utilisateurs de FarmVision.
 * Contient les attributs communs à tous les rôles.
 */
public abstract class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Date dateCreation;
    private boolean activated;
    private String remarques;


    // Constructeur par défaut
    public Utilisateur() {
        this.dateCreation = new Date();   // Date actuelle
        this.activated = false;           // Par défaut : en attente de validation
        this.remarques = null;
    }

    // Constructeur principal (sans id)
    public Utilisateur(String nom, String prenom, String email, String password) {
        this();  // Appelle le constructeur par défaut
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }

    // Getters et Setters (encapsulation complète)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
    // Méthode toString pour debug facile (polymorphisme : sera override dans les enfants)
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", activated=" + activated +
                ", dateCreation=" + dateCreation +
                '}';
    }

    // Méthode métier exemple (peut être override si besoin)
    public String getNomComplet() {
        return prenom + " " + nom;
    }
}