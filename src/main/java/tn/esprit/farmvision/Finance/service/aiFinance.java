package tn.esprit.farmvision.Finance.service;

import tn.esprit.farmvision.Finance.dao.StatsDAO;
import tn.esprit.farmvision.Finance.model.MonthlySummary;
import tn.esprit.farmvision.Finance.service.OpenRouterClient;;
import java.sql.SQLException;
import java.util.List;

public class aiFinance {

    private StatsDAO statsDAO = new StatsDAO();

    public String getAnalysis() throws SQLException, Exception {
        // Récupérer les 3 derniers mois
        List<MonthlySummary> history = statsDAO.getMonthlySummaryLastMonths(3);
        String prompt = buildPrompt(history);
        return OpenRouterClient.askAI(prompt);
    }

    private String buildPrompt(List<MonthlySummary> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un conseiller financier expert pour une exploitation agricole. ");
        sb.append("Voici les données financières des 3 derniers mois (revenus, dépenses, profit en TND) :\n\n");

        for (MonthlySummary data : history) {
            sb.append(String.format("- %s : Revenus = %.2f, Dépenses = %.2f, Profit = %.2f\n",
                    data.getMonth(), data.getTotalRevenue(), data.getTotalExpense(), data.getProfit()));
        }

        sb.append("\nSur la base de cette tendance, réponds aux questions suivantes :\n");
        sb.append("1. Quelle est la performance financière générale ?\n");
        sb.append("2. Le mois prochain, l'entreprise va-t-elle réaliser un profit ou une perte ? Estime un montant approximatif.\n");
        sb.append("3. Quels sont les risques ou opportunités que tu observes ?\n");
        sb.append("4. Donne trois recommandations concrètes pour améliorer la santé financière.\n\n");
        sb.append("Rédige une réponse structurée et professionnelle en français.");

        return sb.toString();
    }
}