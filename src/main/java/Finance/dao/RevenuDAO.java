package Finance.dao;

import Finance.model.Revenu;
import util.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RevenuDAO {

    public void ajouterRevenu(Revenu revenu) throws SQLException {
        String sql = "INSERT INTO revenu (montant, source, dateRevenu) VALUES (?, ?, ?)";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, revenu.getMontant());
        ps.setString(2, revenu.getSource());
        ps.setDate(3, new java.sql.Date(revenu.getDateRevenu().getTime()));

        ps.executeUpdate();
    }

    public List<Revenu> getAllRevenus() throws SQLException {
        List<Revenu> revenus = new ArrayList<>();
        String sql = "SELECT * FROM revenu";

        Connection conn = Myconnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);


        return revenus;
    }



    public void updateRevenu(Revenu revenu) throws SQLException {
        String sql = "UPDATE revenu SET montant = ?, source = ?, dateRevenu = ? WHERE idRevenu = ?";

        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, revenu.getMontant());
        ps.setString(2, revenu.getSource());
        ps.setDate(3, new java.sql.Date(revenu.getDateRevenu().getTime()));
        ps.setLong(4, revenu.getIdRevenu());

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


}