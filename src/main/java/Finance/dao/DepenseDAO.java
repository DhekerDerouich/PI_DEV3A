package Finance.dao;

import Finance.model.Depense;
import util.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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


}