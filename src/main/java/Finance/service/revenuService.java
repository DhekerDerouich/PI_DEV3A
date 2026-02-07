package Finance.service;

import Finance.dao.RevenuDAO;
import Finance.model.Depense;
import Finance.model.Revenu;

import java.sql.SQLException;
import java.util.List;

public class revenuService {

    private RevenuDAO revenuDAO = new RevenuDAO();




    public void ajouterRevenu(Revenu revenu) throws SQLException {
        validateRevenu(revenu);
        revenuDAO.ajouterRevenu(revenu);
    }

    public List<Revenu> getAllRevenus() throws SQLException {
        return revenuDAO.getAllRevenus();
    }

    public Revenu getRevenuById(long id) throws SQLException {
        return revenuDAO.getRevenuById(id);
    }

    public void updateRevenu(Revenu revenu) throws SQLException {
        if (revenu.getIdRevenu() == null || revenu.getIdRevenu() <= 0) {
            throw new IllegalArgumentException("ID de revenu invalide");
        }
        validateRevenu(revenu);
        revenuDAO.updateRevenu(revenu);
    }

    public void deleteRevenu(long id) throws SQLException {
        revenuDAO.deleteRevenu(id);
    }

    private void validateRevenu(Revenu revenu) {
        if (revenu.getMontant() == null || revenu.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (revenu.getSource() == null || revenu.getSource().trim().isEmpty()) {
            throw new IllegalArgumentException("La source est requise");
        }
        if (revenu.getDateRevenu() == null) {
            throw new IllegalArgumentException("La date est requise");
        }
    }
}
