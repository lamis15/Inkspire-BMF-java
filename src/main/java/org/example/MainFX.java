package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Image image = new Image("file:/C:/xampp/htdocs/images/logo.png");
            stage.getIcons().add(image);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUtils/SigninUser.fxml"));
            Parent root = loader.load();
            Scene sc = new Scene(root);
            stage.setTitle("InkSpire");
            stage.setScene(sc);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
