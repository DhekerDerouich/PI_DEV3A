package tn.esprit.farmvision.gestionParcelleEtCulture.dao;


import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;
import tn.esprit.farmvision.gestionuser.util.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CultureDao {



    public void add(Culture c) throws SQLException {
        String sql = "INSERT INTO culture(nomCulture, typeCulture, dateSemis, dateRecolte) VALUES (?,?,?,?)";
        Connection cnx = Myconnection.getConnection();

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, c.getNomCulture());
        ps.setString(2, c.getTypeCulture());
        ps.setDate(3, c.getDateSemis());
        ps.setDate(4, c.getDateRecolte());

        ps.executeUpdate();
    }

    public List<Culture> getAll() throws SQLException {
        List<Culture> list = new ArrayList<>();

        String sql = "SELECT * FROM culture";
        Connection conn = Myconnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while(rs.next()) {
            list.add(new Culture(
                    rs.getInt("idCulture"),
                    rs.getString("nomCulture"),
                    rs.getString("typeCulture"),
                    rs.getDate("dateSemis"),
                    rs.getDate("dateRecolte")
            ));
        }

        return list;
    }

    public void update(Culture c) throws SQLException {
        String sql = """
            UPDATE culture 
            SET nomCulture=?, typeCulture=?, dateSemis=?, dateRecolte=?
            WHERE idCulture=?
        """;
        Connection cnx = Myconnection.getConnection();
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, c.getNomCulture());
        ps.setString(2, c.getTypeCulture());
        ps.setDate(3, c.getDateSemis());
        ps.setDate(4, c.getDateRecolte());
        ps.setInt(5, c.getIdCulture());

        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM culture WHERE idCulture=?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, id);
        ps.executeUpdate();
    }
}

