package com.pdemuinck;


import atlantafx.base.theme.PrimerLight;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  public static ClassroomController classroomController;

  public static Stage stage;

  @Override
  public void start(Stage stage) throws IOException {
    this.stage = stage;
    Session.init();
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("activities.fxml"));
    DataStore dataStore = new FileDataStore();
    ClassroomController controller = new ClassroomController(new ActivityMockService(dataStore), new UserMockService(dataStore), new FileSystemService());
    loader.setController(controller);
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

