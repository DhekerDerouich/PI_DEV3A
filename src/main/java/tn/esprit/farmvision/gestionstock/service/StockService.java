package tn.esprit.farmvision.gestionstock.service;

import tn.esprit.farmvision.gestionstock.dao.StockDAO;
import tn.esprit.farmvision.gestionstock.model.Stock;
import java.util.List;

public class StockService implements IStockService {
    private StockDAO stockDAO;

    public StockService() {
        System.out.println("🔄 [StockService] Initialisation...");
        this.stockDAO = new StockDAO();
        System.out.println("✅ [StockService] Initialisé\n");
    }

    @Override
    public void ajouterStock(Stock stock) {
        System.out.println("\n🟡 [StockService] Ajout stock: " + stock.getNomProduit());

        if (stock.getNomProduit() == null || stock.getNomProduit().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit est obligatoire");
        }
        if (stock.getQuantite() < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }

        stockDAO.ajouterStock(stock);
        System.out.println("✅ [StockService] Stock ajouté avec ID: " + stock.getIdStock() + "\n");
    }

    @Override
    public void modifierStock(Stock stock) {
        System.out.println("\n🟡 [StockService] Modification stock ID: " + stock.getIdStock());

        Stock existingStock = stockDAO.getStockById(stock.getIdStock());
        if (existingStock == null) {
            throw new IllegalArgumentException("Stock non trouvé");
        }

        stockDAO.modifierStock(stock);
        System.out.println("✅ [StockService] Stock modifié\n");
    }

    @Override
    public void supprimerStock(int idStock) {
        System.out.println("\n🟡 [StockService] Suppression stock ID: " + idStock);
        stockDAO.supprimerStock(idStock);
        System.out.println("✅ [StockService] Stock supprimé\n");
    }

    @Override
    public Stock getStockById(int idStock) {
        return stockDAO.getStockById(idStock);
    }

    @Override
    public List<Stock> getAllStocks() {
        System.out.println("\n🟡 [StockService] Récupération de tous les stocks");
        List<Stock> stocks = stockDAO.getAllStocks();
        System.out.println("✅ [StockService] " + stocks.size() + " stocks trouvés\n");
        return stocks;
    }

    @Override
    public List<Stock> getStocksByUtilisateur(int idUtilisateur) {
        return stockDAO.getStocksByUtilisateur(idUtilisateur);
    }

    @Override
    public double getQuantiteDisponible(int idStock) {
        return stockDAO.getQuantiteDisponible(idStock);
    }

    @Override
    public void mettreAJourQuantite(int idStock, double quantiteAjoutee) {
        double quantiteActuelle = stockDAO.getQuantiteDisponible(idStock);
        double nouvelleQuantite = quantiteActuelle + quantiteAjoutee;
        if (nouvelleQuantite < 0) {
            throw new IllegalArgumentException("Quantité insuffisante en stock");
        }
        stockDAO.mettreAJourQuantite(idStock, nouvelleQuantite);
    }
}