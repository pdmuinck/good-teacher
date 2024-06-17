package com.pdemuinck;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ClassroomController implements Initializable {

  @FXML
  private GridPane activitiesPane;

  @FXML
  private VBox kids;

  private ClassroomService classroomService = new ClassroomService();
  List<KidView> kidViews = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<ActivityView> activities =
        classroomService.fetchActivities().stream().map(a -> createActivityView(a.getName(), a.getMaxSpots()))
            .collect(
                Collectors.toList());
    for(int i = 0 ; i < activities.size(); i += 4){
      activitiesPane.add(activities.get(i), i, 0, 1, 1);
      if(i + 1 != activities.size()){
        activitiesPane.add(activities.get(i + 1), i + 1, 0, 1, 1);
      }
      if(i + 2 < activities.size()){
        activitiesPane.add(activities.get(i + 2), i + 2, 0, 1, 1);
      }
      if(i + 3 < activities.size()){
        activitiesPane.add(activities.get(i + 3), i + 3, 0, 1, 1);
      }
    }
    activitiesPane.setHgap(10); //horizontal gap in pixels => that's what you are asking for
    kidViews = classroomService.fetchKids().stream().map(k -> createKidView(k.getAvatar())).collect(
        Collectors.toList());
    kids.getChildren().addAll(kidViews);
  }

  public ActivityView createActivityView(String name, int spots){
    ActivityView activity = new ActivityView(name, spots);
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
        kidViews.stream().filter(n -> n.getAvatar().equals(db.getString())).findFirst().map(l -> kids.getChildren().remove(l));
        activity.getSpots().stream().filter(s -> s.getUserData().equals("icons/empty_box.png")).findFirst().ifPresent(i -> {
          Image image = new Image(this.getClass().getClassLoader().getResourceAsStream(db.getString()));
          i.setImage(image);
          i.setFitHeight(75);
          i.setFitWidth(75);
        });
        System.out.println("Dropped: " + db.getString());
        event.setDropCompleted(true);
      } else {
        event.setDropCompleted(false);
      }
      event.consume();
    });
    return activity;
  }

  public KidView createKidView(String avatar){
    KidView view = new KidView(avatar);
    view.setOnDragDetected((MouseEvent event) -> {
      System.out.println("drag detected");
      Dragboard db = view.startDragAndDrop(TransferMode.ANY);
      ClipboardContent content = new ClipboardContent();
      content.putString(avatar);
      db.setContent(content);
    });
    view.setOnMouseDragged((MouseEvent event) -> {
      event.setDragDetect(true);
    });
    return view;
  }
}
