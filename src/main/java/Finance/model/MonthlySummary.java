package Finance.model;

public class MonthlySummary {
    private String month;
    private double totalRevenue;
    private double totalExpense;
    private double profit;

    public MonthlySummary() {}

    public MonthlySummary(String month, double totalRevenue, double totalExpense, double profit) {
        this.month = month;
        this.totalRevenue = totalRevenue;
        this.totalExpense = totalExpense;
        this.profit = profit;
    }

    // Getters et setters
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
    public double getProfit() { return profit; }
    public void setProfit(double profit) { this.profit = profit; }
}