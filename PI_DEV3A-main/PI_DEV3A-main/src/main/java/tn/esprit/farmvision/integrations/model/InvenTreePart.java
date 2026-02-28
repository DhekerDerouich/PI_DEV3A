package tn.esprit.farmvision.integrations.model;

import java.time.LocalDate;

/**
 * Modèle représentant une pièce/stock dans InvenTree
 */
public class InvenTreePart {
    private int id;
    private String name;
    private String description;
    private int category;
    private String categoryName;
    private double quantity;
    private String units;
    private double minimumStock;
    private boolean active;
    private boolean trackable;
    private LocalDate expiryDate;
    private String ipn; // Internal Part Number
    private String revision;
    private String keywords;
    private String imageUrl;

    // Constructeurs
    public InvenTreePart() {}

    public InvenTreePart(int id, String name, double quantity, String units) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.units = units;
        this.active = true;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }

    public double getMinimumStock() { return minimumStock; }
    public void setMinimumStock(double minimumStock) { this.minimumStock = minimumStock; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isTrackable() { return trackable; }
    public void setTrackable(boolean trackable) { this.trackable = trackable; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getIpn() { return ipn; }
    public void setIpn(String ipn) { this.ipn = ipn; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Méthodes utilitaires
    public boolean isLowStock() {
        return minimumStock > 0 && quantity < minimumStock;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isNearExpiry(int days) {
        if (expiryDate == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return expiryDate.isBefore(threshold) && !isExpired();
    }

    public String getStockStatus() {
        if (isExpired()) return "EXPIRÉ";
        if (quantity <= 0) return "RUPTURE";
        if (isLowStock()) return "FAIBLE";
        if (isNearExpiry(7)) return "EXPIRE BIENTÔT";
        return "NORMAL";
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d, Stock: %.2f %s)", name, id, quantity, units);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvenTreePart that = (InvenTreePart) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}