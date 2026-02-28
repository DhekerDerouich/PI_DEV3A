package tn.esprit.farmvision.integrations.model;

/**
 * Modèle représentant un compte Google Merchant
 */
public class GoogleMerchantAccount {
    private String merchantId;
    private String accountName;
    private String websiteUrl;
    private String country;
    private String language;
    private String currency;
    private AccountStatus status;
    private long totalProducts;
    private long activeProducts;

    public enum AccountStatus {
        ACTIVE("Actif"),
        PENDING("En attente"),
        SUSPENDED("Suspendu"),
        CLOSED("Fermé");

        private final String displayName;

        AccountStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructeurs
    public GoogleMerchantAccount() {}

    public GoogleMerchantAccount(String merchantId, String accountName) {
        this.merchantId = merchantId;
        this.accountName = accountName;
        this.status = AccountStatus.PENDING;
    }

    // Getters et setters
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getActiveProducts() { return activeProducts; }
    public void setActiveProducts(long activeProducts) { this.activeProducts = activeProducts; }

    public long getInactiveProducts() {
        return totalProducts - activeProducts;
    }

    public double getActivePercentage() {
        if (totalProducts == 0) return 0;
        return (activeProducts * 100.0) / totalProducts;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", accountName, merchantId, status.getDisplayName());
    }
}