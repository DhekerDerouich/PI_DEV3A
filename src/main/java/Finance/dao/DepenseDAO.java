package Finance.dao;

import Finance.model.Depense;
import util.Myconnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DepenseDAO {

    public void ajouterDepense(Depense depense) throws SQLException {
        String sql = "INSERT INTO depense (montant, typeDepense, description,dateDepense) VALUES (?, ?, ?,?)";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, depense.getMontant());
        ps.setString(2, depense.getTypeDepense());
        ps.setString(3, depense.getDescription());
        ps.setDate(4, new java.sql.Date(depense.getDateDepense().getTime()));

        ps.executeUpdate();
    }


    public List<Depense> getAllDepenses() throws SQLException {
        List<Depense> depenses = new ArrayList<>();
        String sql = "SELECT * FROM depense";

        Connection conn = Myconnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Depense d = new Depense();
            d.setIdDepense(rs.getLong("idDepense"));
            d.setMontant(rs.getDouble("montant"));
            d.setTypeDepense(rs.getString("typeDepense"));
            d.setDescription(rs.getString("description"));
            d.setDateDepense(rs.getDate("dateDepense"));
            depenses.add(d);
        }
        return depenses;
    }

    public Depense getDepenseById(long id) throws SQLException {
        String sql = "SELECT * FROM depense WHERE id_depense = ?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Depense d = new Depense();
            d.setIdDepense(rs.getLong("idDepense"));
            d.setMontant(rs.getDouble("montant"));
            d.setTypeDepense(rs.getString("typeDepense"));
            d.setDescription(rs.getString("description"));
            d.setDateDepense(rs.getDate("dateDepense"));
            return d;
        }
        return null;

    }
    public void updateDepense(Depense depense) throws SQLException {
        String sql = "UPDATE depense SET montant = ?, typeDepense = ?, description = ?,dateDepense = ? WHERE idDepense = ?";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, depense.getMontant());
        ps.setString(2, depense.getTypeDepense());
        ps.setString(3, depense.getDescription());
        ps.setDate(4, new java.sql.Date(depense.getDateDepense().getTime()));
        ps.setLong(5, depense.getIdDepense());

        ps.executeUpdate();
    }

    public void deleteDepense(long id) throws SQLException {
        String sql = "DELETE FROM depense WHERE idDepense = ?";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);

        ps.executeUpdate();
    }

    public Map<String, Double> getMensuelTotals(int months) throws SQLException {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(dateDepense, '%Y-%m') as mois, COALESCE(SUM(montant), 0) as total " +
                "FROM depense WHERE dateDepense >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
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
    public Map<String, Double> getTotauxByCategorie(int months) throws SQLException {
        Map<String, Double> result = new HashMap<>();
        String sql = "SELECT typeDepense, SUM(montant) as total FROM depense " +
                "WHERE dateDepense >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "GROUP BY typeDepense";
        try (Connection conn = Myconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("typeDepense"), rs.getDouble("total"));
            }
        }
        return result;
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
    public List<Depense> getRecentDepenses(int limit) throws SQLException {
        List<Depense> list = new ArrayList<>();
        String sql = "SELECT * FROM depense ORDER BY dateDepense DESC LIMIT ?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Depense d = new Depense();
            d.setIdDepense(rs.getLong("idDepense"));
            d.setMontant(rs.getDouble("montant"));
            d.setTypeDepense(rs.getString("typeDepense"));
            d.setDescription(rs.getString("description"));
            d.setDateDepense(rs.getDate("dateDepense"));
            list.add(d);
        }
        return list;
    }



}