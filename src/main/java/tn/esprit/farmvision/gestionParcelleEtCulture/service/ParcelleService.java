package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import tn.esprit.farmvision.gestionParcelleEtCulture.dao.ParcelleDao;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;

import java.util.List;

public class ParcelleService {

    private ParcelleDao dao = new ParcelleDao();



    public void ajouter(Parcelle p) throws Exception {
        dao.add(p);
    }

    public List<Parcelle> afficher() throws Exception {
        List<Parcelle> list = dao.getAll();

        // Fill live weather for each parcelle
        for (Parcelle p : list) {
            WeatherService.fillWeather(p);
        }

        return list;
    }
    public void modifier(Parcelle p) throws Exception {
        dao.update(p);
    }
    public Parcelle getById(int id) throws Exception {
        return dao.getById(id);
    }

    public void supprimer(int id) throws Exception {
        dao.delete(id);
    }
}
