package tn.esprit.farmvision.gestionParcelleEtCulture.view;

import javafx.scene.Node;  // ← THIS IMPORT WAS MISSING
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.CultureStatusService;
import tn.esprit.farmvision.gestionParcelleEtCulture.util.GrowthStatus;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CultureCalendarView extends VBox {

    private final CultureStatusService statusService = new CultureStatusService();
    private YearMonth currentYearMonth;
    private final Label monthLabel;
    private final GridView cultureGrid;
    private final List<Culture> cultures;
    private final Map<LocalDate, List<Culture>> cultureMap;

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy");

    public CultureCalendarView(List<Culture> cultures) {
        this.cultures = cultures;
        this.currentYearMonth = YearMonth.now();
        this.cultureMap = new HashMap<>();
        this.monthLabel = new Label();
        this.cultureGrid = new GridView();

        initializeCultureMap();
        setupUI();
        populateCalendar();
    }

    private void initializeCultureMap() {
        cultureMap.clear();
        for (Culture culture : cultures) {
            LocalDate semis = culture.getDateSemis().toLocalDate();
            LocalDate recolte = culture.getDateRecolte().toLocalDate();

            cultureMap.computeIfAbsent(semis, k -> new ArrayList<>()).add(culture);
            cultureMap.computeIfAbsent(recolte, k -> new ArrayList<>()).add(culture);
        }
    }

    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Header with navigation
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);

        Button prevMonth = new Button("◀");
        prevMonth.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        prevMonth.setOnAction(e -> previousMonth());

        monthLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        monthLabel.setText(currentYearMonth.format(MONTH_FORMAT));

        Button nextMonth = new Button("▶");
        nextMonth.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;");
        nextMonth.setOnAction(e -> nextMonth());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status legend
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER_RIGHT);

        for (GrowthStatus status : GrowthStatus.values()) {
            legend.getChildren().add(createLegendItem(
                    status.getDisplayName(),
                    statusService.getStatusColor(status)
            ));
        }

        header.getChildren().addAll(prevMonth, monthLabel, nextMonth, spacer, legend);

        // Calendar grid
        cultureGrid.setPadding(new Insets(10));
        cultureGrid.setVgap(5);
        cultureGrid.setHgap(5);

        getChildren().addAll(header, cultureGrid);
    }

    private HBox createLegendItem(String text, String color) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);

        Circle dot = new Circle(5, Color.web(color));
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #475569; -fx-font-size: 12;");

        item.getChildren().addAll(dot, label);
        return item;
    }

    private void populateCalendar() {
        cultureGrid.getChildren().clear();
        cultureGrid.resetGrid();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        // Add day headers
        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String day : days) {
            Label dayLabel = new Label(day);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            cultureGrid.add(dayLabel);
        }

        // Add empty cells for days before first of month
        for (int i = 1; i < dayOfWeek; i++) {
            VBox emptyCell = createEmptyCell();
            cultureGrid.add(emptyCell);
        }

        // Add cells for each day of the month
        int daysInMonth = currentYearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date);
            cultureGrid.add(dayCell);
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.setPadding(new Insets(8));
        cell.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        cell.setPrefHeight(100);
        cell.setPrefWidth(120);

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        if (date.equals(LocalDate.now())) {
            cell.setStyle("-fx-background-color: #e6f7ff; -fx-background-radius: 8; " +
                    "-fx-border-color: #22c55e; -fx-border-width: 2; -fx-border-radius: 8;");
            dayNumber.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e;");
        }

        cell.getChildren().add(dayNumber);

        List<Culture> culturesForDate = cultureMap.getOrDefault(date, new ArrayList<>());
        for (Culture culture : culturesForDate) {
            HBox cultureItem = createCultureBadge(culture, date);
            cell.getChildren().add(cultureItem);
        }

        return cell;
    }

    private HBox createCultureBadge(Culture culture, LocalDate date) {
        HBox badge = new HBox(3);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(2, 5, 2, 5));

        String icon = date.equals(culture.getDateSemis().toLocalDate()) ? "🌱" : "🌾";
        String text = culture.getNomCulture();

        GrowthStatus status = statusService.calculateGrowthStatus(culture);
        String color = statusService.getStatusColor(status);

        badge.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 4; " +
                "-fx-border-color: " + color + "; -fx-border-radius: 4; -fx-border-width: 1;");

        Label iconLabel = new Label(icon);
        Label nameLabel = new Label(text);
        nameLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 11;");

        if (text.length() > 8) {
            nameLabel.setText(text.substring(0, 7) + "...");
        }

        long daysUntil = statusService.getDaysUntilHarvest(culture);
        String daysText = daysUntil > 0 ? daysUntil + " jours" :
                (daysUntil == 0 ? "Aujourd'hui" : "Passée");

        Tooltip tooltip = new Tooltip(
                String.format("%s\nSemis: %s\nRécolte: %s\nStatut: %s\nProgrès: %d%%\nJours restants: %s",
                        culture.getNomCulture(),
                        culture.getDateSemis(),
                        culture.getDateRecolte(),
                        status.getDisplayName(),
                        statusService.getGrowthProgress(culture),
                        daysText
                )
        );
        Tooltip.install(badge, tooltip);

        badge.getChildren().addAll(iconLabel, nameLabel);

        return badge;
    }

    private VBox createEmptyCell() {
        VBox cell = new VBox();
        cell.setPrefHeight(100);
        cell.setPrefWidth(120);
        cell.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8;");
        return cell;
    }

    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        monthLabel.setText(currentYearMonth.format(MONTH_FORMAT));
        populateCalendar();
    }

    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        monthLabel.setText(currentYearMonth.format(MONTH_FORMAT));
        populateCalendar();
    }

    private static class GridView extends GridPane {
        private int currentColumn = 0;
        private int currentRow = 0;
        private static final int COLUMN_COUNT = 7;

        public void add(Node child) {
            GridPane.setConstraints(child, currentColumn, currentRow);
            getChildren().add(child);

            currentColumn++;
            if (currentColumn >= COLUMN_COUNT) {
                currentColumn = 0;
                currentRow++;
            }
        }

        public void resetGrid() {
            currentColumn = 0;
            currentRow = 0;
        }
    }
}