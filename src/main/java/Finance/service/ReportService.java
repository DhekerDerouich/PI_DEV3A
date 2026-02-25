package Finance.service;

import Finance.dao.DepenseDAO;
import Finance.dao.RevenuDAO;
import Finance.dao.RapportDAO;
import Finance.model.Rapport;
import Finance.service.PDFEndpointClient;

import java.io.File;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.LocalDateTime;

public class ReportService {

    private DepenseDAO depenseDAO = new DepenseDAO();
    private RevenuDAO revenuDAO = new RevenuDAO();
    private RapportDAO rapportDAO = new RapportDAO();

    public void genererRapportMensuel(YearMonth mois) {
        String periode = mois.toString();

        try {
            double totalDep = depenseDAO.getTotalByPeriode(periode);
            double totalRev = revenuDAO.getTotalByPeriode(periode);

            double profit = totalRev - totalDep;

            Rapport rapport = rapportDAO.findByPeriode(periode);
            boolean isNew = (rapport == null);

            if (isNew) {
                rapport = new Rapport();
                rapport.setPeriode(periode);
            }

            rapport.setTotalDepenses(totalDep);
            rapport.setTotalRevenus(totalRev);
            rapport.setProfit(profit);
            rapport.setDateGeneration(LocalDateTime.now());

            // Générer le HTML du rapport
            String html = genererHTML(rapport);
            String pdfDir = "rapports";
            new File(pdfDir).mkdirs();
            String pdfPath = pdfDir + "/rapport_" + periode + ".pdf";

            if (PDFEndpointClient.convertHtmlToPdf(html, pdfPath)) {
                rapport.setCheminPDF(pdfPath);
            } else {
                System.err.println("Échec génération PDF pour " + periode);
            }

            if (isNew) {
                rapportDAO.insert(rapport);
            } else {
                rapportDAO.update(rapport);
            }

            System.out.println("Rapport enregistré pour " + periode);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String genererHTML(Rapport r) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    h1 { color: #1a4d2e; text-align: center; }
                    .stats { display: flex; justify-content: space-around; margin: 30px 0; }
                    .stat-card { background: #f8fafc; border-radius: 10px; padding: 20px; text-align: center; width: 30%%; }
                    .stat-card h3 { margin: 0; color: #475569; }
                    .stat-card .value { font-size: 24px; font-weight: bold; }
                    .depense { color: #dc2626; }
                    .revenu { color: #1a4d2e; }
                    .profit { color: #3b82f6; }
                </style>
            </head>
            <body>
                <h1>FarmVision – Rapport Financier</h1>
                <p style="text-align:center;">Période : %s</p>
                <div class="stats">
                    <div class="stat-card">
                        <h3>Total Dépenses</h3>
                        <div class="value depense">%s</div>
                    </div>
                    <div class="stat-card">
                        <h3>Total Revenus</h3>
                        <div class="value revenu">%s</div>
                    </div>
                    <div class="stat-card">
                        <h3>Profit</h3>
                        <div class="value profit">%s</div>
                    </div>
                </div>
            </body>
            </html>
            """,
                r.getPeriode().replace("-", " "),
                String.format("%.2f TND", r.getTotalDepenses()),
                String.format("%.2f TND", r.getTotalRevenus()),
                String.format("%.2f TND", r.getProfit())
        );
    }
}