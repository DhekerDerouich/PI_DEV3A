package com.pi.dao;

import com.pi.model.Maintenance;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    // CREATE - Ajouter une nouvelle maintenance
    public boolean addMaintenance(Maintenance maintenance) {
        String sql = "INSERT INTO maintenance (equipement_id, type_maintenance, description, " +
                "date_maintenance, cout, statut) VALUES (?, ?, ?, ?, ?, ?)";

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

            pstmt.setInt(1, maintenance.getEquipementId());
            pstmt.setString(2, maintenance.getTypeMaintenance());
            pstmt.setString(3, maintenance.getDescription());

            // Date de maintenance
            if (maintenance.getDateMaintenance() != null) {
                pstmt.setDate(4, Date.valueOf(maintenance.getDateMaintenance()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.setDouble(5, maintenance.getCout());
            pstmt.setString(6, maintenance.getStatut());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Récupérer l'ID généré
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    maintenance.setId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'ajout de la maintenance : " + e.getMessage());
            System.err.println("SQL : " + sql);
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, generatedKeys);
        }

        return false;
    }

    // READ ALL - Récupérer toutes les maintenances
    public List<Maintenance> getAllMaintenances() {
        List<Maintenance> maintenances = new ArrayList<>();
        String sql = "SELECT * FROM maintenance ORDER BY date_maintenance DESC, id DESC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return maintenances;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Maintenance maintenance = mapResultSetToMaintenance(rs);
                maintenances.add(maintenance);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération des maintenances : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }

        return maintenances;
    }

    // READ BY ID - Récupérer une maintenance par son ID
    public Maintenance getMaintenanceById(int id) {
        String sql = "SELECT * FROM maintenance WHERE id = ?";

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
                return mapResultSetToMaintenance(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération de la maintenance ID " + id + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }

        return null;
    }

    // READ BY EQUIPEMENT ID - Récupérer les maintenances d'un équipement
    public List<Maintenance> getByEquipementId(int equipementId) {
        List<Maintenance> maintenances = new ArrayList<>();
        String sql = "SELECT * FROM maintenance WHERE equipement_id = ? ORDER BY date_maintenance DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return maintenances;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, equipementId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Maintenance maintenance = mapResultSetToMaintenance(rs);
                maintenances.add(maintenance);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération des maintenances pour l'équipement " +
                    equipementId + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }

        return maintenances;
    }

    // UPDATE - Mettre à jour une maintenance
    public boolean updateMaintenance(Maintenance maintenance) {
        String sql = "UPDATE maintenance SET equipement_id = ?, type_maintenance = ?, description = ?, " +
                "date_maintenance = ?, cout = ?, statut = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, maintenance.getEquipementId());
            pstmt.setString(2, maintenance.getTypeMaintenance());
            pstmt.setString(3, maintenance.getDescription());

            // Date de maintenance
            if (maintenance.getDateMaintenance() != null) {
                pstmt.setDate(4, Date.valueOf(maintenance.getDateMaintenance()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.setDouble(5, maintenance.getCout());
            pstmt.setString(6, maintenance.getStatut());
            pstmt.setInt(7, maintenance.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la mise à jour de la maintenance : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null);
        }

        return false;
    }

    // UPDATE STATUS - Changer seulement le statut
    public boolean updateStatus(int id, String statut) {
        String sql = "UPDATE maintenance SET statut = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, statut);
            pstmt.setInt(2, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du changement de statut de la maintenance ID " +
                    id + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null);
        }

        return false;
    }

    // DELETE - Supprimer une maintenance par ID
    public boolean deleteMaintenance(int id) {
        String sql = "DELETE FROM maintenance WHERE id = ?";

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
            System.err.println("❌ Erreur SQL lors de la suppression de la maintenance ID " + id + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, null);
        }

        return false;
    }

    // Méthode pour compter les maintenances par statut
    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM maintenance WHERE statut = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return 0;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, statut);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du comptage par statut : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }

        return 0;
    }

    // Méthode pour récupérer les maintenances à venir (dans les 7 prochains jours)
    public List<Maintenance> getUpcomingMaintenances() {
        List<Maintenance> maintenances = new ArrayList<>();
        String sql = "SELECT * FROM maintenance WHERE statut = 'Planifiée' " +
                "AND date_maintenance BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) " +
                "ORDER BY date_maintenance ASC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return maintenances;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Maintenance maintenance = mapResultSetToMaintenance(rs);
                maintenances.add(maintenance);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération des maintenances à venir : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }

        return maintenances;
    }

    // Méthode pour calculer le coût total des maintenances
    public double getTotalCout() {
        String sql = "SELECT SUM(cout) FROM maintenance";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return 0.0;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du calcul du coût total : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }

        return 0.0;
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet Maintenance
    private Maintenance mapResultSetToMaintenance(ResultSet rs) throws SQLException {
        Maintenance maintenance = new Maintenance();

        maintenance.setId(rs.getInt("id"));
        maintenance.setEquipementId(rs.getInt("equipement_id"));
        maintenance.setTypeMaintenance(rs.getString("type_maintenance"));
        maintenance.setDescription(rs.getString("description"));

        // Date de maintenance
        Date dateMaintenance = rs.getDate("date_maintenance");
        if (dateMaintenance != null) {
            maintenance.setDateMaintenance(dateMaintenance.toLocalDate());
        }

        maintenance.setCout(rs.getDouble("cout"));
        maintenance.setStatut(rs.getString("statut"));

        // Timestamp
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            maintenance.setCreatedAt(createdAt.toLocalDateTime());
        }

        return maintenance;
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