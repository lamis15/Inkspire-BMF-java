package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUtils/SigninUser.fxml"));
            Parent root = loader.load();
            Scene sc = new Scene(root);
            stage.setTitle("Ajouter");
            stage.setScene(sc);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();  // print full stack trace to console
        }
    }
}
