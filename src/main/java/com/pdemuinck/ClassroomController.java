package com.pdemuinck;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;

public class ClassroomController implements Initializable {

  @FXML
  private ScrollPane activitiesPane;

  @FXML
  private ScrollPane kidsPane;

  @FXML
  private TextField name;

  private VBox activities;
  private VBox kids;
  private ClassroomService classroomService = new ClassroomService();
  List<Label> kidsNames = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    activities = new VBox();
    kids = new VBox();
    classroomService.fetchActivities().stream().forEach(a -> addActivity(a.getName()));
    classroomService.fetchKids().stream().forEach(k -> addKid(k.getFirstName()));
    activitiesPane.setContent(activities);
    kidsPane.setContent(kids);
  }

  public void addActivity(String name){
    Label activity = new Label(name);
    activity.setOnDragOver((DragEvent event) -> {
      if (event.getGestureSource() != activity && event.getDragboard().hasString()) {
        event.acceptTransferModes(TransferMode.ANY);
      }
      event.consume();
      System.out.print("Try to join activity");
    });
    activity.setOnDragDropped((DragEvent event) -> {
      Dragboard db = event.getDragboard();
      if (db.hasString()) {
        kidsNames.stream().filter(n -> n.getText().equals(db.getString())).findFirst().map(l -> kids.getChildren().remove(l));
        System.out.println("Dropped: " + db.getString());
        event.setDropCompleted(true);
      } else {
        event.setDropCompleted(false);
      }
      event.consume();
    });
    activities.getChildren().add(activity);
  }

  public void addKid(String name){
    Label aKid = new Label(name);
    aKid.setOnDragDetected((MouseEvent event) -> {
      System.out.println("drag detected");
      Dragboard db = aKid.startDragAndDrop(TransferMode.ANY);
      ClipboardContent content = new ClipboardContent();
      content.putString(name);
      db.setContent(content);
    });
    aKid.setOnMouseDragged((MouseEvent event) -> {
      event.setDragDetect(true);
    });
    kidsNames.add(aKid);
    kids.getChildren().add(aKid);
  }

  @FXML
  public void addKid(KeyEvent event){
    if(event.getCode() == KeyCode.ENTER){
      addKid(name.getText());
    }
  }
}
