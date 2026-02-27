package tn.esprit.farmvision.com.pi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.beans.property.*;

public class Equipement {
    private int id;
    private String nom;
    private String type;
    private String etat; // Fonctionnel, En panne, Maintenance
    private LocalDate dateAchat;
    private int dureeVieEstimee;
    private Integer parcelleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private IntegerProperty idProperty = new SimpleIntegerProperty();
    private StringProperty nomProperty = new SimpleStringProperty();
    private StringProperty typeProperty = new SimpleStringProperty();
    private StringProperty etatProperty = new SimpleStringProperty();
    private IntegerProperty dureeVieEstimeeProperty = new SimpleIntegerProperty();

    // Constructeurs
    public Equipement() {}

    public Equipement(String nom, String type, String etat, LocalDate dateAchat,
                      int dureeVieEstimee, Integer parcelleId) {
        this.nom = nom;
        this.type = type;
        this.etat = etat;
        this.dateAchat = dateAchat;
        this.dureeVieEstimee = dureeVieEstimee;
        this.parcelleId = parcelleId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public LocalDate getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDate dateAchat) { this.dateAchat = dateAchat; }

    public int getDureeVieEstimee() { return dureeVieEstimee; }
    public void setDureeVieEstimee(int dureeVieEstimee) { this.dureeVieEstimee = dureeVieEstimee; }

    public Integer getParcelleId() { return parcelleId; }
    public void setParcelleId(Integer parcelleId) { this.parcelleId = parcelleId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public IntegerProperty idProperty() {
        return new SimpleIntegerProperty(id);
    }

    public StringProperty nomProperty() {
        return new SimpleStringProperty(nom);
    }

    public StringProperty typeProperty() {
        return new SimpleStringProperty(type);
    }

    public StringProperty etatProperty() {
        return new SimpleStringProperty(etat);
    }

    public IntegerProperty dureeVieEstimeeProperty() {
        return new SimpleIntegerProperty(dureeVieEstimee);
    }

    @Override
    public String toString() {
        return String.format("Equipement{id=%d, nom='%s', type='%s', etat='%s'}",
                id, nom, type, etat);
    }
}