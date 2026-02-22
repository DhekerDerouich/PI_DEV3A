package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.model.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour calculer les statistiques du dashboard admin
 */
public class DashboardStatsService {

    private final UtilisateurService utilisateurService;

    public DashboardStatsService() {
        this.utilisateurService = new UtilisateurService();
    }

    /**
     * Statistiques principales du dashboard
     */
    public DashboardStats getMainStats() {
        List<Utilisateur> allUsers = utilisateurService.getAll();

        DashboardStats stats = new DashboardStats();
        stats.totalUsers = allUsers.size();

        // Compter par type
        for (Utilisateur user : allUsers) {
            if (user instanceof Administrateur) {
                stats.totalAdmins++;
            } else if (user instanceof Agriculteur) {
                stats.totalAgriculteurs++;
            } else if (user instanceof ResponsableExploitation) {
                stats.totalResponsables++;
            }

            // Compter actifs vs en attente
            if (user.isActivated()) {
                stats.activeUsers++;
            } else {
                stats.pendingUsers++;
            }
        }

        // Nouveaux utilisateurs (7 derniers jours)
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        stats.newUsersLast7Days = (int) allUsers.stream()
                .filter(u -> {
                    LocalDate userDate = u.getDateCreation()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return userDate.isAfter(sevenDaysAgo);
                })
                .count();

        return stats;
    }

    /**
     * Données pour graphique d'évolution des inscriptions (30 derniers jours)
     */
    public Map<String, Integer> getRegistrationTrend() {
        List<Utilisateur> allUsers = utilisateurService.getAll();
        Map<String, Integer> trend = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();

        // Créer les 30 derniers jours
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            trend.put(dateStr, 0);
        }

        // Compter les inscriptions par jour
        for (Utilisateur user : allUsers) {
            LocalDate userDate = user.getDateCreation()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String dateStr = userDate.toString();
            if (trend.containsKey(dateStr)) {
                trend.put(dateStr, trend.get(dateStr) + 1);
            }
        }

        return trend;
    }

    /**
     * Distribution des utilisateurs par type (pour graphique circulaire)
     */
    public Map<String, Integer> getUserDistribution() {
        List<Utilisateur> allUsers = utilisateurService.getAll();
        Map<String, Integer> distribution = new LinkedHashMap<>();

        distribution.put("Agriculteurs", 0);
        distribution.put("Responsables", 0);
        distribution.put("Administrateurs", 0);

        for (Utilisateur user : allUsers) {
            if (user instanceof Agriculteur) {
                distribution.put("Agriculteurs", distribution.get("Agriculteurs") + 1);
            } else if (user instanceof ResponsableExploitation) {
                distribution.put("Responsables", distribution.get("Responsables") + 1);
            } else if (user instanceof Administrateur) {
                distribution.put("Administrateurs", distribution.get("Administrateurs") + 1);
            }
        }

        return distribution;
    }

    /**
     * Comptes récemment créés (5 derniers)
     */
    public List<Utilisateur> getRecentUsers(int limit) {
        List<Utilisateur> allUsers = utilisateurService.getAll();

        return allUsers.stream()
                .sorted((u1, u2) -> u2.getDateCreation().compareTo(u1.getDateCreation()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Comptes en attente de validation
     */
    public List<Utilisateur> getPendingUsers() {
        List<Utilisateur> allUsers = utilisateurService.getAll();

        return allUsers.stream()
                .filter(u -> !u.isActivated())
                .sorted((u1, u2) -> u2.getDateCreation().compareTo(u1.getDateCreation()))
                .collect(Collectors.toList());
    }

    /**
     * Taux d'activation (pourcentage)
     */
    public double getActivationRate() {
        DashboardStats stats = getMainStats();
        if (stats.totalUsers == 0) return 0;
        return (stats.activeUsers * 100.0) / stats.totalUsers;
    }

    /**
     * Classe pour stocker les statistiques principales
     */
    public static class DashboardStats {
        public int totalUsers = 0;
        public int activeUsers = 0;
        public int pendingUsers = 0;
        public int totalAgriculteurs = 0;
        public int totalResponsables = 0;
        public int totalAdmins = 0;
        public int newUsersLast7Days = 0;

        @Override
        public String toString() {
            return "DashboardStats{" +
                    "total=" + totalUsers +
                    ", actifs=" + activeUsers +
                    ", en attente=" + pendingUsers +
                    ", agriculteurs=" + totalAgriculteurs +
                    ", responsables=" + totalResponsables +
                    ", admins=" + totalAdmins +
                    ", nouveaux 7j=" + newUsersLast7Days +
                    '}';
        }
    }
}