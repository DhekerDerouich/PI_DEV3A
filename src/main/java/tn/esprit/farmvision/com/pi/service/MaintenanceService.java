package tn.esprit.farmvision.com.pi.service;

import tn.esprit.farmvision.com.pi.dao.MaintenanceDAO;
import tn.esprit.farmvision.com.pi.dao.EquipementDAO;
import tn.esprit.farmvision.com.pi.model.Maintenance;
import tn.esprit.farmvision.com.pi.model.Equipement;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceService {

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final EquipementDAO equipementDAO = new EquipementDAO();
    private final EquipementService equipementService = new EquipementService();

    // ========================
    //         CREATE
    // ========================
    public Maintenance planifierMaintenance(int equipementId, String typeMaintenance,
                                            String description, LocalDate dateMaintenance,
                                            double cout, String statut) throws Exception {

        // Validation des données
        if (!equipementService.equipementExists(equipementId)) {
            throw new Exception("Équipement avec ID " + equipementId + " non trouvé.");
        }

        if (!isValidTypeMaintenance(typeMaintenance)) {
            throw new Exception("Type de maintenance invalide. Valeurs acceptées : Préventive, Corrective");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new Exception("La description de la maintenance est obligatoire.");
        }

        if (dateMaintenance == null) {
            throw new Exception("La date de maintenance est obligatoire.");
        }

        if (dateMaintenance.isBefore(LocalDate.now())) {
            throw new Exception("La date de maintenance ne peut pas être dans le passé.");
        }

        if (cout < 0) {
            throw new Exception("Le coût ne peut pas être négatif.");
        }

        if (!isValidStatut(statut)) {
            throw new Exception("Statut invalide. Valeurs acceptées : Planifiée, Réalisée");
        }

        // Vérifier si l'équipement est disponible
        Equipement equipement = equipementDAO.getEquipementById(equipementId);
        if ("Maintenance".equals(equipement.getEtat()) && "Planifiée".equals(statut)) {
            throw new Exception("Cet équipement est déjà en maintenance.");
        }

        // Création de l'objet
        Maintenance maintenance = new Maintenance();
        maintenance.setEquipementId(equipementId);
        maintenance.setTypeMaintenance(typeMaintenance);
        maintenance.setDescription(description.trim());
        maintenance.setDateMaintenance(dateMaintenance);
        maintenance.setCout(cout);
        maintenance.setStatut(statut);

        // Sauvegarde
        boolean success = maintenanceDAO.addMaintenance(maintenance);

        if (!success) {
            throw new Exception("Erreur lors de la planification de la maintenance.");
        }

        // Si la maintenance est planifiée, mettre l'équipement en état "Maintenance"
        if ("Planifiée".equals(statut)) {
            equipementService.updateEquipement(equipementId, null, null, "Maintenance",
                    null, null, null);
        }

        return maintenance;
    }

    // ========================
    //          READ
    // ========================
    public List<Maintenance> getAllMaintenances() {
        return maintenanceDAO.getAllMaintenances();
    }

    public Maintenance getMaintenanceById(int id) throws Exception {
        Maintenance maintenance = maintenanceDAO.getMaintenanceById(id);

        if (maintenance == null) {
            throw new Exception("Maintenance avec ID " + id + " non trouvé.");
        }

        return maintenance;
    }

    public List<Maintenance> getMaintenancesByEquipement(int equipementId) throws Exception {
        if (!equipementService.equipementExists(equipementId)) {
            throw new Exception("Équipement avec ID " + equipementId + " non trouvé.");
        }

        return maintenanceDAO.getByEquipementId(equipementId);
    }

    public List<Maintenance> getUpcomingMaintenances() {
        return maintenanceDAO.getUpcomingMaintenances();
    }

    // ========================
    //         UPDATE
    // ========================
    public Maintenance updateMaintenance(int id, Integer equipementId, String typeMaintenance,
                                         String description, LocalDate dateMaintenance,
                                         Double cout, String statut) throws Exception {

        // Vérifier l'existence
        Maintenance maintenance = getMaintenanceById(id);
        int oldEquipementId = maintenance.getEquipementId();

        // Mettre à jour les champs fournis
        if (equipementId != null) {
            if (!equipementService.equipementExists(equipementId)) {
                throw new Exception("Nouvel équipement avec ID " + equipementId + " non trouvé.");
            }
            maintenance.setEquipementId(equipementId);
        }

        if (typeMaintenance != null && !typeMaintenance.trim().isEmpty()) {
            if (!isValidTypeMaintenance(typeMaintenance)) {
                throw new Exception("Type de maintenance invalide.");
            }
            maintenance.setTypeMaintenance(typeMaintenance);
        }

        if (description != null && !description.trim().isEmpty()) {
            maintenance.setDescription(description.trim());
        }

        if (dateMaintenance != null) {
            if (dateMaintenance.isBefore(LocalDate.now())) {
                throw new Exception("La date de maintenance ne peut pas être dans le passé.");
            }
            maintenance.setDateMaintenance(dateMaintenance);
        }

        if (cout != null) {
            if (cout < 0) {
                throw new Exception("Le coût ne peut pas être négatif.");
            }
            maintenance.setCout(cout);
        }

        String oldStatut = maintenance.getStatut();
        if (statut != null && !statut.trim().isEmpty()) {
            if (!isValidStatut(statut)) {
                throw new Exception("Statut invalide.");
            }
            maintenance.setStatut(statut);

            // Si on passe de Planifiée à Réalisée
            if ("Planifiée".equals(oldStatut) && "Réalisée".equals(statut)) {
                // Mettre l'équipement en état "Fonctionnel"
                equipementService.updateEquipement(maintenance.getEquipementId(),
                        null, null, "Fonctionnel", null, null, null);
            }
            // Si on passe de Réalisée à Planifiée
            else if ("Réalisée".equals(oldStatut) && "Planifiée".equals(statut)) {
                // Mettre l'équipement en état "Maintenance"
                equipementService.updateEquipement(maintenance.getEquipementId(),
                        null, null, "Maintenance", null, null, null);
            }
        }

        // Sauvegarder les modifications
        boolean success = maintenanceDAO.updateMaintenance(maintenance);

        if (!success) {
            throw new Exception("Erreur lors de la mise à jour de la maintenance.");
        }

        // Si l'équipement a changé, mettre à jour l'ancien équipement
        if (equipementId != null && equipementId != oldEquipementId) {
            equipementService.updateEquipement(oldEquipementId, null, null, "Fonctionnel",
                    null, null, null);
        }

        return maintenance;
    }

    // Méthode spécifique pour changer seulement le statut
    public void changerStatutMaintenance(int id, String nouveauStatut) throws Exception {
        if (!isValidStatut(nouveauStatut)) {
            throw new Exception("Statut invalide.");
        }

        Maintenance maintenance = getMaintenanceById(id);

        // Si on passe à Réalisée
        if ("Réalisée".equals(nouveauStatut) && !"Réalisée".equals(maintenance.getStatut())) {
            // Mettre l'équipement en état "Fonctionnel"
            equipementService.updateEquipement(maintenance.getEquipementId(),
                    null, null, "Fonctionnel", null, null, null);
        }

        boolean success = maintenanceDAO.updateStatus(id, nouveauStatut);

        if (!success) {
            throw new Exception("Erreur lors du changement de statut.");
        }
    }

    // ========================
    //         DELETE
    // ========================
    public void deleteMaintenance(int id) throws Exception {
        // Vérifier l'existence
        Maintenance maintenance = getMaintenanceById(id);

        // Si la maintenance était planifiée, remettre l'équipement en état "Fonctionnel"
        if ("Planifiée".equals(maintenance.getStatut())) {
            equipementService.updateEquipement(maintenance.getEquipementId(),
                    null, null, "Fonctionnel", null, null, null);
        }

        boolean success = maintenanceDAO.deleteMaintenance(id);

        if (!success) {
            throw new Exception("Erreur lors de la suppression de la maintenance.");
        }
    }

    // ========================
    //     AUTRES MÉTHODES
    // ========================
    public double getCoutTotalMaintenances() {
        return maintenanceDAO.getTotalCout();
    }

    public int countMaintenancesByStatut(String statut) {
        return maintenanceDAO.countByStatut(statut);
    }

    public int countMaintenancesByType(String type) {
        List<Maintenance> all = getAllMaintenances();
        return (int) all.stream()
                .filter(m -> type.equals(m.getTypeMaintenance()))
                .count();
    }

    public double getCoutMoyenMaintenance() {
        List<Maintenance> all = getAllMaintenances();
        if (all.isEmpty()) return 0;

        double total = getCoutTotalMaintenances();
        return total / all.size();
    }

    public long getNombreMaintenancesAujourdhui() {
        return getAllMaintenances().stream()
                .filter(m -> "Planifiée".equals(m.getStatut()))
                .filter(m -> LocalDate.now().equals(m.getDateMaintenance()))
                .count();
    }

    // ========================
    //     VALIDATION
    // ========================
    private boolean isValidTypeMaintenance(String type) {
        return type != null &&
                (type.equals("Préventive") || type.equals("Corrective"));
    }

    private boolean isValidStatut(String statut) {
        return statut != null &&
                (statut.equals("Planifiée") || statut.equals("Réalisée"));
    }
}