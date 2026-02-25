package Finance.model;

import java.time.LocalDateTime;

public class Rapport {
    private int idRapport;
    private String periode;
    private double totalDepenses;
    private double totalRevenus;
    private double profit;
    private String cheminPDF;
    private LocalDateTime dateGeneration;

    public Rapport() {}

    public Rapport(String periode, double totalDepenses, double totalRevenus, double profit, String cheminPDF, LocalDateTime dateGeneration) {
        this.periode = periode;
        this.totalDepenses = totalDepenses;
        this.totalRevenus = totalRevenus;
        this.profit = profit;
        this.cheminPDF = cheminPDF;
        this.dateGeneration = dateGeneration;
    }

    // Getters et setters
    public int getIdRapport() { return idRapport; }
    public void setIdRapport(int idRapport) { this.idRapport = idRapport; }

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }

    public double getTotalDepenses() { return totalDepenses; }
    public void setTotalDepenses(double totalDepenses) { this.totalDepenses = totalDepenses; }

    public double getTotalRevenus() { return totalRevenus; }
    public void setTotalRevenus(double totalRevenus) { this.totalRevenus = totalRevenus; }

    public double getProfit() { return profit; }
    public void setProfit(double profit) { this.profit = profit; }

    public String getCheminPDF() { return cheminPDF; }
    public void setCheminPDF(String cheminPDF) { this.cheminPDF = cheminPDF; }

    public LocalDateTime getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDateTime dateGeneration) { this.dateGeneration = dateGeneration; }
}