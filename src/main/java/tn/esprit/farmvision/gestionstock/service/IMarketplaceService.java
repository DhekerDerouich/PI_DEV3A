package tn.esprit.farmvision.gestionstock.service;

import tn.esprit.farmvision.gestionstock.model.Marketplace;
import java.util.List;

public interface IMarketplaceService {
    void ajouterMarketplace(Marketplace marketplace);
    void modifierMarketplace(Marketplace marketplace);
    void supprimerMarketplace(int idMarketplace);
    Marketplace getMarketplaceById(int idMarketplace);
    List<Marketplace> getAllMarketplaces();
    List<Marketplace> getMarketplacesByUtilisateur(int idUtilisateur);
    List<Marketplace> getMarketplacesDisponibles();
    List<Marketplace> rechercherProduits(String keyword);
    void acheterProduit(int idMarketplace, double quantite);
    void changerStatut(int idMarketplace, String nouveauStatut);
}