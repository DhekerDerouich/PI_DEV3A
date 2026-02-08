package tn.esprit.farmvision.gestionParcelleEtCulture.model;

public class Parcelle {

    private int idParcelle;
    private float surface;
    private String localisation;

    public Parcelle() {}

    public Parcelle(int idParcelle, float surface, String localisation) {
        this.idParcelle = idParcelle;
        this.surface = surface;
        this.localisation = localisation;
    }

    public int getIdParcelle() {
        return idParcelle;
    }

    public void setIdParcelle(int idParcelle) {
        this.idParcelle = idParcelle;
    }

    public float getSurface() {
        return surface;
    }

    public void setSurface(float surface) {
        this.surface = surface;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
}
