package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button parcelleBtn;
    @FXML private Button cultureBtn;

    private Node parcelleView;
    private Node cultureView;

    @FXML
    public void initialize() {
        try {
            // Load both views once and keep them in memory
            FXMLLoader parcelleLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/ParcelleView.fxml"));
            parcelleView = parcelleLoader.load();

            FXMLLoader cultureLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/CultureView.fxml"));
            cultureView = cultureLoader.load();

            contentArea.getChildren().addAll(parcelleView, cultureView);
            showParcelle(); // default view
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showParcelle() {
        parcelleView.setVisible(true);
        cultureView.setVisible(false);
        // Optional: style active button
        parcelleBtn.getStyleClass().add("active");
        cultureBtn.getStyleClass().remove("active");
    }

    @FXML
    private void showCulture() {
        parcelleView.setVisible(false);
        cultureView.setVisible(true);
        cultureBtn.getStyleClass().add("active");
        parcelleBtn.getStyleClass().remove("active");
    }
}