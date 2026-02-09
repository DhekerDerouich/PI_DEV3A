package com.pi.service;

import com.pi.dao.EquipementDAO;
import com.pi.model.Equipement;
import java.time.LocalDate;
import java.util.List;

public class EquipementService {

    private final EquipementDAO equipementDAO = new EquipementDAO();

    // ========================
    //         CREATE
    // ========================
    public Equipement addEquipement(String nom, String type, String etat,
                                    LocalDate dateAchat, int dureeVieEstimee,
                                    Integer parcelleId) throws Exception {

        // Validation des données
        if (nom == null || nom.trim().isEmpty()) {
            throw new Exception("Le nom de l'équipement est obligatoire.");
        }

        if (type == null || type.trim().isEmpty()) {
            throw new Exception("Le type d'équipement est obligatoire.");
        }

        if (!isValidEtat(etat)) {
            throw new Exception("État invalide. Valeurs acceptées : Fonctionnel, En panne, Maintenance");
        }

        if (dateAchat == null) {
            throw new Exception("La date d'achat est obligatoire.");
        }

        if (dateAchat.isAfter(LocalDate.now())) {
            throw new Exception("La date d'achat ne peut pas être dans le futur.");
        }

        if (dureeVieEstimee <= 0) {
            throw new Exception("La durée de vie estimée doit être supérieure à 0.");
        }

        // Création de l'objet
        Equipement equipement = new Equipement();
        equipement.setNom(nom.trim());
        equipement.setType(type.trim());
        equipement.setEtat(etat);
        equipement.setDateAchat(dateAchat);
        equipement.setDureeVieEstimee(dureeVieEstimee);
        equipement.setParcelleId(parcelleId);

        // Sauvegarde
        boolean success = equipementDAO.addEquipement(equipement);

        if (!success) {
            throw new Exception("Erreur lors de l'ajout de l'équipement.");
        }

        return equipement;
    }

    // ========================
    //          READ
    // ========================
    public List<Equipement> getAllEquipements() {
        return equipementDAO.getAllEquipements();
    }

    public Equipement getEquipementById(int id) throws Exception {
        Equipement equipement = equipementDAO.getEquipementById(id);

        if (equipement == null) {
            throw new Exception("Équipement avec ID " + id + " non trouvé.");
        }

        return equipement;
    }

    public List<Equipement> searchEquipements(String searchTerm) {
        return equipementDAO.searchByNom(searchTerm);
    }

    // ========================
    //         UPDATE
    // ========================
    public Equipement updateEquipement(int id, String nom, String type, String etat,
                                       LocalDate dateAchat, Integer dureeVieEstimee,
                                       Integer parcelleId) throws Exception {

        // Vérifier l'existence
        Equipement equipement = getEquipementById(id);

        // Mettre à jour les champs fournis
        if (nom != null && !nom.trim().isEmpty()) {
            equipement.setNom(nom.trim());
        }

        if (type != null && !type.trim().isEmpty()) {
            equipement.setType(type.trim());
        }

        if (etat != null && !etat.trim().isEmpty()) {
            if (!isValidEtat(etat)) {
                throw new Exception("État invalide. Valeurs acceptées : Fonctionnel, En panne, Maintenance");
            }
            equipement.setEtat(etat);
        }

        if (dateAchat != null) {
            if (dateAchat.isAfter(LocalDate.now())) {
                throw new Exception("La date d'achat ne peut pas être dans le futur.");
            }
            equipement.setDateAchat(dateAchat);
        }

        if (dureeVieEstimee != null) {
            if (dureeVieEstimee <= 0) {
                throw new Exception("La durée de vie estimée doit être supérieure à 0.");
            }
            equipement.setDureeVieEstimee(dureeVieEstimee);
        }

        if (parcelleId != null) {
            equipement.setParcelleId(parcelleId == 0 ? null : parcelleId);
        }

        // Sauvegarder les modifications
        boolean success = equipementDAO.updateEquipement(equipement);

        if (!success) {
            throw new Exception("Erreur lors de la mise à jour de l'équipement.");
        }

        return equipement;
    }

    // ========================
    //         DELETE
    // ========================
    public void deleteEquipement(int id) throws Exception {
        // Vérifier l'existence
        getEquipementById(id);

        boolean success = equipementDAO.deleteEquipement(id);

        if (!success) {
            throw new Exception("Erreur lors de la suppression de l'équipement.");
        }
    }

    // ========================
    //     AUTRES MÉTHODES
    // ========================
    public int countEquipementsByEtat(String etat) {
        return equipementDAO.countByEtat(etat);
    }

    public long getTotalEquipements() {
        return equipementDAO.getAllEquipements().size();
    }

    public double getPourcentageEquipementsFonctionnels() {
        long total = getTotalEquipements();
        if (total == 0) return 0;

        long fonctionnels = countEquipementsByEtat("Fonctionnel");
        return (fonctionnels * 100.0) / total;
    }

    // ========================
    //     VALIDATION
    // ========================
    private boolean isValidEtat(String etat) {
        return etat != null &&
                (etat.equals("Fonctionnel") ||
                        etat.equals("En panne") ||
                        etat.equals("Maintenance"));
    }

    public boolean equipementExists(int id) {
        try {
            return getEquipementById(id) != null;
        } catch (Exception e) {
            return false;
        }
    }
}