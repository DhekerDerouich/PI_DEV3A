package tn.esprit.farmvision.gestionParcelleEtCulture.util;

public enum GrowthStatus {
    NEW("🆕 Nouvelle"),
    GROWING("🌱 En croissance"),
    READY("🔔 Prête à récolter"),
    HARVESTED("🌾 Récoltée");

    private final String displayName;

    GrowthStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}