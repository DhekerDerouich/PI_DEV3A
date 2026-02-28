package tn.esprit.farmvision.gestionstock.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;  // IMPORTANT: HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.farmvision.gestionstock.model.Stock;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public boolean exportStocksToPDF(List<Stock> stocks, Stage stage) {
        try {
            // Choix de l'emplacement du fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("rapport_stocks_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return false;
            }

            // Créer le PDF
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Ajouter le contenu
            addContent(document, stocks);

            document.close();
            System.out.println("✅ PDF exporté avec succès: " + file.getAbsolutePath());
            return true;

        } catch (FileNotFoundException e) {
            System.err.println("❌ Erreur création PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void addContent(Document document, List<Stock> stocks) {
        // Titre
        Paragraph title = new Paragraph("FARMVISION - RAPPORT DES STOCKS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(title);

        // Date
        Paragraph dateExport = new Paragraph("Exporté le: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
        document.add(dateExport);

        // Statistiques
        int total = stocks.size();
        double valeurTotale = stocks.stream()
                .mapToDouble(s -> s.getQuantite() * 2.5)
                .sum();
        long expires = stocks.stream()
                .filter(s -> s.getDateExpiration() != null)
                .filter(s -> s.getDateExpiration().isBefore(LocalDate.now()))
                .count();
        long disponibles = stocks.stream()
                .filter(s -> "Disponible".equals(s.getStatut()))
                .count();
        long epuises = stocks.stream()
                .filter(s -> "Épuisé".equals(s.getStatut()))
                .count();

        // Statistiques en ligne
        Paragraph stats = new Paragraph()
                .add("Total: " + total + " produits | ")
                .add("Valeur: " + String.format("%.2f DT", valeurTotale) + " | ")
                .add("Expirés: " + expires + " | ")
                .add("Disponibles: " + disponibles + " | ")
                .add("Épuisés: " + epuises)
                .setMarginBottom(20)
                .setTextAlignment(TextAlignment.LEFT);
        document.add(stats);

        document.add(new Paragraph("\n"));

        // Titre du tableau
        Paragraph tableTitle = new Paragraph("LISTE DÉTAILLÉE DES PRODUITS")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10)
                .setFontColor(ColorConstants.GREEN);
        document.add(tableTitle);

        // Créer le tableau des produits
        float[] columnWidths = {2, 2, 1, 1, 1.5f, 1.5f, 1.5f}; // Sans ID
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        // CORRECTION: Utiliser HorizontalAlignment au lieu de TextAlignment
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // En-têtes du tableau (sans ID)
        String[] headers = {"Produit", "Catégorie", "Qté", "Unité", "Date Entrée", "Expiration", "Statut"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // Remplir le tableau avec les données
        if (stocks.isEmpty()) {
            Cell emptyCell = new Cell(1, 7)
                    .add(new Paragraph("Aucun stock disponible"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(10);
            table.addCell(emptyCell);
        } else {
            for (Stock stock : stocks) {
                table.addCell(createCell(stock.getNomProduit() != null ? stock.getNomProduit() : "-", TextAlignment.LEFT));
                table.addCell(createCell(stock.getTypeProduit() != null ? stock.getTypeProduit() : "-", TextAlignment.LEFT));
                table.addCell(createCell(String.format("%.2f", stock.getQuantite()), TextAlignment.CENTER));
                table.addCell(createCell(stock.getUnite() != null ? stock.getUnite() : "-", TextAlignment.CENTER));

                String dateEntree = stock.getDateEntree() != null ? stock.getDateEntree().format(DATE_FORMATTER) : "-";
                table.addCell(createCell(dateEntree, TextAlignment.CENTER));

                String dateExp = stock.getDateExpiration() != null ? stock.getDateExpiration().format(DATE_FORMATTER) : "-";
                table.addCell(createCell(dateExp, TextAlignment.CENTER));

                String statut = stock.getStatut() != null ? stock.getStatut() : "-";
                Cell statusCell = new Cell().add(new Paragraph(statut));
                statusCell.setTextAlignment(TextAlignment.CENTER);
                statusCell.setPadding(5);

                // Couleurs selon le statut
                if ("Disponible".equals(statut)) {
                    statusCell.setFontColor(ColorConstants.GREEN);
                } else if ("Épuisé".equals(statut)) {
                    statusCell.setFontColor(ColorConstants.RED);
                } else if ("Périmé".equals(statut)) {
                    statusCell.setFontColor(ColorConstants.GRAY);
                } else if ("Réservé".equals(statut)) {
                    statusCell.setFontColor(ColorConstants.ORANGE);
                }

                table.addCell(statusCell);
            }
        }

        document.add(table);

        // Pied de page
        Paragraph footer = new Paragraph("FarmVision - Smart Agriculture - Document généré automatiquement")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setFontColor(ColorConstants.LIGHT_GRAY);
        document.add(footer);
    }

    private Cell createCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "-"))
                .setTextAlignment(alignment)
                .setPadding(5);
    }
}