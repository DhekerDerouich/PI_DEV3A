package Finance.service;

import Finance.dao.DepenseDAO;
import Finance.model.Depense;

import java.sql.SQLException;
import java.util.List;

public class depenseService {

    private DepenseDAO depenseDAO = new DepenseDAO();


    public void ajouterDepense(Depense depense) throws SQLException {
        depenseDAO.ajouterDepense(depense);
    }
    // READ ALL
    public List<Depense> getAllDepenses() throws SQLException {
        return depenseDAO.getAllDepenses();
    }
    public Depense getDepenseById(long id) throws SQLException {
        return depenseDAO.getDepenseById(id);
    }

    // UPDATE
    public void updateDepense(Depense depense) throws SQLException {
        if (depense.getIdDepense() == null || depense.getIdDepense() <= 0) {
            throw new IllegalArgumentException("ID de dépense invalide");
        }
        validateDepense(depense);
        depenseDAO.updateDepense(depense);
    }

    // DELETE
    public void deleteDepense(long id) throws SQLException {
        depenseDAO.deleteDepense(id);
    }

    // Validation helper
    private void validateDepense(Depense depense) {
        if (depense.getMontant() == null || depense.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (depense.getTypeDepense() == null || depense.getTypeDepense().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de dépense est requis");
        }
        if (depense.getDateDepense() == null) {
            throw new IllegalArgumentException("La date est requise");
        }
    }
    public List<Depense> getRecentDepenses(int limit) throws SQLException {
        return depenseDAO.getRecentDepenses(limit);
    }




}