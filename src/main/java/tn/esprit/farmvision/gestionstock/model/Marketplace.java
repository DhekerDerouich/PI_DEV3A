package tn.esprit.farmvision.gestionstock.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Marketplace {
    private final IntegerProperty idMarketplace;
    private final IntegerProperty idStock;
    private final DoubleProperty prixUnitaire;
    private final DoubleProperty quantiteEnVente;
    private final StringProperty statut;
    private final ObjectProperty<LocalDateTime> datePublication;
    private final StringProperty description;
    private final StringProperty nomProduit;
    private final StringProperty nomVendeur;
    private final StringProperty categorie;

    public Marketplace() {
        this.idMarketplace = new SimpleIntegerProperty();
        this.idStock = new SimpleIntegerProperty();
        this.prixUnitaire = new SimpleDoubleProperty();
        this.quantiteEnVente = new SimpleDoubleProperty();
        this.statut = new SimpleStringProperty();
        this.datePublication = new SimpleObjectProperty<>();
        this.description = new SimpleStringProperty();
        this.nomProduit = new SimpleStringProperty();
        this.nomVendeur = new SimpleStringProperty();
        this.categorie = new SimpleStringProperty();
    }

    public Marketplace(int idStock, double prixUnitaire, double quantiteEnVente, String description) {
        this();
        this.idStock.set(idStock);
        this.prixUnitaire.set(prixUnitaire);
        this.quantiteEnVente.set(quantiteEnVente);
        this.statut.set("En vente");
        this.datePublication.set(LocalDateTime.now());
        this.description.set(description);
    }

    // Property getters
    public IntegerProperty idMarketplaceProperty() { return idMarketplace; }
    public IntegerProperty idStockProperty() { return idStock; }
    public DoubleProperty prixUnitaireProperty() { return prixUnitaire; }
    public DoubleProperty quantiteEnVenteProperty() { return quantiteEnVente; }
    public StringProperty statutProperty() { return statut; }
    public ObjectProperty<LocalDateTime> datePublicationProperty() { return datePublication; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty nomProduitProperty() { return nomProduit; }
    public StringProperty nomVendeurProperty() { return nomVendeur; }
    public StringProperty categorieProperty() { return categorie; }

    // Getters et Setters
    public int getIdMarketplace() { return idMarketplace.get(); }
    public void setIdMarketplace(int idMarketplace) { this.idMarketplace.set(idMarketplace); }

    public int getIdStock() { return idStock.get(); }
    public void setIdStock(int idStock) { this.idStock.set(idStock); }

    public double getPrixUnitaire() { return prixUnitaire.get(); }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire.set(prixUnitaire); }

    public double getQuantiteEnVente() { return quantiteEnVente.get(); }
    public void setQuantiteEnVente(double quantiteEnVente) { this.quantiteEnVente.set(quantiteEnVente); }

    public String getStatut() { return statut.get(); }
    public void setStatut(String statut) { this.statut.set(statut); }

    public LocalDateTime getDatePublication() { return datePublication.get(); }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication.set(datePublication); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getNomProduit() { return nomProduit.get(); }
    public void setNomProduit(String nomProduit) { this.nomProduit.set(nomProduit); }

    public String getNomVendeur() { return nomVendeur.get(); }
    public void setNomVendeur(String nomVendeur) { this.nomVendeur.set(nomVendeur); }

    public String getCategorie() { return categorie.get(); }
    public void setCategorie(String categorie) { this.categorie.set(categorie); }

    public String getDatePublicationFormatted() {
        if (datePublication.get() == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return datePublication.get().format(formatter);
    }

    @Override
    public String toString() {
        return nomProduit.get() + " - " + prixUnitaire.get() + " DT (" + quantiteEnVente.get() + " unités)";
    }
}