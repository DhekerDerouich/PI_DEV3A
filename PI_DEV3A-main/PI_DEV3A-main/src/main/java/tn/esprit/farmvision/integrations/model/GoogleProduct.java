package tn.esprit.farmvision.integrations.model;

import tn.esprit.farmvision.integrations.model.GoogleInventory.Price;
import java.time.LocalDateTime;

/**
 * Modèle représentant un produit Google Merchant
 */
public class GoogleProduct {
    private String productId;
    private String title;
    private String description;
    private String link;
    private String imageLink;
    private Price price;                    // ← Maintenant reconnu
    private String availability;
    private String condition;
    private String brand;
    private String gtin;
    private String mpn;
    private String googleProductCategory;
    private String productType;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private String customLabel0;
    private String customLabel1;
    private String customLabel2;

    // Constructeurs
    public GoogleProduct() {}

    public GoogleProduct(String productId, String title, Price price) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.condition = "new";
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    // Getters et setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getImageLink() { return imageLink; }
    public void setImageLink(String imageLink) { this.imageLink = imageLink; }

    public Price getPrice() { return price; }
    public void setPrice(Price price) { this.price = price; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }

    public String getMpn() { return mpn; }
    public void setMpn(String mpn) { this.mpn = mpn; }

    public String getGoogleProductCategory() { return googleProductCategory; }
    public void setGoogleProductCategory(String googleProductCategory) { this.googleProductCategory = googleProductCategory; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

    public String getCustomLabel0() { return customLabel0; }
    public void setCustomLabel0(String customLabel0) { this.customLabel0 = customLabel0; }

    public String getCustomLabel1() { return customLabel1; }
    public void setCustomLabel1(String customLabel1) { this.customLabel1 = customLabel1; }

    public String getCustomLabel2() { return customLabel2; }
    public void setCustomLabel2(String customLabel2) { this.customLabel2 = customLabel2; }

    // Méthodes utilitaires
    public boolean isAvailable() {
        return "IN_STOCK".equals(availability);
    }

    public String getFormattedPrice() {
        if (price != null) {
            return price.getFormattedPrice();
        }
        return "N/A";
    }

    @Override
    public String toString() {
        return title + " - " + getFormattedPrice();
    }
}