package com.pi.controller;

import com.pi.model.Maintenance;
import com.pi.model.Equipement;
import com.pi.service.MaintenanceService;
import com.pi.service.EquipementService;
import com.pi.service.external.SunriseSunsetService;
import com.pi.service.external.SunriseSunsetService.SunriseSunsetData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalendrierMaintenanceController {

    @FXML private Label moisAnneeLabel;
    @FXML private GridPane calendrierGrid;
    @FXML private ListView<Maintenance> maintenancesJourListView;
    @FXML private Label dateSelectionneeLabel;
    @FXML private ComboBox<String> filtreTypeCombo;
    @FXML private ComboBox<String> filtreEquipementCombo;
    @FXML private ComboBox<String> filtreStatutCombo;
    @FXML private Label totalMaintenancesLabel;
    @FXML private Label maintenancesJourLabel;
    @FXML private Label maintenancesMoisLabel;
    @FXML private Label coutMoisLabel;
    @FXML private Button aujourdhuiBtn;
    @FXML private Button moisPrecedentBtn;
    @FXML private Button moisSuivantBtn;
    @FXML private Button ajouterMaintenanceBtn;
    @FXML private VBox legendeBox;
    @FXML private HBox solarInfoBox;

    private YearMonth currentYearMonth;
    private LocalDate dateSelectionnee;
    private Map<LocalDate, List<Maintenance>> maintenancesParJour = new HashMap<>();
    private Map<LocalDate, VBox> cellulesCalendrier = new HashMap<>();

    private MaintenanceService maintenanceService = new MaintenanceService();
    private EquipementService equipementService = new EquipementService();
    private SunriseSunsetService sunriseService = new SunriseSunsetService();

    private DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);
    private DateTimeFormatter shortDayFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        dateSelectionnee = LocalDate.now();

        setupFilters();
        setupLegende();
        chargerMaintenances();
        dessinerCalendrier();
        mettreAJourStatistiques();
        mettreAJourInfosSolaires();
        selectionnerDate(LocalDate.now());
    }

    private void setupFilters() {
        filtreTypeCombo.getItems().addAll("Tous", "Préventive", "Corrective");
        filtreTypeCombo.setValue("Tous");

        filtreStatutCombo.getItems().addAll("Tous", "Planifiée", "Réalisée");
        filtreStatutCombo.setValue("Tous");

        filtreEquipementCombo.getItems().add("Tous");
        equipementService.getAllEquipements().stream()
                .map(e -> e.getId() + " - " + e.getNom())
                .sorted()
                .forEach(filtreEquipementCombo.getItems()::add);
        filtreEquipementCombo.setValue("Tous");

        filtreTypeCombo.valueProperty().addListener((obs, old, val) -> filtrerMaintenances());
        filtreStatutCombo.valueProperty().addListener((obs, old, val) -> filtrerMaintenances());
        filtreEquipementCombo.valueProperty().addListener((obs, old, val) -> filtrerMaintenances());
    }

    private void setupLegende() {
        legendeBox.getChildren().clear();

        HBox legendePreventive = new HBox(10);
        legendePreventive.setAlignment(Pos.CENTER_LEFT);
        Label couleurPrev = new Label("●");
        couleurPrev.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label textPrev = new Label("Maintenance préventive");
        legendePreventive.getChildren().addAll(couleurPrev, textPrev);

        HBox legendeCorrective = new HBox(10);
        legendeCorrective.setAlignment(Pos.CENTER_LEFT);
        Label couleurCorr = new Label("●");
        couleurCorr.setStyle("-fx-text-fill: #f44336; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label textCorr = new Label("Maintenance corrective");
        legendeCorrective.getChildren().addAll(couleurCorr, textCorr);

        HBox legendeAujourdhui = new HBox(10);
        legendeAujourdhui.setAlignment(Pos.CENTER_LEFT);
        Label couleurAuj = new Label("●");
        couleurAuj.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label textAuj = new Label("Aujourd'hui");
        legendeAujourdhui.getChildren().addAll(couleurAuj, textAuj);

        legendeBox.getChildren().addAll(legendePreventive, legendeCorrective, legendeAujourdhui);
    }

    private void mettreAJourInfosSolaires() {
        if (solarInfoBox == null) return;

        solarInfoBox.getChildren().clear();

        SunriseSunsetData data = sunriseService.getSunriseSunsetToday();
        if (data != null) {
            Label icon = new Label("☀️");
            icon.setStyle("-fx-font-size: 18px; -fx-padding: 0 5 0 0;");

            Label leverLabel = new Label("Lever: " + data.getSunrise().format(timeFormatter));
            leverLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-padding: 0 10 0 0;");

            Label coucherLabel = new Label("Coucher: " + data.getSunset().format(timeFormatter));
            coucherLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-padding: 0 10 0 0;");

            Label dureeLabel = new Label("Durée: " + data.getDayLengthFormatted());
            dureeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-padding: 0 10 0 0;");

            solarInfoBox.getChildren().addAll(icon, leverLabel, coucherLabel, dureeLabel);
        } else {
            Label errorLabel = new Label("☀️ Données solaires non disponibles");
            errorLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            solarInfoBox.getChildren().add(errorLabel);
        }
    }

    private void chargerMaintenances() {
        maintenancesParJour.clear();
        cellulesCalendrier.clear();

        List<Maintenance> toutesMaintenances = maintenanceService.getAllMaintenances();

        for (Maintenance m : toutesMaintenances) {
            if (m.getDateMaintenance() != null) {
                LocalDate date = m.getDateMaintenance();
                maintenancesParJour.computeIfAbsent(date, k -> new ArrayList<>()).add(m);
            }
        }
    }

    private void dessinerCalendrier() {
        calendrierGrid.getChildren().clear();
        calendrierGrid.getColumnConstraints().clear();
        calendrierGrid.getRowConstraints().clear();
        cellulesCalendrier.clear();

        // Créer 7 colonnes
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setFillWidth(true);
            calendrierGrid.getColumnConstraints().add(col);
        }

        // Créer 7 lignes (en-tête + 6 semaines)
        for (int i = 0; i < 7; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(100);
            row.setPrefHeight(100);
            row.setVgrow(Priority.ALWAYS);
            calendrierGrid.getRowConstraints().add(row);
        }

        // Ajouter les en-têtes des jours
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label jourLabel = new Label(jours[i]);
            jourLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            jourLabel.setAlignment(Pos.CENTER);
            jourLabel.setPadding(new Insets(10));
            jourLabel.setMaxWidth(Double.MAX_VALUE);
            jourLabel.setMaxHeight(Double.MAX_VALUE);

            if (i >= 5) {
                jourLabel.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #7f8c8d; -fx-border-color: #ddd; -fx-border-width: 0 1 1 0;");
            } else {
                jourLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-border-color: #ddd; -fx-border-width: 0 1 1 0;");
            }

            calendrierGrid.add(jourLabel, i, 0);
        }

        // Premier jour du mois
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() - 1;

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = dayOfWeek;

        // Cellules vides avant le premier jour
        for (int i = 0; i < dayOfWeek; i++) {
            VBox emptyCell = creerCelluleVide();
            calendrierGrid.add(emptyCell, i, row);
        }

        // Jours du mois
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);

            if (col >= 7) {
                col = 0;
                row++;
            }

            VBox dayCell = creerCelluleJour(date, day);
            calendrierGrid.add(dayCell, col, row);
            cellulesCalendrier.put(date, dayCell);

            col++;
        }

        // Remplir les cellules restantes
        while (row < 6) {
            while (col < 7) {
                VBox emptyCell = creerCelluleVide();
                calendrierGrid.add(emptyCell, col, row);
                col++;
            }
            col = 0;
            row++;
        }

        moisAnneeLabel.setText(currentYearMonth.format(monthFormatter));
    }

    private VBox creerCelluleVide() {
        VBox cell = new VBox(5);
        cell.setPadding(new Insets(5));
        cell.setPrefHeight(100);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 1 1 0; -fx-background-color: #fafafa;");
        return cell;
    }

    private VBox creerCelluleJour(LocalDate date, int jour) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(5));
        cell.setPrefHeight(100);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setOnMouseClicked(e -> selectionnerDate(date));

        // Style de base
        String style = "-fx-border-color: #ddd; -fx-border-width: 0 1 1 0;";

        if (date.equals(LocalDate.now())) {
            style += " -fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-width: 2;";
        } else if (date.getDayOfWeek().getValue() > 5) {
            style += " -fx-background-color: #f9f9f9;";
        } else {
            style += " -fx-background-color: white;";
        }

        if (date.equals(dateSelectionnee)) {
            style += " -fx-background-color: #fff3e0; -fx-border-color: #FF9800; -fx-border-width: 2;";
        }

        cell.setStyle(style);

        // Numéro du jour
        Label jourLabel = new Label(String.valueOf(jour));
        jourLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        jourLabel.setAlignment(Pos.TOP_RIGHT);
        jourLabel.setMaxWidth(Double.MAX_VALUE);
        HBox headerBox = new HBox(jourLabel);
        headerBox.setAlignment(Pos.TOP_RIGHT);
        cell.getChildren().add(headerBox);

        // Maintenances du jour
        List<Maintenance> maintenancesJour = maintenancesParJour.getOrDefault(date, new ArrayList<>());

        if (!maintenancesJour.isEmpty()) {
            VBox maintBox = new VBox(2);
            maintBox.setPadding(new Insets(2, 0, 0, 0));

            int count = 0;
            for (Maintenance m : maintenancesJour) {
                if (count >= 2) break;

                String type = m.getTypeMaintenance();
                String couleur = "Préventive".equals(type) ? "#4CAF50" : "#f44336";

                Label maintLabel = new Label("• " + m.getDescription());
                maintLabel.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 10px; -fx-font-weight: bold;");
                maintLabel.setMaxWidth(Double.MAX_VALUE);
                maintBox.getChildren().add(maintLabel);
                count++;
            }

            if (maintenancesJour.size() > 2) {
                Label plusLabel = new Label("+" + (maintenancesJour.size() - 2) + " autres");
                plusLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 9px; -fx-font-style: italic;");
                plusLabel.setMaxWidth(Double.MAX_VALUE);
                maintBox.getChildren().add(plusLabel);
            }

            cell.getChildren().add(maintBox);
        }

        return cell;
    }

    private void selectionnerDate(LocalDate date) {
        dateSelectionnee = date;

        dateSelectionneeLabel.setText("📅 " + date.format(dayFormatter));

        // Mettre à jour le style des cellules
        for (Map.Entry<LocalDate, VBox> entry : cellulesCalendrier.entrySet()) {
            LocalDate d = entry.getKey();
            VBox cell = entry.getValue();

            String style = "-fx-border-color: #ddd; -fx-border-width: 0 1 1 0;";

            if (d.equals(LocalDate.now())) {
                style += " -fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-width: 2;";
            } else if (d.getDayOfWeek().getValue() > 5) {
                style += " -fx-background-color: #f9f9f9;";
            } else {
                style += " -fx-background-color: white;";
            }

            if (d.equals(dateSelectionnee)) {
                style += " -fx-background-color: #fff3e0; -fx-border-color: #FF9800; -fx-border-width: 2;";
            }

            cell.setStyle(style);
        }

        afficherMaintenancesDuJour(date);
        maintenancesJourLabel.setText(String.valueOf(maintenancesParJour.getOrDefault(date, new ArrayList<>()).size()));
    }

    public void setDateSelectionnee(LocalDate date) {
        if (date != null) {
            if (YearMonth.from(date).equals(currentYearMonth)) {
                this.dateSelectionnee = date;
                selectionnerDate(date);
            } else {
                currentYearMonth = YearMonth.from(date);
                this.dateSelectionnee = date;
                chargerMaintenances();
                dessinerCalendrier();
                selectionnerDate(date);
            }
        }
    }

    private void afficherMaintenancesDuJour(LocalDate date) {
        List<Maintenance> maintenancesJour = maintenancesParJour.getOrDefault(date, new ArrayList<>());

        ObservableList<Maintenance> items = FXCollections.observableArrayList(maintenancesJour);
        maintenancesJourListView.setItems(items);

        maintenancesJourListView.setCellFactory(lv -> new ListCell<Maintenance>() {
            @Override
            protected void updateItem(Maintenance m, boolean empty) {
                super.updateItem(m, empty);

                if (empty || m == null) {
                    setGraphic(null);
                } else {
                    try {
                        Equipement e = equipementService.getEquipementById(m.getEquipementId());
                        String nomEquip = e != null ? e.getNom() : "Inconnu";

                        VBox cell = new VBox(5);
                        cell.setPadding(new Insets(10));

                        String bgColor = "Préventive".equals(m.getTypeMaintenance()) ? "#E8F5E9" : "#FFEBEE";
                        cell.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

                        HBox header = new HBox(10);
                        Label typeLabel = new Label(m.getTypeMaintenance());
                        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                                ("Préventive".equals(m.getTypeMaintenance()) ? "#4CAF50" : "#f44336") + ";");

                        Label equipLabel = new Label(nomEquip);
                        equipLabel.setStyle("-fx-text-fill: #757575;");

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        Label statutLabel = new Label(m.getStatut());
                        statutLabel.setStyle("-fx-text-fill: " +
                                ("Réalisée".equals(m.getStatut()) ? "#4CAF50" : "#FF9800") + "; -fx-font-weight: bold;");

                        header.getChildren().addAll(typeLabel, equipLabel, spacer, statutLabel);

                        Label descLabel = new Label(m.getDescription());
                        descLabel.setWrapText(true);
                        descLabel.setStyle("-fx-font-size: 12px;");

                        HBox footer = new HBox(15);
                        Label dateLabel = new Label("📅 " + m.getDateMaintenance().format(shortDayFormatter));
                        dateLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");

                        Label coutLabel = new Label(String.format("💰 %.2f DT", m.getCout()));
                        coutLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11px; -fx-font-weight: bold;");

                        footer.getChildren().addAll(dateLabel, coutLabel);

                        cell.getChildren().addAll(header, descLabel, footer);
                        setGraphic(cell);

                    } catch (Exception ex) {
                        setText(m.getTypeMaintenance() + " - " + m.getDescription());
                    }
                }
            }
        });
    }

    private void filtrerMaintenances() {
        String typeFiltre = filtreTypeCombo.getValue();
        String statutFiltre = filtreStatutCombo.getValue();
        String equipFiltre = filtreEquipementCombo.getValue();

        List<Maintenance> toutesMaintenances = maintenanceService.getAllMaintenances();
        maintenancesParJour.clear();

        for (Maintenance m : toutesMaintenances) {
            boolean correspondType = "Tous".equals(typeFiltre) || m.getTypeMaintenance().equals(typeFiltre);
            boolean correspondStatut = "Tous".equals(statutFiltre) || m.getStatut().equals(statutFiltre);
            boolean correspondEquip = true;

            if (!"Tous".equals(equipFiltre)) {
                try {
                    int equipId = Integer.parseInt(equipFiltre.split(" - ")[0]);
                    correspondEquip = m.getEquipementId() == equipId;
                } catch (NumberFormatException e) {
                    correspondEquip = false;
                }
            }

            if (correspondType && correspondStatut && correspondEquip && m.getDateMaintenance() != null) {
                LocalDate date = m.getDateMaintenance();
                maintenancesParJour.computeIfAbsent(date, k -> new ArrayList<>()).add(m);
            }
        }

        dessinerCalendrier();
        afficherMaintenancesDuJour(dateSelectionnee);
        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        List<Maintenance> toutes = maintenanceService.getAllMaintenances();
        List<Maintenance> maintenancesMois = toutes.stream()
                .filter(m -> m.getDateMaintenance() != null && YearMonth.from(m.getDateMaintenance()).equals(currentYearMonth))
                .collect(Collectors.toList());

        totalMaintenancesLabel.setText(String.valueOf(toutes.size()));

        double coutMois = maintenancesMois.stream().mapToDouble(Maintenance::getCout).sum();
        coutMoisLabel.setText(String.format("%.2f DT", coutMois));

        maintenancesMoisLabel.setText(String.valueOf(maintenancesMois.size()));
    }

    @FXML
    private void moisPrecedent() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        chargerMaintenances();
        dessinerCalendrier();
        mettreAJourStatistiques();
        mettreAJourInfosSolaires();
    }

    @FXML
    private void moisSuivant() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        chargerMaintenances();
        dessinerCalendrier();
        mettreAJourStatistiques();
        mettreAJourInfosSolaires();
    }

    @FXML
    private void aujourdhui() {
        currentYearMonth = YearMonth.now();
        dateSelectionnee = LocalDate.now();
        chargerMaintenances();
        dessinerCalendrier();
        selectionnerDate(dateSelectionnee);
        mettreAJourStatistiques();
        mettreAJourInfosSolaires();
    }

    @FXML
    private void ajouterMaintenance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/view/maintenance.fxml"));
            Parent root = loader.load();

            MaintenanceController controller = loader.getController();
            controller.setDatePreselectionnee(dateSelectionnee);

            Stage stage = new Stage();
            stage.setTitle("Planifier une maintenance");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            chargerMaintenances();
            dessinerCalendrier();
            afficherMaintenancesDuJour(dateSelectionnee);
            mettreAJourStatistiques();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la fenêtre d'ajout");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void exporterCalendrier() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Export du calendrier");
        alert.setContentText("Fonctionnalité d'export à venir...");
        alert.showAndWait();
    }

    @FXML
    private void imprimerCalendrier() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Impression");
        alert.setHeaderText("Impression du calendrier");
        alert.setContentText("Fonctionnalité d'impression à venir...");
        alert.showAndWait();
    }
}