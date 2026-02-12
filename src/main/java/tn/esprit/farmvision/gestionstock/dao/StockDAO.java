package tn.esprit.farmvision.gestionstock.dao;

import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO implements IStockDAO {
    private Connection connection;

    public StockDAO() {
        System.out.println("🔄 [StockDAO] Initialisation...");
        this.connection = DatabaseConnection.getInstance().getConnection();
        System.out.println("✅ [StockDAO] Initialisé");
    }

    @Override
    public void ajouterStock(Stock stock) {
        String sql = "INSERT INTO stock (id_utilisateur, nom_produit, type_produit, quantite, unite, date_entree, date_expiration, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        System.out.println("\n🔵 [StockDAO.ajouterStock] Début ajout stock");
        System.out.println("   Produit: " + stock.getNomProduit());
        System.out.println("   Type: " + stock.getTypeProduit());
        System.out.println("   Quantité: " + stock.getQuantite() + " " + stock.getUnite());

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, stock.getIdUtilisateur());
            stmt.setString(2, stock.getNomProduit());
            stmt.setString(3, stock.getTypeProduit());
            stmt.setDouble(4, stock.getQuantite());
            stmt.setString(5, stock.getUnite());
            stmt.setDate(6, Date.valueOf(stock.getDateEntree()));
            stmt.setDate(7, stock.getDateExpiration() != null ? Date.valueOf(stock.getDateExpiration()) : null);
            stmt.setString(8, stock.getStatut());

            int affectedRows = stmt.executeUpdate();
            System.out.println("   ✅ Lignes affectées: " + affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stock.setIdStock(generatedKeys.getInt(1));
                        System.out.println("   ✅ ID généré: " + stock.getIdStock());
                    }
                }
            }
            System.out.println("✅ [StockDAO.ajouterStock] Ajout réussi\n");

        } catch (SQLException e) {
            System.err.println("❌ [StockDAO.ajouterStock] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifierStock(Stock stock) {
        String sql = "UPDATE stock SET nom_produit = ?, type_produit = ?, quantite = ?, unite = ?, date_expiration = ?, statut = ? WHERE id_stock = ?";

        System.out.println("\n🔵 [StockDAO.modifierStock] Début modification stock ID: " + stock.getIdStock());

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, stock.getNomProduit());
            stmt.setString(2, stock.getTypeProduit());
            stmt.setDouble(3, stock.getQuantite());
            stmt.setString(4, stock.getUnite());
            stmt.setDate(5, stock.getDateExpiration() != null ? Date.valueOf(stock.getDateExpiration()) : null);
            stmt.setString(6, stock.getStatut());
            stmt.setInt(7, stock.getIdStock());

            int affectedRows = stmt.executeUpdate();
            System.out.println("   ✅ Lignes affectées: " + affectedRows);
            System.out.println("✅ [StockDAO.modifierStock] Modification réussie\n");

        } catch (SQLException e) {
            System.err.println("❌ [StockDAO.modifierStock] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimerStock(int idStock) {
        String sql = "DELETE FROM stock WHERE id_stock = ?";

        System.out.println("\n🔵 [StockDAO.supprimerStock] Début suppression stock ID: " + idStock);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idStock);
            int affectedRows = stmt.executeUpdate();
            System.out.println("   ✅ Lignes affectées: " + affectedRows);
            System.out.println("✅ [StockDAO.supprimerStock] Suppression réussie\n");

        } catch (SQLException e) {
            System.err.println("❌ [StockDAO.supprimerStock] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Stock getStockById(int idStock) {
        String sql = "SELECT * FROM stock WHERE id_stock = ?";
        Stock stock = null;

        System.out.println("\n🔵 [StockDAO.getStockById] Recherche stock ID: " + idStock);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idStock);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stock = new Stock();
                stock.setIdStock(rs.getInt("id_stock"));
                stock.setIdUtilisateur(rs.getInt("id_utilisateur"));
                stock.setNomProduit(rs.getString("nom_produit"));
                stock.setTypeProduit(rs.getString("type_produit"));
                stock.setQuantite(rs.getDouble("quantite"));
                stock.setUnite(rs.getString("unite"));
                stock.setDateEntree(rs.getDate("date_entree") != null ? rs.getDate("date_entree").toLocalDate() : null);
                stock.setDateExpiration(rs.getDate("date_expiration") != null ? rs.getDate("date_expiration").toLocalDate() : null);
                stock.setStatut(rs.getString("statut"));
                System.out.println("   ✅ Stock trouvé: " + stock.getNomProduit());
            } else {
                System.out.println("   ❌ Stock non trouvé");
            }

        } catch (SQLException e) {
            System.err.println("❌ [StockDAO.getStockById] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return stock;
    }

    @Override
    public List<Stock> getAllStocks() {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stock ORDER BY id_stock DESC";

        System.out.println("\n🔵 [StockDAO.getAllStocks] Récupération de tous les stocks");

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Stock stock = new Stock();
                stock.setIdStock(rs.getInt("id_stock"));
                stock.setIdUtilisateur(rs.getInt("id_utilisateur"));
                stock.setNomProduit(rs.getString("nom_produit"));
                stock.setTypeProduit(rs.getString("type_produit"));
                stock.setQuantite(rs.getDouble("quantite"));
                stock.setUnite(rs.getString("unite"));
                stock.setDateEntree(rs.getDate("date_entree") != null ? rs.getDate("date_entree").toLocalDate() : null);
                stock.setDateExpiration(rs.getDate("date_expiration") != null ? rs.getDate("date_expiration").toLocalDate() : null);
                stock.setStatut(rs.getString("statut"));

                stocks.add(stock);
                System.out.println("   📦 Stock chargé: ID=" + stock.getIdStock() +
                        ", Produit=" + stock.getNomProduit() +
                        ", Type=" + stock.getTypeProduit() +
                        ", Quantité=" + stock.getQuantite() + " " + stock.getUnite() +
                        ", Statut=" + stock.getStatut());
            }

            System.out.println("✅ [StockDAO.getAllStocks] Total stocks: " + stocks.size() + "\n");

        } catch (SQLException e) {
            System.err.println("❌ [StockDAO.getAllStocks] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return stocks;
    }

    @Override
    public List<Stock> getStocksByUtilisateur(int idUtilisateur) {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stock WHERE id_utilisateur = ? ORDER BY date_entree DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Stock stock = new Stock();
                stock.setIdStock(rs.getInt("id_stock"));
                stock.setIdUtilisateur(rs.getInt("id_utilisateur"));
                stock.setNomProduit(rs.getString("nom_produit"));
                stock.setTypeProduit(rs.getString("type_produit"));
                stock.setQuantite(rs.getDouble("quantite"));
                stock.setUnite(rs.getString("unite"));
                stock.setDateEntree(rs.getDate("date_entree") != null ? rs.getDate("date_entree").toLocalDate() : null);
                stock.setDateExpiration(rs.getDate("date_expiration") != null ? rs.getDate("date_expiration").toLocalDate() : null);
                stock.setStatut(rs.getString("statut"));
                stocks.add(stock);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }

    @Override
    public double getQuantiteDisponible(int idStock) {
        String sql = "SELECT quantite FROM stock WHERE id_stock = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idStock);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("quantite");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void mettreAJourQuantite(int idStock, double nouvelleQuantite) {
        String sql = "UPDATE stock SET quantite = ? WHERE id_stock = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, nouvelleQuantite);
            stmt.setInt(2, idStock);
            stmt.executeUpdate();
            System.out.println("✅ [StockDAO.mettreAJourQuantite] Quantité mise à jour: " + nouvelleQuantite);

        } catch (SQLException e) {
            System.err.println("❌ [StockDAO.mettreAJourQuantite] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}