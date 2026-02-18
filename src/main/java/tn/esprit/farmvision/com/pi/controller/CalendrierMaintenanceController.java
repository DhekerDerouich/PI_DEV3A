package com.pi.controller;

import com.pi.model.Maintenance;
import com.pi.model.Equipement;
import com.pi.service.MaintenanceService;
import com.pi.service.EquipementService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


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

    private YearMonth currentYearMonth;
    private LocalDate dateSelectionnee;
    private Map<LocalDate, List<Maintenance>> maintenancesParJour = new HashMap<>();
    private Map<LocalDate, VBox> cellulesCalendrier = new HashMap<>();

    private MaintenanceService maintenanceService = new MaintenanceService();
    private EquipementService equipementService = new EquipementService();

    private DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);
    private DateTimeFormatter shortDayFormatter = DateTimeFormatter.ofPattern("dd/MM");

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        dateSelectionnee = LocalDate.now();

        setupFilters();
        setupLegende();
        chargerMaintenances();
        dessinerCalendrier();
        mettreAJourStatistiques();

        // Mettre en surbrillance aujourd'hui
        mettreEnSurbrillanceAujourdhui();

        // Afficher les maintenances d'aujourd'hui par défaut
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
        Label couleurPrev = new Label("🟢");
        couleurPrev.setStyle("-fx-font-size: 16px;");
        Label textPrev = new Label("Maintenance préventive");
        legendePreventive.getChildren().addAll(couleurPrev, textPrev);

        HBox legendeCorrective = new HBox(10);
        legendeCorrective.setAlignment(Pos.CENTER_LEFT);
        Label couleurCorr = new Label("🔴");
        couleurCorr.setStyle("-fx-font-size: 16px;");
        Label textCorr = new Label("Maintenance corrective");
        legendeCorrective.getChildren().addAll(couleurCorr, textCorr);

        HBox legendeAujourdhui = new HBox(10);
        legendeAujourdhui.setAlignment(Pos.CENTER_LEFT);
        Label couleurAuj = new Label("🔵");
        couleurAuj.setStyle("-fx-font-size: 16px;");
        Label textAuj = new Label("Aujourd'hui");
        legendeAujourdhui.getChildren().addAll(couleurAuj, textAuj);

        legendeBox.getChildren().addAll(legendePreventive, legendeCorrective, legendeAujourdhui);
    }

    private void chargerMaintenances() {
        maintenancesParJour.clear();
        cellulesCalendrier.clear();

        List<Maintenance> toutesMaintenances = maintenanceService.getAllMaintenances();

        for (Maintenance m : toutesMaintenances) {
            LocalDate date = m.getDateMaintenance();
            maintenancesParJour.computeIfAbsent(date, k -> new ArrayList<>()).add(m);
        }
    }

    private void dessinerCalendrier() {
        calendrierGrid.getChildren().clear();
        calendrierGrid.getColumnConstraints().clear();
        calendrierGrid.getRowConstraints().clear();
        cellulesCalendrier.clear();

        // Créer 7 colonnes (lundi à dimanche)
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setFillWidth(true);
            calendrierGrid.getColumnConstraints().add(col);
        }

        // Créer 6 lignes
        for (int i = 0; i < 7; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / 7);
            row.setFillHeight(true);
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

            if (i >= 5) { // Weekend
                jourLabel.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #7f8c8d;");
            } else {
                jourLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            }

            calendrierGrid.add(jourLabel, i, 0);
        }

        // Premier jour du mois
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() - 1; // 0 pour lundi

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = dayOfWeek;

        // Ajouter les cellules vides pour les jours avant le premier du mois
        for (int i = 0; i < dayOfWeek; i++) {
            VBox emptyCell = creerCelluleVide();
            calendrierGrid.add(emptyCell, i, row);
            col = i + 1;
        }

        // Ajouter les jours du mois
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

        // Compléter les cellules restantes si nécessaire
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
        VBox cell = new VBox();
        cell.setPadding(new Insets(5));
        cell.setPrefHeight(100);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
        return cell;
    }

    private VBox creerCelluleJour(LocalDate date, int jour) {
        VBox cell = new VBox(5);
        cell.setPadding(new Insets(8));
        cell.setPrefHeight(100);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setOnMouseClicked(e -> selectionnerDate(date));

        // Effet hover
        cell.setOnMouseEntered(e -> {
            if (!date.equals(dateSelectionnee)) {
                cell.setStyle(cell.getStyle() + " -fx-background-color: #f0f7ff;");
            }
        });
        cell.setOnMouseExited(e -> {
            if (!date.equals(dateSelectionnee)) {
                appliquerStyleCellule(cell, date);
            }
        });

        // Style de base
        appliquerStyleCellule(cell, date);

        // Numéro du jour
        Label jourLabel = new Label(String.valueOf(jour));
        jourLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox headerBox = new HBox(jourLabel);
        headerBox.setAlignment(Pos.TOP_RIGHT);
        cell.getChildren().add(headerBox);

        // Maintenances du jour
        List<Maintenance> maintenancesJour = maintenancesParJour.getOrDefault(date, new ArrayList<>());

        if (!maintenancesJour.isEmpty()) {
            VBox maintBox = new VBox(3);

            // Limiter à 3 affichages maximum dans la cellule
            int count = 0;
            for (Maintenance m : maintenancesJour) {
                if (count >= 3) break;

                String type = m.getTypeMaintenance();
                String statut = m.getStatut();

                String icone = "Préventive".equals(type) ? "🛡️" : "🔧";
                String couleur = "Préventive".equals(type) ? "#27ae60" : "#e67e22";

                if ("Réalisée".equals(statut)) {
                    couleur = "#95a5a6";
                }

                Label maintLabel = new Label(icone + " " + m.getDescription().substring(0, Math.min(10, m.getDescription().length())) + "...");
                maintLabel.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 10px;");
                maintLabel.setTooltip(new Tooltip(m.getDescription() + " (" + statut + ")"));
                maintBox.getChildren().add(maintLabel);
                count++;
            }

            // Indicateur de nombre si plus de 3
            if (maintenancesJour.size() > 3) {
                Label plusLabel = new Label("+" + (maintenancesJour.size() - 3) + " autres");
                plusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 9px; -fx-font-style: italic;");
                maintBox.getChildren().add(plusLabel);
            }

            // Indicateur de quantité (badge)
            Label countLabel = new Label(String.valueOf(maintenancesJour.size()));
            boolean hasCorrective = maintenancesJour.stream().anyMatch(m -> "Corrective".equals(m.getTypeMaintenance()));
            String badgeColor = hasCorrective ? "#e74c3c" : "#3498db";

            countLabel.setStyle(
                    "-fx-text-fill: white; -fx-background-color: " + badgeColor +
                            "; -fx-padding: 2 6; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;"
            );
            countLabel.setAlignment(Pos.CENTER);

            HBox footerBox = new HBox(countLabel);
            footerBox.setAlignment(Pos.CENTER_RIGHT);

            cell.getChildren().addAll(maintBox, footerBox);
        }

        return cell;
    }

    private void appliquerStyleCellule(VBox cell, LocalDate date) {
        String style = "-fx-border-color: #ddd; -fx-border-width: 1;";

        if (date.equals(LocalDate.now())) {
            style += " -fx-background-color: #e8f4fd; -fx-border-color: #3498db; -fx-border-width: 2;";
        } else if (date.getDayOfWeek().getValue() > 5) { // Weekend
            style += " -fx-background-color: #f9f9f9;";
        } else {
            style += " -fx-background-color: white;";
        }

        // Si c'est la date sélectionnée
        if (date.equals(dateSelectionnee)) {
            style += " -fx-background-color: #e0f0ff; -fx-border-color: #2980b9; -fx-border-width: 2;";
        }

        cell.setStyle(style);
    }

    private void selectionnerDate(LocalDate date) {
        dateSelectionnee = date;
        dateSelectionneeLabel.setText("📅 " + date.format(dayFormatter));

        // Mettre à jour le style des cellules
        for (Map.Entry<LocalDate, VBox> entry : cellulesCalendrier.entrySet()) {
            appliquerStyleCellule(entry.getValue(), entry.getKey());
        }

        afficherMaintenancesDuJour(date);
        maintenancesJourLabel.setText(String.valueOf(maintenancesParJour.getOrDefault(date, new ArrayList<>()).size()));
    }

    /**
     * Méthode publique pour définir la date sélectionnée (appelée depuis MaintenanceController)
     */
    public void setDateSelectionnee(LocalDate date) {
        this.dateSelectionnee = date;
        selectionnerDate(date);
    }

    private void mettreEnSurbrillanceAujourdhui() {
        // Déjà géré dans appliquerStyleCellule
    }

    private void afficherMaintenancesDuJour(LocalDate date) {
        List<Maintenance> maintenancesJour = maintenancesParJour.getOrDefault(date, new ArrayList<>());

        ObservableList<Maintenance> items = FXCollections.observableArrayList(maintenancesJour);
        maintenancesJourListView.setItems(items);

        // Configuration de l'affichage des éléments
        maintenancesJourListView.setCellFactory(lv -> new ListCell<Maintenance>() {
            @Override
            protected void updateItem(Maintenance m, boolean empty) {
                super.updateItem(m, empty);

                if (empty || m == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        Equipement e = equipementService.getEquipementById(m.getEquipementId());
                        String nomEquip = e != null ? e.getNom() : "Inconnu";

                        String icon = "Préventive".equals(m.getTypeMaintenance()) ? "🛡️" : "🔧";
                        String statutIcon = "Réalisée".equals(m.getStatut()) ? "✅" : "⏳";

                        VBox cell = new VBox(3);
                        cell.setPadding(new Insets(8));

                        HBox line1 = new HBox(10);
                        Label typeLabel = new Label(icon + " " + m.getTypeMaintenance());
                        typeLabel.setStyle("-fx-font-weight: bold;");
                        Label statutLabel = new Label(statutIcon + " " + m.getStatut());
                        statutLabel.setStyle("-fx-text-fill: " + ("Réalisée".equals(m.getStatut()) ? "#2ecc71" : "#f39c12") + ";");

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        Label equipLabel = new Label(nomEquip);
                        equipLabel.setStyle("-fx-text-fill: #7f8c8d;");

                        line1.getChildren().addAll(typeLabel, statutLabel, spacer, equipLabel);

                        Label descLabel = new Label(m.getDescription());
                        descLabel.setWrapText(true);
                        descLabel.setStyle("-fx-font-size: 11px;");

                        HBox line3 = new HBox(10);
                        Label dateLabel = new Label("📅 " + m.getDateMaintenance().format(shortDayFormatter));
                        dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");

                        Label coutLabel = new Label(String.format("💰 %.2f DT", m.getCout()));
                        coutLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 10px; -fx-font-weight: bold;");

                        line3.getChildren().addAll(dateLabel, coutLabel);

                        cell.getChildren().addAll(line1, descLabel, line3);

                        // Style de fond selon le type
                        String bgColor = "Préventive".equals(m.getTypeMaintenance()) ? "#e8f5e9" : "#ffebee";
                        cell.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-padding: 8;");

                        setGraphic(cell);

                    } catch (Exception ex) {
                        setText(m.getTypeMaintenance() + " - ID: " + m.getEquipementId());
                    }
                }
            }
        });
    }

    private void filtrerMaintenances() {
        String typeFiltre = filtreTypeCombo.getValue();
        String statutFiltre = filtreStatutCombo.getValue();
        String equipFiltre = filtreEquipementCombo.getValue();

        // Recharger les maintenances
        List<Maintenance> toutesMaintenances = maintenanceService.getAllMaintenances();
        maintenancesParJour.clear();

        // Appliquer les filtres
        for (Maintenance m : toutesMaintenances) {
            boolean correspondType = "Tous".equals(typeFiltre) || m.getTypeMaintenance().equals(typeFiltre);
            boolean correspondStatut = "Tous".equals(statutFiltre) || m.getStatut().equals(statutFiltre);
            boolean correspondEquip = true;

            if (!"Tous".equals(equipFiltre)) {
                int equipId = Integer.parseInt(equipFiltre.split(" - ")[0]);
                correspondEquip = m.getEquipementId() == equipId;
            }

            if (correspondType && correspondStatut && correspondEquip) {
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
                .filter(m -> YearMonth.from(m.getDateMaintenance()).equals(currentYearMonth))
                .collect(Collectors.toList());

        totalMaintenancesLabel.setText(String.valueOf(toutes.size()));

        double coutMois = maintenancesMois.stream().mapToDouble(Maintenance::getCout).sum();
        coutMoisLabel.setText(String.format("%.2f DT", coutMois));

        maintenancesMoisLabel.setText(String.valueOf(maintenancesMois.size()));
    }

    @FXML
    private void moisPrecedent() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        filtrerMaintenances();
    }

    @FXML
    private void moisSuivant() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        filtrerMaintenances();
    }

    @FXML
    private void aujourdhui() {
        currentYearMonth = YearMonth.now();
        dateSelectionnee = LocalDate.now();
        filtrerMaintenances();
        selectionnerDate(dateSelectionnee);
    }

    @FXML
    private void ajouterMaintenance() {
        try {
            // Ouvrir dialogue d'ajout avec date présélectionnée
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/view/maintenance.fxml"));
            Parent root = loader.load();

            MaintenanceController controller = loader.getController();
            controller.setDatePreselectionnee(dateSelectionnee);
            controller.showAddDialog();

            // Rafraîchir après ajout
            chargerMaintenances();
            filtrerMaintenances();

        } catch (Exception e) {
            e.printStackTrace();
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