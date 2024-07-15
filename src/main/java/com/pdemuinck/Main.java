package com.pdemuinck;


import atlantafx.base.theme.PrimerLight;
import java.io.IOException;
import java.util.Random;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  public static ClassroomController classroomController;

  public static String session;

  public static Stage stage;

  @Override
  public void start(Stage stage) throws IOException {
    this.stage = stage;
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 15;
    Random random = new Random();

    session = random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("activities.fxml"));
    Parent root = loader.load();
    classroomController = loader.getController();
    Scene scene = new Scene(root, 300, 275);
    stage.setTitle("Good Teacher");
    stage.setScene(scene);
    stage.show();

  }

  public static void main(String[] args) {
    launch();
  }

}
