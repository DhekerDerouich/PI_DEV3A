package tn.esprit.farmvision.gestionParcelleEtCulture.dao;


import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;
import tn.esprit.farmvision.gestionuser.util.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParcelleDao {



    public void add(Parcelle p) throws SQLException {
        String sql = "INSERT INTO parcelle(surface, localisation) VALUES (?,?)";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setFloat(1, p.getSurface());
        ps.setString(2, p.getLocalisation());

        ps.executeUpdate();
    }

    public List<Parcelle> getAll() throws SQLException {
        List<Parcelle> list = new ArrayList<>();

        String sql = "SELECT * FROM parcelle";
        Connection cnx = Myconnection.getConnection();
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while(rs.next()) {
            list.add(new Parcelle(
                    rs.getInt("idParcelle"),
                    rs.getFloat("surface"),
                    rs.getString("localisation")
            ));
        }

        return list;
    }

    public void update(Parcelle p) throws SQLException {
        String sql = "UPDATE parcelle SET surface=?, localisation=? WHERE idParcelle=?";
        Connection conn = Myconnection.getConnection();
        Statement st = conn.createStatement();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setFloat(1, p.getSurface());
        ps.setString(2, p.getLocalisation());
        ps.setInt(3, p.getIdParcelle());

        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM parcelle WHERE idParcelle=?";
        Connection conn = Myconnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
