package tn.esprit.farmvision.Finance.model;

import java.util.Date;

public class Revenu {
    private Long idRevenu;
    private Double montant;
    private String source;
    private String description;
    private Date dateRevenu;

    public Revenu() {}

    public Revenu(Double montant, String source,String description, Date dateRevenu) {
        this.montant = montant;
        this.source = source;
        this.description=description;
        this.dateRevenu = dateRevenu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getIdRevenu() {
        return idRevenu;
    }

    public void setIdRevenu(Long idRevenu) {
        this.idRevenu = idRevenu;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getDateRevenu() {
        return dateRevenu;
    }

    public void setDateRevenu(Date dateRevenu) {
        this.dateRevenu = dateRevenu;
    }

    @Override
    public String toString() {
        return "Revenu{" +
                "idRevenu=" + idRevenu +
                ", montant=" + montant +
                ", source='" + source + '\'' +
                ", description='" + description + '\'' +
                ", dateRevenu=" + dateRevenu +
                '}';
    }
}