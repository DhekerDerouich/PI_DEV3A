package tn.esprit.farmvision.com.pi.controller;

import tn.esprit.farmvision.com.pi.service.AlertesService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class AlertesController {

    @FXML private ListView<AlertesService.Alerte> alertesListView;
    @FXML private Label compteurAlertes;

    private AlertesService alertesService = new AlertesService();
    private ObservableList<AlertesService.Alerte> alertesObservables = FXCollections.observableArrayList();
    private String filtreActuel = "TOUTES";
    private Timer timer;

    @FXML
    public void initialize() {
        setupListView();
        chargerAlertes();
        demarrerRafraichissementAutomatique();
    }

    private void setupListView() {
        alertesListView.setItems(alertesObservables);

        alertesListView.setCellFactory(listView -> new ListCell<AlertesService.Alerte>() {
            @Override
            protected void updateItem(AlertesService.Alerte alerte, boolean empty) {
                super.updateItem(alerte, empty);

                if (empty || alerte == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    vbox.setStyle("-fx-padding: 10; -fx-background-color: " + getCouleurFond(alerte) + "; -fx-background-radius: 5;");

                    HBox header = new HBox(10);
                    Label icon = new Label(getIcone(alerte));
                    Label titre = new Label(alerte.getTitre());
                    titre.setStyle("-fx-font-weight: bold;");

                    Region spacer = new Region();
                    spacer.setPrefWidth(Double.MAX_VALUE);
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label date = new Label(alerte.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
                    date.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");

                    header.getChildren().addAll(icon, titre, spacer, date);

                    Label message = new Label(alerte.getMessage());
                    message.setWrapText(true);
                    message.setStyle("-fx-font-size: 12px;");

                    vbox.getChildren().addAll(header, message);

                    if (alerte.getAction() != null) {
                        Button actionBtn = new Button("Voir détails");
                        actionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                        actionBtn.setOnAction(e -> alerte.getAction().run());

                        HBox buttonBox = new HBox(actionBtn);
                        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        vbox.getChildren().add(buttonBox);
                    }

                    setGraphic(vbox);
                }
            }

            private String getCouleurFond(AlertesService.Alerte alerte) {
                switch (alerte.getType()) {
                    case "URGENT": return "#ffebee";
                    case "WARNING": return "#fff3e0";
                    default: return "#ffffff";
                }
            }

            private String getIcone(AlertesService.Alerte alerte) {
                switch (alerte.getType()) {
                    case "URGENT": return "🔴";
                    case "WARNING": return "⚠️";
                    default: return "ℹ️";
                }
            }
        });
    }

    private void chargerAlertes() {
        List<AlertesService.Alerte> toutesAlertes = alertesService.getToutesLesAlertes();

        List<AlertesService.Alerte> filtrees = toutesAlertes.stream()
                .filter(a -> correspondFiltre(a))
                .collect(Collectors.toList());

        alertesObservables.setAll(filtrees);
        compteurAlertes.setText(String.valueOf(toutesAlertes.size()));

        // Afficher une notification système si alertes urgentes
        if (toutesAlertes.stream().anyMatch(a -> "URGENT".equals(a.getType()))) {
            afficherNotificationSysteme();
        }
    }

    private boolean correspondFiltre(AlertesService.Alerte alerte) {
        switch (filtreActuel) {
            case "URGENTES":
                return "URGENT".equals(alerte.getType());
            case "MAINTENANCES":
                return alerte.getTitre().contains("Maintenance");
            default:
                return true;
        }
    }

    private void afficherNotificationSysteme() {
        // Notification discrète en bas à droite
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Alertes urgentes");
            alert.setHeaderText("Des actions urgentes requièrent votre attention");
            alert.setContentText("Consultez le centre de notifications pour plus de détails.");
            alert.showAndWait();
        });
    }

    private void demarrerRafraichissementAutomatique() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> chargerAlertes());
            }
        }, 60000, 60000); // Rafraîchir toutes les minutes
    }

    @FXML
    private void rafraichir() {
        chargerAlertes();
    }

    @FXML
    private void filtrerToutes() {
        filtreActuel = "TOUTES";
        chargerAlertes();
    }

    @FXML
    private void filtrerUrgentes() {
        filtreActuel = "URGENTES";
        chargerAlertes();
    }

    @FXML
    private void filtrerMaintenances() {
        filtreActuel = "MAINTENANCES";
        chargerAlertes();
    }

    @FXML
    private void toutMarquerLu() {
        // Logique pour marquer comme lu
        compteurAlertes.setText("0");
    }
}