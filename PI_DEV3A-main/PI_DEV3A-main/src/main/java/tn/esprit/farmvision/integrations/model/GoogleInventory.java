package tn.esprit.farmvision.integrations.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un inventaire Google Merchant
 */
public class GoogleInventory {
    private String productId;
    private String storeCode;
    private String availability;
    private int availabilityCount;
    private Price price;
    private List<String> regions;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ProductInfo product;
    private String expirationDate;

    // Constructeurs
    public GoogleInventory() {
        this.regions = new ArrayList<>();
    }

    public GoogleInventory(String productId, String storeCode) {
        this.productId = productId;
        this.storeCode = storeCode;
        this.regions = new ArrayList<>();
    }

    // Classes internes
    public static class Price {
        private long amountMicros; // Prix en micros (1 DT = 1,000,000 micros)
        private String currencyCode;

        public Price() {}

        public Price(double amount, String currency) {
            this.amountMicros = (long) (amount * 1000000);
            this.currencyCode = currency;
        }

        public double getAmount() {
            return amountMicros / 1000000.0;
        }

        public long getAmountMicros() { return amountMicros; }
        public void setAmountMicros(long amountMicros) { this.amountMicros = amountMicros; }

        public String getCurrencyCode() { return currencyCode; }
        public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

        public String getFormattedPrice() {
            return String.format("%.3f %s", getAmount(), currencyCode);
        }
    }

    public static class ProductInfo {
        private String title;
        private String description;
        private String brand;
        private String gtin;
        private String mpn;
        private List<String> productTypes;
        private String imageLink;
        private String condition;

        public ProductInfo() {
            this.productTypes = new ArrayList<>();
            this.condition = "new";
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getGtin() { return gtin; }
        public void setGtin(String gtin) { this.gtin = gtin; }

        public String getMpn() { return mpn; }
        public void setMpn(String mpn) { this.mpn = mpn; }

        public List<String> getProductTypes() { return productTypes; }
        public void setProductTypes(List<String> productTypes) { this.productTypes = productTypes; }

        public String getImageLink() { return imageLink; }
        public void setImageLink(String imageLink) { this.imageLink = imageLink; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getFormattedProductTypes() {
            return String.join(" > ", productTypes);
        }
    }

    // Getters et setters pour GoogleInventory
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public int getAvailabilityCount() { return availabilityCount; }
    public void setAvailabilityCount(int count) { this.availabilityCount = count; }

    public Price getPrice() { return price; }
    public void setPrice(Price price) { this.price = price; }

    public List<String> getRegions() { return regions; }
    public void setRegions(List<String> regions) { this.regions = regions; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    // Méthodes utilitaires
    public boolean isInStock() {
        return "IN_STOCK".equals(availability) && availabilityCount > 0;
    }

    public boolean isOutOfStock() {
        return "OUT_OF_STOCK".equals(availability) || availabilityCount <= 0;
    }

    public boolean isReserved() {
        return "RESERVED".equals(availability);
    }

    public String getAvailabilityDisplay() {
        if (isInStock()) {
            return "✅ En stock (" + availabilityCount + " disponibles)";
        } else if (isReserved()) {
            return "🟠 Réservé";
        } else {
            return "❌ Rupture de stock";
        }
    }

    public boolean isExpired() {
        if (endDateTime != null) {
            return endDateTime.isBefore(LocalDateTime.now());
        }
        return false;
    }

    public String getStatus() {
        if (isExpired()) return "EXPIRÉ";
        if (isOutOfStock()) return "RUPTURE";
        if (isReserved()) return "RÉSERVÉ";
        return "ACTIF";
    }
}