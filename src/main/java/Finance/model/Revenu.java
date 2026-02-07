package Finance.model;

import java.util.Date;

public class Revenu {
    private Long idRevenu;
    private Double montant;
    private String source;
    private Date dateRevenu;

    public Revenu() {}

    public Revenu(Double montant, String source, Date dateRevenu) {
        this.montant = montant;
        this.source = source;
        this.dateRevenu = dateRevenu;
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
                ", dateRevenu=" + dateRevenu +
                '}';
    }
}