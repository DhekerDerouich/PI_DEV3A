package tn.esprit.farmvision.gestionParcelleEtCulture.service;

import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;
import tn.esprit.farmvision.gestionParcelleEtCulture.util.GrowthStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CultureStatusService {

    /**
     * Calculate growth status based on dates
     */
    public GrowthStatus calculateGrowthStatus(Culture culture) {
        if (culture == null || culture.getDateSemis() == null || culture.getDateRecolte() == null) {
            return GrowthStatus.NEW;
        }

        LocalDate today = LocalDate.now();
        LocalDate semis = culture.getDateSemis().toLocalDate();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();

        // If harvest date is in the past -> HARVESTED
        if (today.isAfter(recolte)) {
            return GrowthStatus.HARVESTED;
        }

        // If we haven't reached the planting date yet -> NEW
        else if (today.isBefore(semis)) {
            return GrowthStatus.NEW;
        }

        // If harvest is today or within next 7 days -> READY
        else if (today.isEqual(recolte) ||
                (today.isAfter(semis) && ChronoUnit.DAYS.between(today, recolte) <= 7)) {
            return GrowthStatus.READY;
        }

        // If we're between planting and the ready period -> GROWING
        else if (today.isAfter(semis) && today.isBefore(recolte)) {
            return GrowthStatus.GROWING;
        }

        // Default fallback
        else {
            return GrowthStatus.NEW;
        }
    }

    /**
     * Check if harvest is tomorrow
     */
    public boolean isHarvestTomorrow(Culture culture) {
        if (culture == null || culture.getDateRecolte() == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();
        return ChronoUnit.DAYS.between(today, recolte) == 1;
    }

    /**
     * Check if harvest is today
     */
    public boolean isHarvestToday(Culture culture) {
        if (culture == null || culture.getDateRecolte() == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();
        return today.isEqual(recolte);
    }

    /**
     * Check if harvest is within next X days
     */
    public boolean isHarvestWithinDays(Culture culture, int days) {
        if (culture == null || culture.getDateRecolte() == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();
        long daysUntil = ChronoUnit.DAYS.between(today, recolte);
        return daysUntil >= 0 && daysUntil <= days;
    }

    /**
     * Get color code for status (for UI)
     */
    public String getStatusColor(GrowthStatus status) {
        switch (status) {
            case NEW: return "#3b82f6";      // Blue
            case GROWING: return "#22c55e";   // Green
            case READY: return "#eab308";     // Yellow
            case HARVESTED: return "#64748b"; // Gray
            default: return "#64748b";
        }
    }

    /**
     * Get days until harvest
     */
    public long getDaysUntilHarvest(Culture culture) {
        if (culture == null || culture.getDateRecolte() == null) return 0;
        LocalDate today = LocalDate.now();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();
        return ChronoUnit.DAYS.between(today, recolte);
    }

    /**
     * Get growth progress percentage (0-100)
     */
    public int getGrowthProgress(Culture culture) {
        if (culture == null || culture.getDateSemis() == null || culture.getDateRecolte() == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate semis = culture.getDateSemis().toLocalDate();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();

        long totalDays = ChronoUnit.DAYS.between(semis, recolte);
        long daysPassed = ChronoUnit.DAYS.between(semis, today);

        if (daysPassed < 0) return 0;
        if (daysPassed > totalDays) return 100;

        return (int) ((daysPassed * 100) / totalDays);
    }

    /**
     * Get status order for sorting
     */
    public int getStatusOrder(GrowthStatus status) {
        switch (status) {
            case READY: return 1;  // Ready first (urgent)
            case GROWING: return 2;
            case NEW: return 3;
            case HARVESTED: return 4;
            default: return 5;
        }
    }

    /**
     * Debug method to check culture status
     */
    public String getStatusDebugInfo(Culture culture) {
        if (culture == null) return "Culture is null";

        LocalDate today = LocalDate.now();
        LocalDate semis = culture.getDateSemis().toLocalDate();
        LocalDate recolte = culture.getDateRecolte().toLocalDate();

        long daysToHarvest = ChronoUnit.DAYS.between(today, recolte);
        long daysSincePlanting = ChronoUnit.DAYS.between(semis, today);

        return String.format(
                "Culture: %s | Today: %s | Semis: %s | Récolte: %s | Days to harvest: %d | Days since planting: %d",
                culture.getNomCulture(), today, semis, recolte, daysToHarvest, daysSincePlanting
        );
    }
}