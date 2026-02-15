package Finance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainAppF extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Updated path to match your folder structure
        Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/view/MainView.fxml")
        ));

        primaryStage.setTitle("Gestion Financière - FarmVision");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}