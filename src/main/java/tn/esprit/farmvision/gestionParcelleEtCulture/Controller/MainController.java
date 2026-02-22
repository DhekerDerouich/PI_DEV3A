package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button parcelleBtn;
    @FXML private Button cultureBtn;
    @FXML private Button chatBtn;

    private Node parcelleView;
    private Node cultureView;
    private Node chatView;
    private CultureController cultureController;

    // Keep track of custom views
    private List<Node> customViews = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            // Load parcelle view
            FXMLLoader parcelleLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/ParcelleView.fxml"));
            parcelleView = parcelleLoader.load();

            // Load culture view and get its controller
            FXMLLoader cultureLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/CultureView.fxml"));
            cultureView = cultureLoader.load();
            cultureController = cultureLoader.getController();
            cultureController.setMainController(this);

            // Load chat view
            FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/ChatFXML.fxml"));
            chatView = chatLoader.load();

            // Add all main views to content area
            contentArea.getChildren().addAll(parcelleView, cultureView, chatView);

            showParcelle(); // default view
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showParcelle() {
        // Remove any custom views
        removeCustomViews();

        // Show parcelle view
        parcelleView.setVisible(true);
        cultureView.setVisible(false);
        chatView.setVisible(false);

        // Update button styles
        updateButtonStyles(parcelleBtn);
    }

    @FXML
    public void showCulture() {
        // Remove any custom views
        removeCustomViews();

        // Show culture view
        parcelleView.setVisible(false);
        cultureView.setVisible(true);
        chatView.setVisible(false);

        // Update button styles
        updateButtonStyles(cultureBtn);

        // Refresh data
        if (cultureController != null) {
            cultureController.loadData();
        }
    }

    @FXML
    private void showChat() {
        // Remove any custom views
        removeCustomViews();

        // Show chat view
        parcelleView.setVisible(false);
        cultureView.setVisible(false);
        chatView.setVisible(true);

        // Update button styles
        updateButtonStyles(chatBtn);
    }

    public void showCustomView(Node view) {
        // Hide all main views
        parcelleView.setVisible(false);
        cultureView.setVisible(false);
        chatView.setVisible(false);

        // Remove any existing custom views
        removeCustomViews();

        // Add and show the new custom view
        contentArea.getChildren().add(view);
        customViews.add(view);
        view.setVisible(true);

        // Remove active class from all navigation buttons
        removeAllActiveStyles();
    }

    private void removeCustomViews() {
        for (Node customView : customViews) {
            contentArea.getChildren().remove(customView);
        }
        customViews.clear();
    }

    private void updateButtonStyles(Button activeButton) {
        // Remove active class from all buttons
        parcelleBtn.getStyleClass().remove("active");
        cultureBtn.getStyleClass().remove("active");
        chatBtn.getStyleClass().remove("active");

        // Add active class to the clicked button
        activeButton.getStyleClass().add("active");
    }

    private void removeAllActiveStyles() {
        parcelleBtn.getStyleClass().remove("active");
        cultureBtn.getStyleClass().remove("active");
        chatBtn.getStyleClass().remove("active");
    }

    // Optional: Add a method to check if we're in a custom view
    public boolean isShowingCustomView() {
        return !customViews.isEmpty();
    }
}