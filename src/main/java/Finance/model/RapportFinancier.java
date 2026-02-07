package Finance.model;

public class RapportFinancier {
    private Double totalDepenses;
    private Double totalRevenus;
    private Double profit;

    public RapportFinancier(Double totalDepenses, Double totalRevenus) {
        this.totalDepenses = totalDepenses;
        this.totalRevenus = totalRevenus;
        this.profit = totalRevenus - totalDepenses;
    }

    public Double getTotalDepenses() {
        return totalDepenses;
    }

    public Double getTotalRevenus() {
        return totalRevenus;
    }

    public Double getProfit() {
        return profit;
    }

    @Override
    public String toString() {
        return "RapportFinancier{" +
                "totalDepenses=" + totalDepenses +
                ", totalRevenus=" + totalRevenus +
                ", profit=" + profit +
                '}';
    }
}