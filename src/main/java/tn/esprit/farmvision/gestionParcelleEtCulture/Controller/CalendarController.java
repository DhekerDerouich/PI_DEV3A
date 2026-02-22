package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Culture;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.CultureService;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.CultureStatusService;
import tn.esprit.farmvision.gestionParcelleEtCulture.util.GrowthStatus;
import tn.esprit.farmvision.gestionParcelleEtCulture.view.CultureCalendarView;
import java.util.List;

public class CalendarController {

    @FXML private VBox calendarContainer;
    @FXML private Button backToCulturesBtn;


    private CultureService cultureService = new CultureService();
    private CultureStatusService statusService = new CultureStatusService();
    private Runnable onBackToCultures;

    @FXML
    public void initialize() {
        styleBackButton();
        loadCalendar();

    }

    private void styleBackButton() {
        backToCulturesBtn.setOnMouseEntered(e ->
                backToCulturesBtn.setStyle("-fx-background-color: #5a67d8; -fx-background-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);")
        );

        backToCulturesBtn.setOnMouseExited(e ->
                backToCulturesBtn.setStyle("-fx-background-color: #667eea; -fx-background-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);")
        );
    }

    private void loadCalendar() {
        try {
            List<Culture> cultures = cultureService.afficher();
            CultureCalendarView calendarView = new CultureCalendarView(cultures);
            calendarContainer.getChildren().clear();
            calendarContainer.getChildren().add(calendarView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void setOnBackToCultures(Runnable callback) {
        this.onBackToCultures = callback;
    }

    @FXML
    private void backToCultures() {
        if (onBackToCultures != null) {
            onBackToCultures.run();
        }
    }
}