package tn.esprit.farmvision.gestionParcelleEtCulture.model;
import java.sql.Date;

public class Culture {

    private int idCulture;
    private String nomCulture;
    private String typeCulture;
    private Date dateSemis;
    private Date dateRecolte;

    public Culture() {}

    public Culture(int idCulture, String nomCulture, String typeCulture,
                   Date dateSemis, Date dateRecolte) {
        this.idCulture = idCulture;
        this.nomCulture = nomCulture;
        this.typeCulture = typeCulture;
        this.dateSemis = dateSemis;
        this.dateRecolte = dateRecolte;
    }

    // getters & setters
    public int getIdCulture() { return idCulture; }
    public void setIdCulture(int idCulture) { this.idCulture = idCulture; }

    public String getNomCulture() { return nomCulture; }
    public void setNomCulture(String nomCulture) { this.nomCulture = nomCulture; }

    public String getTypeCulture() { return typeCulture; }
    public void setTypeCulture(String typeCulture) { this.typeCulture = typeCulture; }

    public Date getDateSemis() { return dateSemis; }
    public void setDateSemis(Date dateSemis) { this.dateSemis = dateSemis; }

    public Date getDateRecolte() { return dateRecolte; }
    public void setDateRecolte(Date dateRecolte) { this.dateRecolte = dateRecolte; }
}