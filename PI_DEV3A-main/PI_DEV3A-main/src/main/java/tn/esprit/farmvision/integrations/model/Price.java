package tn.esprit.farmvision.integrations.model;

/**
 * Modèle représentant un prix pour Google Merchant
 */
public class Price {
    private long amountMicros; // Prix en micros (1 DT = 1,000,000 micros)
    private String currencyCode;

    // Constructeurs
    public Price() {}

    public Price(double amount, String currency) {
        this.amountMicros = (long) (amount * 1000000);
        this.currencyCode = currency;
    }

    public Price(long amountMicros, String currencyCode) {
        this.amountMicros = amountMicros;
        this.currencyCode = currencyCode;
    }

    // Getters et Setters
    public long getAmountMicros() {
        return amountMicros;
    }

    public void setAmountMicros(long amountMicros) {
        this.amountMicros = amountMicros;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    // Méthodes utilitaires
    public double getAmount() {
        return amountMicros / 1000000.0;
    }

    public String getFormattedPrice() {
        return String.format("%.3f %s", getAmount(), currencyCode);
    }

    public Price multiply(double factor) {
        return new Price(this.getAmount() * factor, this.currencyCode);
    }

    public Price add(Price other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException("Cannot add prices with different currencies");
        }
        return new Price(this.getAmount() + other.getAmount(), this.currencyCode);
    }

    @Override
    public String toString() {
        return getFormattedPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return amountMicros == price.amountMicros &&
                currencyCode.equals(price.currencyCode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(amountMicros, currencyCode);
    }
}