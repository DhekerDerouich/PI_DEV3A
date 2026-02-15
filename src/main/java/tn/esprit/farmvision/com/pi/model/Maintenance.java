package com.pi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.beans.property.*;


public class Maintenance {
    private int id;
    private int equipementId;
    private String typeMaintenance; // Préventive, Corrective
    private String description;
    private LocalDate dateMaintenance;
    private double cout;
    private String statut; // Planifiée, Réalisée
    private LocalDateTime createdAt;
    private IntegerProperty idProperty = new SimpleIntegerProperty();
    private IntegerProperty equipementIdProperty = new SimpleIntegerProperty();
    private StringProperty typeMaintenanceProperty = new SimpleStringProperty();
    private StringProperty descriptionProperty = new SimpleStringProperty();
    private DoubleProperty coutProperty = new SimpleDoubleProperty();
    private StringProperty statutProperty = new SimpleStringProperty();

    // Constructeurs
    public Maintenance() {}

    public Maintenance(int equipementId, String typeMaintenance, String description,
                       LocalDate dateMaintenance, double cout, String statut) {
        this.equipementId = equipementId;
        this.typeMaintenance = typeMaintenance;
        this.description = description;
        this.dateMaintenance = dateMaintenance;
        this.cout = cout;
        this.statut = statut;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEquipementId() { return equipementId; }
    public void setEquipementId(int equipementId) { this.equipementId = equipementId; }

    public String getTypeMaintenance() { return typeMaintenance; }
    public void setTypeMaintenance(String typeMaintenance) { this.typeMaintenance = typeMaintenance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateMaintenance() { return dateMaintenance; }
    public void setDateMaintenance(LocalDate dateMaintenance) { this.dateMaintenance = dateMaintenance; }

    public double getCout() { return cout; }
    public void setCout(double cout) { this.cout = cout; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public IntegerProperty idProperty() {
        return new SimpleIntegerProperty(id);
    }

    public IntegerProperty equipementIdProperty() {
        return new SimpleIntegerProperty(equipementId);
    }

    public StringProperty typeMaintenanceProperty() {
        return new SimpleStringProperty(typeMaintenance);
    }

    public StringProperty descriptionProperty() {
        return new SimpleStringProperty(description);
    }

    public DoubleProperty coutProperty() {
        return new SimpleDoubleProperty(cout);
    }

    public StringProperty statutProperty() {
        return new SimpleStringProperty(statut);
    }

    @Override
    public String toString() {
        return String.format("Maintenance{id=%d, equipementId=%d, type='%s', statut='%s'}",
                id, equipementId, typeMaintenance, statut);
    }
}