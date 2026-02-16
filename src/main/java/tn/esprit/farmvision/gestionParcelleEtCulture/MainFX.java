package tn.esprit.farmvision.gestionParcelleEtCulture;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        System.out.println("TEST RESOURCE:");
        System.out.println(getClass().getResource("/ParcelleEtCultureView/ MainView.fxml"));

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/ParcelleEtCultureView/ MainView.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}