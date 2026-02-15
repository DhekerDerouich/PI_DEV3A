package tn.esprit.farmvision.gestionuser.dao;

import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.model.ResponsableExploitation;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.util.Myconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private Connection conn;

    public UtilisateurDAO() {
        try {
            this.conn = Myconnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Erreur de connexion dans DAO : " + e.getMessage());
        }
    }

    private String getTypeRole(Utilisateur u) {
        if (u instanceof Administrateur) return "ADMINISTRATEUR";
        if (u instanceof Agriculteur) return "AGRICULTEUR";
        if (u instanceof ResponsableExploitation) return "RESPONSABLE_EXPLOITATION";
        throw new IllegalArgumentException("Type inconnu");
    }

    public void save(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO utilisateur (type_role, nom, prenom, email, password, matricule, telephone, adresse, activated, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            String typeRole = getTypeRole(user);
            String matricule = null;
            String telephone = null;
            String adresse = null;

            // ✅ CORRECTION ICI
            if (user instanceof Administrateur a) {
                matricule = a.getMatricule();
            } else if (user instanceof Agriculteur a) {
                telephone = a.getTelephone();
                adresse = a.getAdresse();
            } else if (user instanceof ResponsableExploitation r) {
                matricule = r.getMatricule();
            }

            pstmt.setString(1, typeRole);
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getPrenom());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPassword());
            pstmt.setString(6, matricule);
            pstmt.setString(7, telephone);
            pstmt.setString(8, adresse);
            pstmt.setBoolean(9, user.isActivated());
            pstmt.setTimestamp(10, new Timestamp(user.getDateCreation().getTime()));

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToUtilisateur(rs);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
        return null;
    }

    public List<Utilisateur> getAll() {
        List<Utilisateur> list = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";
        Statement stm = null;
        ResultSet rs = null;

        try {
            stm = conn.createStatement();
            rs = stm.executeQuery(req);
            while (rs.next()) {
                Utilisateur u = mapRowToUtilisateur(rs);
                list.add(u);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur getAll : " + ex.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (stm != null) try { stm.close(); } catch (SQLException ignored) {}
        }
        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur delete : " + e.getMessage());
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public void valider(int id) {
        String sql = "UPDATE utilisateur SET activated = 1 WHERE id = ?";
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur valider : " + e.getMessage());
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public void update(Utilisateur user) {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, password=?";
        List<Object> params = new ArrayList<>();
        params.add(user.getNom());
        params.add(user.getPrenom());
        params.add(user.getEmail());
        params.add(user.getPassword());

        if (user instanceof Agriculteur a) {
            sql += ", telephone=?, adresse=?";
            params.add(a.getTelephone());
            params.add(a.getAdresse());
        } else if (user instanceof ResponsableExploitation r) {
            sql += ", matricule=?";
            params.add(r.getMatricule());
        }

        sql += " WHERE id=?";
        params.add(user.getId());

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur update : " + e.getMessage());
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public void resetPassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE utilisateur SET password = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    private Utilisateur mapRowToUtilisateur(ResultSet rs) throws SQLException {
        String role = rs.getString("type_role");
        Utilisateur u;

        switch (role) {
            case "ADMINISTRATEUR":
                u = new Administrateur();
                ((Administrateur) u).setMatricule(rs.getString("matricule"));
                break;
            case "AGRICULTEUR":
                u = new Agriculteur();
                ((Agriculteur) u).setTelephone(rs.getString("telephone"));
                ((Agriculteur) u).setAdresse(rs.getString("adresse"));
                break;
            case "RESPONSABLE_EXPLOITATION":
                u = new ResponsableExploitation();
                ((ResponsableExploitation) u).setMatricule(rs.getString("matricule"));
                break;
            default:
                throw new SQLException("Rôle inconnu : " + role);
        }

        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setDateCreation(rs.getTimestamp("date_creation"));
        u.setActivated(rs.getBoolean("activated"));

        return u;
    }
}