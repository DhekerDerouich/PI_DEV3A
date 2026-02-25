package Finance.dao;

import Finance.model.Revenu;
import util.Myconnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RevenuDAO {

    public void ajouterRevenu(Revenu revenu) throws SQLException {
        String sql = "INSERT INTO revenu (montant, source, description,dateRevenu) VALUES (?, ?, ?,?)";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, revenu.getMontant());
        ps.setString(2, revenu.getSource());
        ps.setString(3, revenu.getDescription());
        ps.setDate(4, new java.sql.Date(revenu.getDateRevenu().getTime()));

        ps.executeUpdate();
    }

    public List<Revenu> getAllRevenus() throws SQLException {
        List<Revenu> revenus = new ArrayList<>();
        String sql = "SELECT * FROM revenu";

        Connection conn = Myconnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        // ADD THIS WHILE LOOP - You were missing this!
        while (rs.next()) {
            Revenu revenu = new Revenu();
            revenu.setIdRevenu(rs.getLong("idRevenu")); // Adjust column name to match your DB
            revenu.setMontant(rs.getDouble("montant"));
            revenu.setSource(rs.getString("source"));
            revenu.setDescription(rs.getString("description"));
            revenu.setDateRevenu(rs.getDate("dateRevenu")); // Adjust column name

            revenus.add(revenu);
        }



        return revenus;
    }



    public void updateRevenu(Revenu revenu) throws SQLException {
        String sql = "UPDATE revenu SET montant = ?, source = ?, description = ?,dateRevenu = ? WHERE idRevenu = ?";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, revenu.getMontant());
        ps.setString(2, revenu.getSource());
        ps.setString(3, revenu.getDescription());
        ps.setDate(4, new java.sql.Date(revenu.getDateRevenu().getTime()));
        ps.setLong(5, revenu.getIdRevenu());

        ps.executeUpdate();
    }
    public Revenu getRevenuById(long id) throws SQLException {
        String sql = "SELECT * FROM revenu WHERE id_revenu = ?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Revenu r = new Revenu();
            r.setIdRevenu(rs.getLong("idRevenu"));
            r.setMontant(rs.getDouble("montant"));
            r.setSource(rs.getString("source"));
            r.setDescription(rs.getString("description"));
            r.setDateRevenu(rs.getDate("dateRevenu"));
            return r;
        }
        return null;
    }

    public void deleteRevenu(long id) throws SQLException {
        String sql = "DELETE FROM revenu WHERE idRevenu = ?";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);

        ps.executeUpdate();
    }
    public double getTotalByPeriode(String periode) throws SQLException {

        String sql = """
        SELECT COALESCE(SUM(montant), 0)
        FROM depense
        WHERE dateDepense >= ?
        AND dateDepense < ?
    """;

        LocalDate start = LocalDate.parse(periode + "-01");
        LocalDate end = start.plusMonths(1);

        try (Connection conn = Myconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }

        return 0;
    }
    public Map<String, Double> getMensuelTotals(int months) throws SQLException {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(dateRevenu, '%Y-%m') as mois, COALESCE(SUM(montant), 0) as total " +
                "FROM revenu WHERE dateRevenu >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "GROUP BY mois ORDER BY mois ASC";
        try (Connection conn = Myconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("mois"), rs.getDouble("total"));
            }
        }
        return result;
    }
    public List<Revenu> getRecentRevenus(int limit) throws SQLException {
        List<Revenu> list = new ArrayList<>();
        String sql = "SELECT * FROM revenu ORDER BY dateRevenu DESC LIMIT ?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Revenu r = new Revenu();
            r.setIdRevenu(rs.getLong("idRevenu"));
            r.setMontant(rs.getDouble("montant"));
            r.setSource(rs.getString("source"));
            r.setDescription(rs.getString("description"));
            r.setDateRevenu(rs.getDate("dateRevenu"));
            list.add(r);
        }
        return list;
    }



}