package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import tn.esprit.farmvision.gestionParcelleEtCulture.dao.CultureDao;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;

import java.util.List;

public class CultureService {


    private CultureDao dao = new CultureDao();

    public void ajouter(Culture c) throws Exception {
        dao.add(c);
    }

    public List<Culture> afficher() throws Exception {
        return dao.getAll();
    }

    public void modifier(Culture c) throws Exception {
        dao.update(c);
    }

    public void supprimer(int id) throws Exception {
        dao.delete(id);
    }
}

