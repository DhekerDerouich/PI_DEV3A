package Finance.dao;

import Finance.model.Rapport;
import util.Myconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RapportDAO {

    public void insert(Rapport rapport) throws SQLException {
        String sql = "INSERT INTO rapport_financier (periode, totalDepenses, totalRevenus, profit, cheminPDF, dateGeneration) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, rapport.getPeriode());
        ps.setDouble(2, rapport.getTotalDepenses());
        ps.setDouble(3, rapport.getTotalRevenus());
        ps.setDouble(4, rapport.getProfit());
        ps.setString(5, rapport.getCheminPDF());
        ps.setTimestamp(6, Timestamp.valueOf(rapport.getDateGeneration()));
        ps.executeUpdate();
    }

    public void update(Rapport rapport) throws SQLException {
        String sql = "UPDATE rapport_financier SET totalDepenses=?, totalRevenus=?, profit=?, cheminPDF=?, dateGeneration=? WHERE periode=?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, rapport.getTotalDepenses());
        ps.setDouble(2, rapport.getTotalRevenus());
        ps.setDouble(3, rapport.getProfit());
        ps.setString(4, rapport.getCheminPDF());
        ps.setTimestamp(5, Timestamp.valueOf(rapport.getDateGeneration()));
        ps.setString(6, rapport.getPeriode());
        ps.executeUpdate();
    }

    public Rapport findByPeriode(String periode) throws SQLException {
        String sql = "SELECT * FROM rapport_financier WHERE periode = ?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, periode);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return map(rs);
        }
        return null;
    }

    public List<Rapport> getAll() throws SQLException {
        List<Rapport> rapports = new ArrayList<>();
        String sql = "SELECT * FROM rapport_financier ORDER BY periode DESC";
        Connection conn = Myconnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            rapports.add(map(rs));
        }
        return rapports;
    }

    private Rapport map(ResultSet rs) throws SQLException {
        Rapport r = new Rapport();
        r.setIdRapport(rs.getInt("idRapport"));
        r.setPeriode(rs.getString("periode"));
        r.setTotalDepenses(rs.getDouble("totalDepenses"));
        r.setTotalRevenus(rs.getDouble("totalRevenus"));
        r.setProfit(rs.getDouble("profit"));
        r.setCheminPDF(rs.getString("cheminPDF"));
        r.setDateGeneration(rs.getTimestamp("dateGeneration").toLocalDateTime());
        return r;
    }
}