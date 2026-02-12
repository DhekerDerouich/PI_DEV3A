package tn.esprit.farmvision.gestionstock.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Stock {
    private final IntegerProperty idStock;
    private final IntegerProperty idUtilisateur;
    private final StringProperty nomProduit;
    private final StringProperty typeProduit;
    private final DoubleProperty quantite;
    private final StringProperty unite;
    private final ObjectProperty<LocalDate> dateEntree;
    private final ObjectProperty<LocalDate> dateExpiration;
    private final StringProperty statut;

    public Stock() {
        this.idStock = new SimpleIntegerProperty();
        this.idUtilisateur = new SimpleIntegerProperty();
        this.nomProduit = new SimpleStringProperty();
        this.typeProduit = new SimpleStringProperty();
        this.quantite = new SimpleDoubleProperty();
        this.unite = new SimpleStringProperty();
        this.dateEntree = new SimpleObjectProperty<>();
        this.dateExpiration = new SimpleObjectProperty<>();
        this.statut = new SimpleStringProperty();
    }

    public Stock(int idUtilisateur, String nomProduit, String typeProduit,
                 double quantite, String unite, LocalDate dateExpiration) {
        this();
        this.idUtilisateur.set(idUtilisateur);
        this.nomProduit.set(nomProduit);
        this.typeProduit.set(typeProduit);
        this.quantite.set(quantite);
        this.unite.set(unite);
        this.dateEntree.set(LocalDate.now());
        this.dateExpiration.set(dateExpiration);
        this.statut.set("Disponible");
    }

    public IntegerProperty idStockProperty() { return idStock; }
    public IntegerProperty idUtilisateurProperty() { return idUtilisateur; }
    public StringProperty nomProduitProperty() { return nomProduit; }
    public StringProperty typeProduitProperty() { return typeProduit; }
    public DoubleProperty quantiteProperty() { return quantite; }
    public StringProperty uniteProperty() { return unite; }
    public ObjectProperty<LocalDate> dateEntreeProperty() { return dateEntree; }
    public ObjectProperty<LocalDate> dateExpirationProperty() { return dateExpiration; }
    public StringProperty statutProperty() { return statut; }

    public int getIdStock() { return idStock.get(); }
    public void setIdStock(int idStock) { this.idStock.set(idStock); }
    public int getIdUtilisateur() { return idUtilisateur.get(); }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur.set(idUtilisateur); }
    public String getNomProduit() { return nomProduit.get(); }
    public void setNomProduit(String nomProduit) { this.nomProduit.set(nomProduit); }
    public String getTypeProduit() { return typeProduit.get(); }
    public void setTypeProduit(String typeProduit) { this.typeProduit.set(typeProduit); }
    public double getQuantite() { return quantite.get(); }
    public void setQuantite(double quantite) { this.quantite.set(quantite); }
    public String getUnite() { return unite.get(); }
    public void setUnite(String unite) { this.unite.set(unite); }
    public LocalDate getDateEntree() { return dateEntree.get(); }
    public void setDateEntree(LocalDate dateEntree) { this.dateEntree.set(dateEntree); }
    public LocalDate getDateExpiration() { return dateExpiration.get(); }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration.set(dateExpiration); }
    public String getStatut() { return statut.get(); }
    public void setStatut(String statut) { this.statut.set(statut); }

    @Override
    public String toString() {
        return nomProduit.get() + " (" + quantite.get() + " " + unite.get() + ")";
    }
}