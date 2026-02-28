package tn.esprit.farmvision.gestionstock.dao;

import tn.esprit.farmvision.gestionstock.model.Marketplace;
import tn.esprit.farmvision.gestionstock.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceDAO implements IMarketplaceDAO {
    private Connection connection;

    public MarketplaceDAO() {
        System.out.println("🔄 [MarketplaceDAO] Initialisation...");
        this.connection = DatabaseConnection.getInstance().getConnection();
        System.out.println("✅ [MarketplaceDAO] Initialisé");
    }

    @Override
    public void ajouterMarketplace(Marketplace marketplace) {
        String sql = "INSERT INTO marketplace (id_stock, prix_unitaire, quantite_en_vente, statut, date_publication, description) VALUES (?, ?, ?, ?, ?, ?)";

        System.out.println("\n🔵 [MarketplaceDAO.ajouterMarketplace] Début ajout annonce");

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, marketplace.getIdStock());
            stmt.setDouble(2, marketplace.getPrixUnitaire());
            stmt.setDouble(3, marketplace.getQuantiteEnVente());
            stmt.setString(4, marketplace.getStatut());
            stmt.setTimestamp(5, Timestamp.valueOf(marketplace.getDatePublication()));
            stmt.setString(6, marketplace.getDescription());

            int affectedRows = stmt.executeUpdate();
            System.out.println("   ✅ Lignes affectées: " + affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        marketplace.setIdMarketplace(generatedKeys.getInt(1));
                        System.out.println("   ✅ ID généré: " + marketplace.getIdMarketplace());
                    }
                }
            }
            System.out.println("✅ [MarketplaceDAO.ajouterMarketplace] Ajout réussi\n");

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.ajouterMarketplace] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifierMarketplace(Marketplace marketplace) {
        String sql = "UPDATE marketplace SET prix_unitaire = ?, quantite_en_vente = ?, statut = ?, description = ? WHERE id_marketplace = ?";

        System.out.println("\n🔵 [MarketplaceDAO.modifierMarketplace] Début modification annonce ID: " + marketplace.getIdMarketplace());

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, marketplace.getPrixUnitaire());
            stmt.setDouble(2, marketplace.getQuantiteEnVente());
            stmt.setString(3, marketplace.getStatut());
            stmt.setString(4, marketplace.getDescription());
            stmt.setInt(5, marketplace.getIdMarketplace());

            int affectedRows = stmt.executeUpdate();
            System.out.println("   ✅ Lignes affectées: " + affectedRows);
            System.out.println("✅ [MarketplaceDAO.modifierMarketplace] Modification réussie\n");

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.modifierMarketplace] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimerMarketplace(int idMarketplace) {
        String sql = "DELETE FROM marketplace WHERE id_marketplace = ?";

        System.out.println("\n🔵 [MarketplaceDAO.supprimerMarketplace] Début suppression annonce ID: " + idMarketplace);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMarketplace);
            int affectedRows = stmt.executeUpdate();
            System.out.println("   ✅ Lignes affectées: " + affectedRows);
            System.out.println("✅ [MarketplaceDAO.supprimerMarketplace] Suppression réussie\n");

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.supprimerMarketplace] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Marketplace getMarketplaceById(int idMarketplace) {
        String sql = "SELECT m.*, s.nom_produit, s.type_produit, u.id as vendeur_id, u.nom, u.prenom " +
                "FROM marketplace m " +
                "LEFT JOIN stock s ON m.id_stock = s.id_stock " +
                "LEFT JOIN utilisateur u ON s.id_utilisateur = u.id " +
                "WHERE m.id_marketplace = ?";
        Marketplace marketplace = null;

        System.out.println("\n🔵 [MarketplaceDAO.getMarketplaceById] Recherche annonce ID: " + idMarketplace);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMarketplace);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                marketplace = new Marketplace();
                marketplace.setIdMarketplace(rs.getInt("id_marketplace"));
                marketplace.setIdStock(rs.getInt("id_stock"));
                marketplace.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                marketplace.setQuantiteEnVente(rs.getDouble("quantite_en_vente"));
                marketplace.setStatut(rs.getString("statut"));
                marketplace.setDescription(rs.getString("description"));
                marketplace.setDatePublication(rs.getTimestamp("date_publication") != null ?
                        rs.getTimestamp("date_publication").toLocalDateTime() : null);
                marketplace.setNomProduit(rs.getString("nom_produit"));
                marketplace.setCategorie(rs.getString("type_produit"));
                marketplace.setNomVendeur(rs.getString("nom") + " " + rs.getString("prenom"));

                System.out.println("   ✅ Annonce trouvée: " + marketplace.getNomProduit());
            } else {
                System.out.println("   ❌ Annonce non trouvée");
            }

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.getMarketplaceById] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return marketplace;
    }

    @Override
    public List<Marketplace> getAllMarketplaces() {
        List<Marketplace> marketplaces = new ArrayList<>();
        String sql = "SELECT m.*, s.nom_produit, s.type_produit, u.nom, u.prenom " +
                "FROM marketplace m " +
                "LEFT JOIN stock s ON m.id_stock = s.id_stock " +
                "LEFT JOIN utilisateur u ON s.id_utilisateur = u.id " +
                "ORDER BY m.date_publication DESC";

        System.out.println("\n🔵 [MarketplaceDAO.getAllMarketplaces] Récupération de toutes les annonces");

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Marketplace marketplace = new Marketplace();
                marketplace.setIdMarketplace(rs.getInt("id_marketplace"));
                marketplace.setIdStock(rs.getInt("id_stock"));
                marketplace.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                marketplace.setQuantiteEnVente(rs.getDouble("quantite_en_vente"));
                marketplace.setStatut(rs.getString("statut"));
                marketplace.setDescription(rs.getString("description"));
                marketplace.setDatePublication(rs.getTimestamp("date_publication") != null ?
                        rs.getTimestamp("date_publication").toLocalDateTime() : null);
                marketplace.setNomProduit(rs.getString("nom_produit"));
                marketplace.setCategorie(rs.getString("type_produit"));
                marketplace.setNomVendeur(rs.getString("nom") + " " + rs.getString("prenom"));

                marketplaces.add(marketplace);
                System.out.println("   📦 Annonce chargée: ID=" + marketplace.getIdMarketplace() +
                        ", Produit=" + marketplace.getNomProduit() +
                        ", Prix=" + marketplace.getPrixUnitaire() + " DT" +
                        ", Statut=" + marketplace.getStatut());
            }

            System.out.println("✅ [MarketplaceDAO.getAllMarketplaces] Total annonces: " + marketplaces.size() + "\n");

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.getAllMarketplaces] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return marketplaces;
    }

    @Override
    public List<Marketplace> getMarketplacesByUtilisateur(int idUtilisateur) {
        List<Marketplace> marketplaces = new ArrayList<>();
        String sql = "SELECT m.*, s.nom_produit, s.type_produit " +
                "FROM marketplace m " +
                "LEFT JOIN stock s ON m.id_stock = s.id_stock " +
                "WHERE s.id_utilisateur = ? " +
                "ORDER BY m.date_publication DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Marketplace marketplace = new Marketplace();
                marketplace.setIdMarketplace(rs.getInt("id_marketplace"));
                marketplace.setIdStock(rs.getInt("id_stock"));
                marketplace.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                marketplace.setQuantiteEnVente(rs.getDouble("quantite_en_vente"));
                marketplace.setStatut(rs.getString("statut"));
                marketplace.setDescription(rs.getString("description"));
                marketplace.setDatePublication(rs.getTimestamp("date_publication") != null ?
                        rs.getTimestamp("date_publication").toLocalDateTime() : null);
                marketplace.setNomProduit(rs.getString("nom_produit"));
                marketplace.setCategorie(rs.getString("type_produit"));

                marketplaces.add(marketplace);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return marketplaces;
    }

    @Override
    public List<Marketplace> getMarketplacesDisponibles() {
        List<Marketplace> marketplaces = new ArrayList<>();
        String sql = "SELECT m.*, s.nom_produit, s.type_produit, u.nom, u.prenom " +
                "FROM marketplace m " +
                "LEFT JOIN stock s ON m.id_stock = s.id_stock " +
                "LEFT JOIN utilisateur u ON s.id_utilisateur = u.id " +
                "WHERE m.statut = 'En vente' " +
                "ORDER BY m.date_publication DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Marketplace marketplace = new Marketplace();
                marketplace.setIdMarketplace(rs.getInt("id_marketplace"));
                marketplace.setIdStock(rs.getInt("id_stock"));
                marketplace.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                marketplace.setQuantiteEnVente(rs.getDouble("quantite_en_vente"));
                marketplace.setStatut(rs.getString("statut"));
                marketplace.setDescription(rs.getString("description"));
                marketplace.setDatePublication(rs.getTimestamp("date_publication") != null ?
                        rs.getTimestamp("date_publication").toLocalDateTime() : null);
                marketplace.setNomProduit(rs.getString("nom_produit"));
                marketplace.setCategorie(rs.getString("type_produit"));
                marketplace.setNomVendeur(rs.getString("nom") + " " + rs.getString("prenom"));

                marketplaces.add(marketplace);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return marketplaces;
    }

    @Override
    public List<Marketplace> searchMarketplace(String keyword) {
        List<Marketplace> marketplaces = new ArrayList<>();
        String sql = "SELECT m.*, s.nom_produit, s.type_produit, u.nom, u.prenom " +
                "FROM marketplace m " +
                "LEFT JOIN stock s ON m.id_stock = s.id_stock " +
                "LEFT JOIN utilisateur u ON s.id_utilisateur = u.id " +
                "WHERE (s.nom_produit LIKE ? OR m.description LIKE ?) " +
                "AND m.statut = 'En vente' " +
                "ORDER BY m.date_publication DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Marketplace marketplace = new Marketplace();
                marketplace.setIdMarketplace(rs.getInt("id_marketplace"));
                marketplace.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                marketplace.setQuantiteEnVente(rs.getDouble("quantite_en_vente"));
                marketplace.setStatut(rs.getString("statut"));
                marketplace.setDescription(rs.getString("description"));
                marketplace.setNomProduit(rs.getString("nom_produit"));
                marketplace.setCategorie(rs.getString("type_produit"));
                marketplace.setNomVendeur(rs.getString("nom") + " " + rs.getString("prenom"));

                marketplaces.add(marketplace);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return marketplaces;
    }

    @Override
    public void changerStatut(int idMarketplace, String nouveauStatut) {
        String sql = "UPDATE marketplace SET statut = ? WHERE id_marketplace = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nouveauStatut);
            stmt.setInt(2, idMarketplace);
            stmt.executeUpdate();
            System.out.println("✅ [MarketplaceDAO.changerStatut] Statut modifié: " + nouveauStatut);

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.changerStatut] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void vendreProduit(int idMarketplace, double quantite) {
        String sql = "UPDATE marketplace SET quantite_en_vente = quantite_en_vente - ? WHERE id_marketplace = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, quantite);
            stmt.setInt(2, idMarketplace);
            stmt.executeUpdate();
            System.out.println("✅ [MarketplaceDAO.vendreProduit] Vente effectuée: " + quantite + " unités");

        } catch (SQLException e) {
            System.err.println("❌ [MarketplaceDAO.vendreProduit] Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}