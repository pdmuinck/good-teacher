package com.pdemuinck;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class ClassroomController implements Initializable {

  @FXML
  private ScrollPane activitiesPane;

  @FXML
  private ScrollPane kidsPane;

  private VBox activities;
  private VBox kids;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    activities = new VBox();
    kids = new VBox();
    activitiesPane.setContent(activities);
    kidsPane.setContent(kids);
  }

  public void addActivity(String name){
    activities.getChildren().add(new Label(name));
  }

  public void addKid(String name){
    kids.getChildren().add(new Label(name));
  }
}
