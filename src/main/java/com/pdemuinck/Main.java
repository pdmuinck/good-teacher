package com.pdemuinck;


import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("test.fxml"));
    Parent root = loader.load();
    ClassroomController controller = loader.getController();
    controller.addActivity("drawing");
    controller.addActivity("ipad");
    controller.addActivity("blocks");
    controller.addActivity("puzzles");
    controller.addKid("Charlie");
    controller.addKid("Peppa");
    controller.addKid("Lola");
    Scene scene = new Scene(root, 300, 275);
    stage.setTitle("Good Teacher");
    stage.setScene(scene);
    stage.show();

  }

  public static void main(String[] args) {
    launch();
  }

}
