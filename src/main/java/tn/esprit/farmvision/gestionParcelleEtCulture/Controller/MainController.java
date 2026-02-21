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
    @FXML private Button chatBtn;

    private Node parcelleView;
    private Node cultureView;
    private Node chatView;

    @FXML
    public void initialize() {
        try {
            // Load both views once and keep them in memory
            FXMLLoader parcelleLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/ParcelleView.fxml"));
            parcelleView = parcelleLoader.load();

            FXMLLoader cultureLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/CultureView.fxml"));
            cultureView = cultureLoader.load();

            FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/ChatFXML.fxml"));
            chatView = chatLoader.load();

            contentArea.getChildren().addAll(parcelleView, cultureView, chatView);

            showParcelle(); // default view
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showParcelle() {
        parcelleView.setVisible(true);
        cultureView.setVisible(false);
        chatView.setVisible(false);
        parcelleBtn.getStyleClass().add("active");
        cultureBtn.getStyleClass().remove("active");
    }

    @FXML
    private void showCulture() {
        parcelleView.setVisible(false);
        cultureView.setVisible(true);
        chatView.setVisible(false);
        cultureBtn.getStyleClass().add("active");
        parcelleBtn.getStyleClass().remove("active");
    }
    @FXML
    private void showChat() {
        parcelleView.setVisible(false);
        cultureView.setVisible(false);
        chatView.setVisible(true);
        chatBtn.getStyleClass().add("active");
        parcelleBtn.getStyleClass().remove("active");
        cultureBtn.getStyleClass().remove("active");

    }
}