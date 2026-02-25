package Finance.Controller;

import Finance.dao.DepenseDAO;
import Finance.dao.RevenuDAO;
import Finance.model.Depense;
import Finance.model.Revenu;
import Finance.service.PDFEndpointClient;
import Finance.service.aiFinance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class KPIController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private PieChart pieChart;
    @FXML private Label lblStatus;
    @FXML private Label lblAnalysis;
    private aiFinance aiService = new aiFinance();

    private DepenseDAO depenseDAO = new DepenseDAO();
    private RevenuDAO revenuDAO = new RevenuDAO();


    @FXML
    public void initialize() {
        styleCharts();
        loadCharts();
    }

    private void styleCharts() {
        barChart.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        lineChart.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        pieChart.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
    }

    private void loadCharts() {
        try {
            Map<String, Double> depenses = depenseDAO.getMensuelTotals(6);
            Map<String, Double> revenus = revenuDAO.getMensuelTotals(6);

            TreeMap<String, Double> allMonths = new TreeMap<>();
            allMonths.putAll(depenses);
            allMonths.putAll(revenus);

            XYChart.Series<String, Number> seriesDep = new XYChart.Series<>();
            seriesDep.setName("Dépenses");
            XYChart.Series<String, Number> seriesRev = new XYChart.Series<>();
            seriesRev.setName("Revenus");
            XYChart.Series<String, Number> seriesProfit = new XYChart.Series<>();
            seriesProfit.setName("Profit");

            for (String mois : allMonths.keySet()) {
                double dep = depenses.getOrDefault(mois, 0.0);
                double rev = revenus.getOrDefault(mois, 0.0);
                double profit = rev - dep;
                seriesDep.getData().add(new XYChart.Data<>(mois, dep));
                seriesRev.getData().add(new XYChart.Data<>(mois, rev));
                seriesProfit.getData().add(new XYChart.Data<>(mois, profit));
            }

            barChart.getData().addAll(seriesDep, seriesRev);
            lineChart.getData().add(seriesProfit);

            Map<String, Double> categories = depenseDAO.getTotauxByCategorie(6);
            loadPieChart(categories);

            Platform.runLater(() -> {
                styleBarChart();
                styleLineChart();
            });

            lblStatus.setText("✓ Données chargées - " + allMonths.size() + " mois analysés");
        } catch (SQLException e) {
            lblStatus.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPieChart(Map<String, Double> categories) {
        pieChart.getData().clear();
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            pieChart.getData().add(new PieChart.Data(
                    entry.getKey() + " (" + String.format("%.2f", entry.getValue()) + " TND)",
                    entry.getValue()
            ));
        }
        Platform.runLater(() -> {
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-pie-color: " + getColorForIndex(index++) + ";");
                    Tooltip tooltip = new Tooltip(String.format("%s: %.2f TND",
                            data.getName().split("\\(")[0].trim(), data.getPieValue()));
                    tooltip.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        });
    }

    private String getColorForIndex(int index) {
        String[] colors = {"#f43f5e","#8b5cf6","#10b981","#f59e0b","#3b82f6",
                "#ec4899","#14b8a6","#a855f7","#ef4444","#84cc16"};
        return colors[index % colors.length];
    }

    private void styleBarChart() {
        for (int i = 0; i < barChart.getData().size(); i++) {
            XYChart.Series<String, Number> series = barChart.getData().get(i);
            String color = (i == 0) ? "#ef4444" : "#10b981";
            String label = (i == 0) ? "Dépenses" : "Revenus";

            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle(
                            "-fx-bar-fill: " + color + ";" +
                                    "-fx-background-radius: 5 5 0 0;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                    );

                    data.getNode().setOnMouseEntered(e -> {
                        data.getNode().setStyle(
                                "-fx-bar-fill: derive(" + color + ", -10%);" +
                                        "-fx-background-radius: 5 5 0 0;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);" +
                                        "-fx-scale-y: 1.05;"
                        );
                    });

                    data.getNode().setOnMouseExited(e -> {
                        data.getNode().setStyle(
                                "-fx-bar-fill: " + color + ";" +
                                        "-fx-background-radius: 5 5 0 0;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                        );
                    });

                    Tooltip tooltip = new Tooltip(String.format(
                            "%s\n%s: %.2f TND",
                            data.getXValue(),
                            label,
                            data.getYValue().doubleValue()
                    ));
                    tooltip.setStyle(
                            "-fx-background-color: rgba(30, 30, 30, 0.95);" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-size: 13px;" +
                                    "-fx-padding: 10;" +
                                    "-fx-background-radius: 8;"
                    );
                    tooltip.setShowDelay(Duration.millis(100));
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        }
    }

    private void styleLineChart() {
        XYChart.Series<String, Number> series = lineChart.getData().get(0);

        if (series.getNode() != null) {
            series.getNode().setStyle(
                    "-fx-stroke: #3b82f6;" +
                            "-fx-stroke-width: 3px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 5, 0, 0, 2);"
            );
        }

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                double value = data.getYValue().doubleValue();
                String color = value >= 0 ? "#10b981" : "#ef4444";

                data.getNode().setStyle(
                        "-fx-background-color: " + color + ", white;" +
                                "-fx-background-insets: 0, 2;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 8px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
                );

                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle(
                            "-fx-background-color: " + color + ", white;" +
                                    "-fx-background-insets: 0, 2;" +
                                    "-fx-background-radius: 10px;" +
                                    "-fx-padding: 10px;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);" +
                                    "-fx-scale-x: 1.2;" +
                                    "-fx-scale-y: 1.2;"
                    );
                });

                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle(
                            "-fx-background-color: " + color + ", white;" +
                                    "-fx-background-insets: 0, 2;" +
                                    "-fx-background-radius: 8px;" +
                                    "-fx-padding: 8px;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
                    );
                });

                Tooltip tooltip = new Tooltip(String.format(
                        "%s\n%s: %.2f TND",
                        data.getXValue(),
                        value >= 0 ? "Bénéfice ✓" : "Perte ⚠",
                        Math.abs(value)
                ));
                tooltip.setStyle(
                        "-fx-background-color: rgba(30, 30, 30, 0.95);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 13px;" +
                                "-fx-padding: 10;" +
                                "-fx-background-radius: 8;"
                );
                tooltip.setShowDelay(Duration.millis(100));
                Tooltip.install(data.getNode(), tooltip);
            }
        }
    }

    // ========== EXPORT PDF SANS GRAPHIQUES (CORRIGÉ) ==========

    private String generateHTMLReport() throws SQLException {
        String periode = YearMonth.now().toString();
        double totalDep = depenseDAO.getTotalByPeriode(periode);
        double totalRev = revenuDAO.getTotalByPeriode(periode);
        double profit = totalRev - totalDep;

        List<Depense> depenses = depenseDAO.getAllDepenses();
        List<Revenu> revenus = revenuDAO.getAllRevenus();

        StringBuilder tableRows = new StringBuilder();
        int count = 0;
        for (Depense d : depenses) {
            if (count++ >= 5) break;
            tableRows.append("<tr>")
                    .append("<td>").append(d.getIdDepense()).append("</td>")
                    .append("<td>Dépense</td>")
                    .append("<td>").append(d.getDescription() != null ? d.getDescription() : "-").append("</td>")
                    .append("<td>").append(String.format("%.2f TND", d.getMontant())).append("</td>")
                    .append("<td>").append(d.getDateDepense()).append("</td>")
                    .append("</tr>");
        }
        for (Revenu r : revenus) {
            if (count++ >= 10) break;
            tableRows.append("<tr>")
                    .append("<td>").append(r.getIdRevenu()).append("</td>")
                    .append("<td>Revenu</td>")
                    .append("<td>").append(r.getDescription() != null ? r.getDescription() : "-").append("</td>")
                    .append("<td>").append(String.format("%.2f TND", r.getMontant())).append("</td>")
                    .append("<td>").append(r.getDateRevenu()).append("</td>")
                    .append("</tr>");
        }

        String monthYear = YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
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
                    table { width: 100%%; border-collapse: collapse; margin-top: 30px; }
                    th { background: #1a4d2e; color: white; padding: 10px; }
                    td { border: 1px solid #e2e8f0; padding: 8px; }
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
                <h2>Dernières Transactions</h2>
                <table>
                    <tr><th>ID</th><th>Type</th><th>Description</th><th>Montant</th><th>Date</th></tr>
                    %s
                </table>
            </body>
            </html>
            """, monthYear,
                String.format("%.2f TND", totalDep),
                String.format("%.2f TND", totalRev),
                String.format("%.2f TND", profit),
                tableRows.toString());
    }

    @FXML
    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("rapport_financier.pdf");
        File file = fileChooser.showSaveDialog(barChart.getScene().getWindow());

        if (file != null) {
            try {
                String html = generateHTMLReport();
                boolean success = PDFEndpointClient.convertHtmlToPdf(html, file.getAbsolutePath());
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export réussi");
                    alert.setHeaderText(null);
                    alert.setContentText("Le rapport PDF a été généré avec succès !");
                    alert.showAndWait();
                } else {
                    throw new Exception("Échec de la conversion PDF");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de l'export PDF");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
    @FXML
    private void handleAIAnalysis() {
        // Afficher immédiatement le message de chargement
        lblAnalysis.setText("🧠 Analyse en cours...");

        new Thread(() -> {
            try {
                String result = aiService.getAnalysis();
                // Revenir sur le thread JavaFX pour mettre à jour l'UI
                javafx.application.Platform.runLater(() -> lblAnalysis.setText(result));
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        lblAnalysis.setText("❌ Erreur lors de l'analyse.")
                );
            }
        }).start();
    }

}