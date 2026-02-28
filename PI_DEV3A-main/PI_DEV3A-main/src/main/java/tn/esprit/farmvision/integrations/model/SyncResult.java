package tn.esprit.farmvision.integrations.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle générique pour les résultats de synchronisation
 */
public class SyncResult {
    private boolean success;
    private int totalItems;
    private int successCount;
    private int failedCount;
    private int created;
    private int updated;
    private String message;
    private List<String> errors;
    private long startTime;
    private long endTime;

    // Constructeurs
    public SyncResult() {
        this.errors = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
    }

    public SyncResult(String message) {
        this();
        this.message = message;
    }

    // Méthodes utilitaires
    public void incrementSuccess() { successCount++; }
    public void incrementFailed() { failedCount++; }
    public void incrementCreated() { created++; successCount++; }
    public void incrementUpdated() { updated++; successCount++; }
    public void addError(String error) { errors.add(error); }

    public void complete() {
        this.endTime = System.currentTimeMillis();
        this.totalItems = successCount + failedCount;
    }

    public long getDuration() {
        if (endTime == 0) return 0;
        return endTime - startTime;
    }

    public String getDurationDisplay() {
        long duration = getDuration();
        if (duration < 1000) {
            return duration + " ms";
        }
        return String.format("%.2f s", duration / 1000.0);
    }

    // Getters et setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public int getCreated() { return created; }
    public void setCreated(int created) { this.created = created; }

    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public double getSuccessRate() {
        if (totalItems == 0) return 0;
        return (successCount * 100.0) / totalItems;
    }

    public String getSummary() {
        return String.format(
                "✅ Succès: %d, ❌ Échecs: %d, ⏱️ %s",
                successCount, failedCount, getDurationDisplay()
        );
    }

    @Override
    public String toString() {
        return String.format(
                "SyncResult{success=%s, total=%d, ok=%d, ko=%d, created=%d, updated=%d, msg='%s'}",
                success, totalItems, successCount, failedCount, created, updated, message
        );
    }
}