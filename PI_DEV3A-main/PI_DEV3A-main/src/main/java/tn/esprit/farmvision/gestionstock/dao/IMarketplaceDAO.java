package tn.esprit.farmvision.gestionstock.dao;

import tn.esprit.farmvision.gestionstock.model.Marketplace;
import java.util.List;

public interface IMarketplaceDAO {
    void ajouterMarketplace(Marketplace marketplace);
    void modifierMarketplace(Marketplace marketplace);
    void supprimerMarketplace(int idMarketplace);
    Marketplace getMarketplaceById(int idMarketplace);
    List<Marketplace> getAllMarketplaces();
    List<Marketplace> getMarketplacesByUtilisateur(int idUtilisateur);
    List<Marketplace> getMarketplacesDisponibles();
    List<Marketplace> searchMarketplace(String keyword);
    void changerStatut(int idMarketplace, String nouveauStatut);
    void vendreProduit(int idMarketplace, double quantite);
}