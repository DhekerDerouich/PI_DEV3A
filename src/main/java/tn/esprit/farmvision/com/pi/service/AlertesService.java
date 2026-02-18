package com.pi.service;

import com.pi.model.Equipement;
import com.pi.model.Maintenance;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AlertesService {

    private final EquipementService equipementService = new EquipementService();
    private final MaintenanceService maintenanceService = new MaintenanceService();

    public static class Alerte {
        private String titre;
        private String message;
        private String type; // "URGENT", "WARNING", "INFO"
        private String categorie; // "MAINTENANCE", "GARANTIE", "PANNE", "RETARD"
        private LocalDate date;
        private Runnable action;
        private int priorite; // 1 = Haute, 2 = Moyenne, 3 = Basse

        public Alerte(String titre, String message, String type, String categorie) {
            this.titre = titre;
            this.message = message;
            this.type = type;
            this.categorie = categorie;
            this.date = LocalDate.now();
            this.priorite = calculerPriorite(type);
        }

        private int calculerPriorite(String type) {
            switch (type) {
                case "URGENT": return 1;
                case "WARNING": return 2;
                default: return 3;
            }
        }

        public Alerte avecAction(Runnable action) {
            this.action = action;
            return this;
        }

        // Getters
        public String getTitre() { return titre; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public String getCategorie() { return categorie; }
        public LocalDate getDate() { return date; }
        public Runnable getAction() { return action; }
        public int getPriorite() { return priorite; }

        public String getIcone() {
            switch (categorie) {
                case "MAINTENANCE": return "🔧";
                case "GARANTIE": return "🛡️";
                case "PANNE": return "🔴";
                case "RETARD": return "⏰";
                default: return "ℹ️";
            }
        }

        public String getCouleurFond() {
            switch (type) {
                case "URGENT": return "#ffebee"; // Rouge clair
                case "WARNING": return "#fff3e0"; // Orange clair
                default: return "#e8f5e8"; // Vert clair
            }
        }
    }

    public List<Alerte> getToutesLesAlertes() {
        List<Alerte> alertes = new ArrayList<>();

        // Alertes de maintenances à venir
        alertes.addAll(getAlertesMaintenancesProches());

        // Alertes de garanties qui expirent
        alertes.addAll(getAlertesGaranties());

        // Alertes d'équipements en panne
        alertes.addAll(getAlertesPannes());

        // Alertes de maintenances en retard
        alertes.addAll(getAlertesMaintenancesRetard());

        // Alertes d'équipements sans maintenance
        alertes.addAll(getAlertesSansMaintenance());

        // Alertes de maintenances aujourd'hui
        alertes.addAll(getAlertesMaintenancesAujourdhui());

        // Alertes de maintenances dépassées
        alertes.addAll(getAlertesMaintenancesDepassees());

        // Trier par priorité (1 = plus urgent) et par date
        alertes.sort((a1, a2) -> {
            if (a1.getPriorite() != a2.getPriorite()) {
                return Integer.compare(a1.getPriorite(), a2.getPriorite());
            }
            return a1.getDate().compareTo(a2.getDate());
        });

        return alertes;
    }

    private List<Alerte> getAlertesMaintenancesProches() {
        List<Alerte> alertes = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();

        for (Maintenance m : maintenanceService.getAllMaintenances()) {
            if ("Planifiée".equals(m.getStatut()) && !m.getDateMaintenance().isBefore(aujourdhui)) {
                long joursRestants = ChronoUnit.DAYS.between(aujourdhui, m.getDateMaintenance());

                if (joursRestants <= 7) {
                    Equipement e = null;
                    try {
                        e = equipementService.getEquipementById(m.getEquipementId());
                    } catch (Exception ex) {
                        continue;
                    }

                    String message = String.format(
                            "🔧 %s - Maintenance %s dans %d jours\nDate: %s\nCoût: %.2f DT",
                            e.getNom(),
                            m.getTypeMaintenance(),
                            joursRestants,
                            m.getDateMaintenance().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            m.getCout()
                    );

                    String type = joursRestants <= 2 ? "URGENT" :
                            (joursRestants <= 4 ? "WARNING" : "INFO");

                    Alerte alerte = new Alerte(
                            "⚠️ Maintenance imminente",
                            message,
                            type,
                            "MAINTENANCE"
                    );

                    alerte.avecAction(() -> voirMaintenance(m));
                    alertes.add(alerte);
                }
            }
        }
        return alertes;
    }

    private List<Alerte> getAlertesMaintenancesAujourdhui() {
        List<Alerte> alertes = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();

        for (Maintenance m : maintenanceService.getAllMaintenances()) {
            if ("Planifiée".equals(m.getStatut()) && m.getDateMaintenance().equals(aujourdhui)) {
                Equipement e = null;
                try {
                    e = equipementService.getEquipementById(m.getEquipementId());
                } catch (Exception ex) {
                    continue;
                }

                String message = String.format(
                        "🔧 %s - Maintenance %s AUJOURD'HUI !\nDescription: %s\nCoût: %.2f DT",
                        e.getNom(),
                        m.getTypeMaintenance(),
                        m.getDescription(),
                        m.getCout()
                );

                Alerte alerte = new Alerte(
                        "🔔 Maintenance aujourd'hui",
                        message,
                        "URGENT",
                        "MAINTENANCE"
                );

                alerte.avecAction(() -> voirMaintenance(m));
                alertes.add(alerte);
            }
        }
        return alertes;
    }

    private List<Alerte> getAlertesMaintenancesDepassees() {
        List<Alerte> alertes = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();

        for (Maintenance m : maintenanceService.getAllMaintenances()) {
            if ("Planifiée".equals(m.getStatut()) && m.getDateMaintenance().isBefore(aujourdhui)) {
                long joursDepasses = ChronoUnit.DAYS.between(m.getDateMaintenance(), aujourdhui);

                Equipement e = null;
                try {
                    e = equipementService.getEquipementById(m.getEquipementId());
                } catch (Exception ex) {
                    continue;
                }

                String message = String.format(
                        "🔧 %s - Maintenance dépassée de %d jours !\nDate prévue: %s",
                        e.getNom(),
                        joursDepasses,
                        m.getDateMaintenance().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );

                Alerte alerte = new Alerte(
                        "⚠️ Maintenance dépassée",
                        message,
                        joursDepasses > 3 ? "URGENT" : "WARNING",
                        "RETARD"
                );

                alerte.avecAction(() -> voirMaintenance(m));
                alertes.add(alerte);
            }
        }
        return alertes;
    }

    private List<Alerte> getAlertesGaranties() {
        List<Alerte> alertes = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();

        for (Equipement e : equipementService.getAllEquipements()) {
            if (e.getDateAchat() != null) {
                LocalDate finGarantie = e.getDateAchat().plusYears(e.getDureeVieEstimee());

                if (finGarantie.isAfter(aujourdhui)) {
                    long joursRestants = ChronoUnit.DAYS.between(aujourdhui, finGarantie);

                    if (joursRestants <= 90) { // 3 mois avant expiration
                        String message = String.format(
                                "🛡️ %s - Garantie expire dans %d jours\nDate fin: %s\nÂge: %d ans",
                                e.getNom(),
                                joursRestants,
                                finGarantie.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                ChronoUnit.YEARS.between(e.getDateAchat(), aujourdhui)
                        );

                        String type = joursRestants <= 30 ? "URGENT" :
                                (joursRestants <= 60 ? "WARNING" : "INFO");

                        alertes.add(new Alerte(
                                "Garantie bientôt expirée",
                                message,
                                type,
                                "GARANTIE"
                        ));
                    }
                } else {
                    // Garantie déjà expirée
                    long joursDepasses = ChronoUnit.DAYS.between(finGarantie, aujourdhui);

                    String message = String.format(
                            "🛡️ %s - Garantie expirée depuis %d jours !\nDate fin: %s",
                            e.getNom(),
                            joursDepasses,
                            finGarantie.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    );

                    alertes.add(new Alerte(
                            "⚠️ Garantie expirée",
                            message,
                            joursDepasses > 30 ? "URGENT" : "WARNING",
                            "GARANTIE"
                    ));
                }
            }
        }
        return alertes;
    }

    private List<Alerte> getAlertesPannes() {
        List<Alerte> alertes = new ArrayList<>();

        for (Equipement e : equipementService.getAllEquipements()) {
            if ("En panne".equals(e.getEtat())) {
                // Chercher la dernière maintenance corrective
                Optional<Maintenance> dernierePanne = maintenanceService.getAllMaintenances().stream()
                        .filter(m -> m.getEquipementId() == e.getId())
                        .filter(m -> "Corrective".equals(m.getTypeMaintenance()))
                        .max(Comparator.comparing(Maintenance::getDateMaintenance));

                long joursEnPanne = 0;
                if (dernierePanne.isPresent()) {
                    joursEnPanne = ChronoUnit.DAYS.between(dernierePanne.get().getDateMaintenance(), LocalDate.now());
                }

                String message = String.format(
                        "🔴 %s est en panne depuis %d jours",
                        e.getNom(),
                        joursEnPanne
                );

                String type = joursEnPanne > 7 ? "URGENT" :
                        (joursEnPanne > 3 ? "WARNING" : "INFO");

                Alerte alerte = new Alerte(
                        "Équipement en panne",
                        message,
                        type,
                        "PANNE"
                );

                alerte.avecAction(() -> voirEquipement(e));
                alertes.add(alerte);
            }
        }
        return alertes;
    }

    private List<Alerte> getAlertesMaintenancesRetard() {
        List<Alerte> alertes = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();

        for (Equipement e : equipementService.getAllEquipements()) {
            if ("Fonctionnel".equals(e.getEtat()) || "Maintenance".equals(e.getEtat())) {
                // Dernière maintenance
                Optional<Maintenance> derniereMaintenance = maintenanceService.getAllMaintenances().stream()
                        .filter(m -> m.getEquipementId() == e.getId())
                        .max(Comparator.comparing(Maintenance::getDateMaintenance));

                if (derniereMaintenance.isPresent()) {
                    Maintenance lastMaint = derniereMaintenance.get();
                    long joursDepuis = ChronoUnit.DAYS.between(lastMaint.getDateMaintenance(), aujourdhui);

                    // Si plus de 3 mois sans maintenance (90 jours)
                    if (joursDepuis > 90) {
                        String message = String.format(
                                "⚠️ %s - Dernière maintenance il y a %d mois\nDate: %s\nType: %s",
                                e.getNom(),
                                joursDepuis / 30,
                                lastMaint.getDateMaintenance().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                lastMaint.getTypeMaintenance()
                        );

                        String type = joursDepuis > 180 ? "URGENT" : "WARNING";

                        alertes.add(new Alerte(
                                "Maintenance retardée",
                                message,
                                type,
                                "RETARD"
                        ));
                    }
                }
            }
        }
        return alertes;
    }

    private List<Alerte> getAlertesSansMaintenance() {
        List<Alerte> alertes = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();

        for (Equipement e : equipementService.getAllEquipements()) {
            // Vérifier si l'équipement a des maintenances
            boolean aDesMaintenances = maintenanceService.getAllMaintenances().stream()
                    .anyMatch(m -> m.getEquipementId() == e.getId());

            if (!aDesMaintenances && e.getDateAchat() != null) {
                long moisDepuisAchat = ChronoUnit.MONTHS.between(e.getDateAchat(), aujourdhui);

                if (moisDepuisAchat > 6) {
                    String message = String.format(
                            "⚠️ %s n'a jamais eu de maintenance (installé il y a %d mois)",
                            e.getNom(),
                            moisDepuisAchat
                    );

                    alertes.add(new Alerte(
                            "⚠️ Maintenance jamais effectuée",
                            message,
                            moisDepuisAchat > 12 ? "WARNING" : "INFO",
                            "RETARD"
                    ));
                }
            }
        }
        return alertes;
    }

    private void voirMaintenance(Maintenance m) {
        // Ouvrir la vue détail de la maintenance
        System.out.println("Voir maintenance: " + m.getId());
        // Ici vous pouvez ajouter du code pour ouvrir une fenêtre de détail
    }

    private void voirEquipement(Equipement e) {
        // Ouvrir la vue détail de l'équipement
        System.out.println("Voir équipement: " + e.getNom());
        // Ici vous pouvez ajouter du code pour ouvrir une fenêtre de détail
    }

    // Statistiques des alertes
    public Map<String, Integer> getStatistiquesAlertes() {
        Map<String, Integer> stats = new HashMap<>();
        List<Alerte> toutes = getToutesLesAlertes();

        stats.put("total", toutes.size());
        stats.put("urgentes", (int) toutes.stream().filter(a -> "URGENT".equals(a.getType())).count());
        stats.put("warnings", (int) toutes.stream().filter(a -> "WARNING".equals(a.getType())).count());
        stats.put("infos", (int) toutes.stream().filter(a -> "INFO".equals(a.getType())).count());

        stats.put("maintenances", (int) toutes.stream().filter(a -> "MAINTENANCE".equals(a.getCategorie())).count());
        stats.put("garanties", (int) toutes.stream().filter(a -> "GARANTIE".equals(a.getCategorie())).count());
        stats.put("pannes", (int) toutes.stream().filter(a -> "PANNE".equals(a.getCategorie())).count());
        stats.put("retards", (int) toutes.stream().filter(a -> "RETARD".equals(a.getCategorie())).count());

        return stats;
    }
}