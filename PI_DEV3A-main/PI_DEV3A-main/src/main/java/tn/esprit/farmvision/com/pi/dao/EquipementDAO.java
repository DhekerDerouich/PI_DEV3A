package tn.esprit.farmvision.com.pi.dao;

import tn.esprit.farmvision.com.pi.model.Equipement;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EquipementDAO {

    // CREATE - Ajouter un nouvel équipement
    public boolean addEquipement(Equipement equipement) {
        String sql = "INSERT INTO equipement (nom, type, etat, date_achat, duree_vie_estimee, parcelle_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Connexion à la base de données non disponible");
                return false;
            }

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, equipement.getNom());
            pstmt.setString(2, equipement.getType());
            pstmt.setString(3, equipement.getEtat());

            // Gérer la date d'achat
            if (equipement.getDateAchat() != null) {
                pstmt.setDate(4, Date.valueOf(equipement.getDateAchat()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.setInt(5, equipement.getDureeVieEstimee());

            // Gérer parcelle_id (peut être null)
            if (equipement.getParcelleId() != null) {
                pstmt.setInt(6, equipement.getParcelleId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Récupérer l'ID généré
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    equipement.setId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'ajout de l'équipement : " + e.getMessage());
            System.err.println("SQL : " + sql);
            e.printStackTrace();
        } finally {
            // Fermer les ressources
            closeResources(null, pstmt, generatedKeys);
        }

        return false;
    }

    // READ ALL - Récupérer tous les équipements
    public List<Equipement> getAllEquipements() {
        List<Equipement> equipements = new ArrayList<>();
        String sql = "SELECT * FROM equipement ORDER BY id DESC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return equipements;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Equipement equipement = mapResultSetToEquipement(rs);
                equipements.add(equipement);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération des équipements : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }

        return equipements;
    }

    // READ BY ID - Récupérer un équipement par son ID
    public Equipement getEquipementById(int id) {
        String sql = "SELECT * FROM equipement WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return null;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEquipement(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération de l'équipement ID " + id + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }

        return null;
    }

    // UPDATE - Mettre à jour un équipement
    public boolean updateEquipement(Equipement equipement) {
        String sql = "UPDATE equipement SET nom = ?, type = ?, etat = ?, date_achat = ?, " +
                "duree_vie_estimee = ?, parcelle_id = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, equipement.getNom());
            pstmt.setString(2, equipement.getType());
            pstmt.setString(3, equipement.getEtat());

            // Gérer la date d'achat
            if (equipement.getDateAchat() != null) {
                pstmt.setDate(4, Date.valueOf(equipement.getDateAchat()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.setInt(5, equipement.getDureeVieEstimee());

            // Gérer parcelle_id (peut être null)
            if (equipement.getParcelleId() != null) {
                pstmt.setInt(6, equipement.getParcelleId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setInt(7, equipement.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la mise à jour de l'équipement : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null);
        }

        return false;
    }

    // DELETE - Supprimer un équipement par ID
    public boolean deleteEquipement(int id) {
        String sql = "DELETE FROM equipement WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la suppression de l'équipement ID " + id + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null);
        }

        return false;
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet Equipement
    private Equipement mapResultSetToEquipement(ResultSet rs) throws SQLException {
        Equipement equipement = new Equipement();

        equipement.setId(rs.getInt("id"));
        equipement.setNom(rs.getString("nom"));
        equipement.setType(rs.getString("type"));
        equipement.setEtat(rs.getString("etat"));

        // Date d'achat
        Date dateAchat = rs.getDate("date_achat");
        if (dateAchat != null) {
            equipement.setDateAchat(dateAchat.toLocalDate());
        }

        equipement.setDureeVieEstimee(rs.getInt("duree_vie_estimee"));

        // Parcelle ID (peut être null)
        int parcelleId = rs.getInt("parcelle_id");
        if (!rs.wasNull()) {
            equipement.setParcelleId(parcelleId);
        }

        // Timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            equipement.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            equipement.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return equipement;
    }

    // Méthode pour compter le nombre d'équipements par état
    public int countByEtat(String etat) {
        String sql = "SELECT COUNT(*) FROM equipement WHERE etat = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return 0;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, etat);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du comptage par état : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }

        return 0;
    }

    // Méthode pour rechercher des équipements par nom (LIKE)
    public List<Equipement> searchByNom(String searchTerm) {
        List<Equipement> equipements = new ArrayList<>();
        String sql = "SELECT * FROM equipement WHERE nom LIKE ? ORDER BY nom";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return equipements;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + searchTerm + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Equipement equipement = mapResultSetToEquipement(rs);
                equipements.add(equipement);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }

        return equipements;
    }

    // Méthode pour fermer les ressources JDBC
    private void closeResources(ResultSet rs, Statement stmt, ResultSet generatedKeys) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (generatedKeys != null) generatedKeys.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la fermeture des ressources JDBC : " + e.getMessage());
        }
    }
}