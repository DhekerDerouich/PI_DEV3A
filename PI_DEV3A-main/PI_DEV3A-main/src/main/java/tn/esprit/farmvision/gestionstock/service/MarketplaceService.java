package tn.esprit.farmvision.gestionstock.service;

import tn.esprit.farmvision.gestionstock.dao.MarketplaceDAO;
import tn.esprit.farmvision.gestionstock.dao.StockDAO;
import tn.esprit.farmvision.gestionstock.model.Marketplace;
import java.util.List;

public class MarketplaceService implements IMarketplaceService {
    private MarketplaceDAO marketplaceDAO;
    private StockDAO stockDAO;

    public MarketplaceService() {
        System.out.println("🔄 [MarketplaceService] Initialisation...");
        this.marketplaceDAO = new MarketplaceDAO();
        this.stockDAO = new StockDAO();
        System.out.println("✅ [MarketplaceService] Initialisé\n");
    }

    @Override
    public void ajouterMarketplace(Marketplace marketplace) {
        System.out.println("\n🟡 [MarketplaceService] Ajout annonce pour stock ID: " + marketplace.getIdStock());

        double quantiteStock = stockDAO.getQuantiteDisponible(marketplace.getIdStock());
        if (quantiteStock < marketplace.getQuantiteEnVente()) {
            throw new IllegalArgumentException("❌ Quantité insuffisante en stock. Disponible: " + quantiteStock);
        }
        if (marketplace.getPrixUnitaire() <= 0) {
            throw new IllegalArgumentException("❌ Le prix doit être positif");
        }

        marketplaceDAO.ajouterMarketplace(marketplace);
        System.out.println("✅ [MarketplaceService] Annonce ajoutée avec ID: " + marketplace.getIdMarketplace() + "\n");
    }

    @Override
    public void modifierMarketplace(Marketplace marketplace) {
        System.out.println("\n🟡 [MarketplaceService] Modification annonce ID: " + marketplace.getIdMarketplace());

        Marketplace existing = marketplaceDAO.getMarketplaceById(marketplace.getIdMarketplace());
        if (existing == null) {
            throw new IllegalArgumentException("❌ Annonce non trouvée");
        }

        marketplaceDAO.modifierMarketplace(marketplace);
        System.out.println("✅ [MarketplaceService] Annonce modifiée\n");
    }

    @Override
    public void supprimerMarketplace(int idMarketplace) {
        System.out.println("\n🟡 [MarketplaceService] Suppression annonce ID: " + idMarketplace);
        marketplaceDAO.supprimerMarketplace(idMarketplace);
        System.out.println("✅ [MarketplaceService] Annonce supprimée\n");
    }

    @Override
    public Marketplace getMarketplaceById(int idMarketplace) {
        return marketplaceDAO.getMarketplaceById(idMarketplace);
    }

    @Override
    public List<Marketplace> getAllMarketplaces() {
        System.out.println("\n🟡 [MarketplaceService] Récupération de toutes les annonces");
        List<Marketplace> marketplaces = marketplaceDAO.getAllMarketplaces();
        System.out.println("✅ [MarketplaceService] " + marketplaces.size() + " annonces trouvées\n");
        return marketplaces;
    }

    @Override
    public List<Marketplace> getMarketplacesByUtilisateur(int idUtilisateur) {
        return marketplaceDAO.getMarketplacesByUtilisateur(idUtilisateur);
    }

    @Override
    public List<Marketplace> getMarketplacesDisponibles() {
        System.out.println("\n🟡 [MarketplaceService] Récupération des annonces disponibles");
        List<Marketplace> marketplaces = marketplaceDAO.getMarketplacesDisponibles();
        System.out.println("✅ [MarketplaceService] " + marketplaces.size() + " annonces disponibles\n");
        return marketplaces;
    }

    @Override
    public List<Marketplace> rechercherProduits(String keyword) {
        System.out.println("\n🟡 [MarketplaceService] Recherche de produits: " + keyword);
        List<Marketplace> marketplaces = marketplaceDAO.searchMarketplace(keyword);
        System.out.println("✅ [MarketplaceService] " + marketplaces.size() + " résultats trouvés\n");
        return marketplaces;
    }

    @Override
    public void acheterProduit(int idMarketplace, double quantite) {
        System.out.println("\n🟡 [MarketplaceService] Achat de produit ID: " + idMarketplace);

        Marketplace marketplace = marketplaceDAO.getMarketplaceById(idMarketplace);
        if (marketplace == null) {
            throw new IllegalArgumentException("❌ Annonce non trouvée");
        }

        if (!"En vente".equals(marketplace.getStatut())) {
            throw new IllegalArgumentException("❌ Ce produit n'est plus disponible à la vente");
        }

        if (quantite > marketplace.getQuantiteEnVente()) {
            throw new IllegalArgumentException("❌ Quantité demandée supérieure à la quantité disponible");
        }

        // Mettre à jour la quantité en vente
        double nouvelleQuantite = marketplace.getQuantiteEnVente() - quantite;
        if (nouvelleQuantite > 0) {
            marketplace.setQuantiteEnVente(nouvelleQuantite);
            marketplaceDAO.modifierMarketplace(marketplace);
        } else {
            marketplaceDAO.changerStatut(idMarketplace, "Vendu");
        }

        // Mettre à jour le stock
        double quantiteStock = stockDAO.getQuantiteDisponible(marketplace.getIdStock());
        stockDAO.mettreAJourQuantite(marketplace.getIdStock(), quantiteStock - quantite);

        System.out.println("✅ [MarketplaceService] Achat effectué: " + quantite + " unités\n");
    }

    @Override
    public void changerStatut(int idMarketplace, String nouveauStatut) {
        marketplaceDAO.changerStatut(idMarketplace, nouveauStatut);
    }
}