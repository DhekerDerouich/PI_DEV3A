package Finance.model;

import java.util.Date;

public class Depense {
    private Long idDepense;
    private Double montant;
    private String typeDepense;
    private String description;
    private Date dateDepense;

    public Depense() {}

    public Depense(Double montant, String typeDepense, String description,Date dateDepense) {
        this.montant = montant;
        this.typeDepense = typeDepense;
        this.description = description;
        this.dateDepense = dateDepense;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getIdDepense() {
        return idDepense;
    }

    public void setIdDepense(Long idDepense) {
        this.idDepense = idDepense;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getTypeDepense() {
        return typeDepense;
    }

    public void setTypeDepense(String typeDepense) {
        this.typeDepense = typeDepense;
    }

    public Date getDateDepense() {
        return dateDepense;
    }

    public void setDateDepense(Date dateDepense) {
        this.dateDepense = dateDepense;
    }

    @Override
    public String toString() {
        return "Depense{" +
                "idDepense=" + idDepense +
                ", montant=" + montant +
                ", typeDepense='" + typeDepense + '\'' +
                ", description='" + description + '\'' +
                ", dateDepense=" + dateDepense +
                '}';
    }
}