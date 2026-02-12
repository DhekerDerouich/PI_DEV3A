package tn.esprit.farmvision.gestionstock.service;

import tn.esprit.farmvision.gestionstock.model.Stock;
import java.util.List;

public interface IStockService {
    void ajouterStock(Stock stock);
    void modifierStock(Stock stock);
    void supprimerStock(int idStock);
    Stock getStockById(int idStock);
    List<Stock> getAllStocks();
    List<Stock> getStocksByUtilisateur(int idUtilisateur);
    double getQuantiteDisponible(int idStock);
    void mettreAJourQuantite(int idStock, double quantiteAjoutee);
}