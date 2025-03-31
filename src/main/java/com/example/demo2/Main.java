package com.example.demo2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;



public class Main extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();

        Controller myController = fxmlLoader.getController();
        Scene mainScene = new Scene(root, 650, 400);

        stage.setScene(mainScene);

        if (myController != null) {
            System.out.println("Is not null");
            myController.setScene(mainScene);
            myController.setStage(stage);
        } else {
            System.err.println("Error!");
        }
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


