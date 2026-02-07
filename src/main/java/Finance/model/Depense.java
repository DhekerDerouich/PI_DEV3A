package Finance.model;

import java.util.Date;

public class Depense {
    private Long idDepense;
    private Double montant;
    private String typeDepense;
    private Date dateDepense;

    public Depense() {}

    public Depense(Double montant, String typeDepense, Date dateDepense) {
        this.montant = montant;
        this.typeDepense = typeDepense;
        this.dateDepense = dateDepense;
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
                ", dateDepense=" + dateDepense +
                '}';
    }
}