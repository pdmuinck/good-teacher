package com.pdemuinck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FixedActivityView extends VBox {

  private String name;

  List<ImageView> spots;

  private ActivityService activityService;
  private UserService userService;

  public FixedActivityView(String name, String imageUrl, int spots, ActivityService activityService,
                           UserService userService) {
    this.activityService = activityService;
    this.userService = userService;
    this.name = name;
    Image image = new Image(getClass().getClassLoader().getResourceAsStream("icons/empty_box.png"));
    this.spots = IntStream.range(0, spots).boxed().map(x -> prepareImageView(image)).collect(
        Collectors.toList());
    if (!imageUrl.isBlank()) {
      Image icon = null;
      try {
        icon = new Image(new FileInputStream(imageUrl), 150, 150, false, false);
        ImageView activityImage = new ImageView(icon);
        super.getChildren().add(activityImage);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    super.getChildren().add(fillSpotPane());
    super.getChildren().add(blackList());
  }

  private Node blackList() {
    ColorAdjust monochrome = new ColorAdjust();
    monochrome.setSaturation(-1);
    GridPane gridPane = new GridPane();
    List<ImageView> blackList =
        activityService.fetchBlackList(this.name).stream().map(userService::fetchUserByName).filter(Optional::isPresent)
            .map(Optional::get).map(u -> {
              try {
                ImageView imageView = new ImageView(
                    new Image(new FileInputStream(u.getAvatar()), 55, 55, false, false));
                imageView.setEffect(monochrome);
                return imageView;
              } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
              }
            }).toList();
    for (int i = 0; i < blackList.size(); i += 3) {
      gridPane.add(blackList.get(i), 0, i, 1, 1);
      if (i + 1 != blackList.size()) {
        gridPane.add(blackList.get(i + 1), 1, i, 1, 1);
      }
      if (i + 2 < blackList.size()) {
        gridPane.add(blackList.get(i + 2), 2, i, 1, 1);
      }
    }
    return gridPane;
  }

  private GridPane fillSpotPane() {
    GridPane gridPane = new GridPane();
    for (int i = 0; i < spots.size(); i += 2) {
      gridPane.add(this.spots.get(i), 0, i / 2, 1, 1);
      if (i + 1 != spots.size()) {
        gridPane.add(this.spots.get(i + 1), 1, i / 2, 1, 1);
      }
    }
    return gridPane;
  }

  private ImageView prepareImageView(Image basic) {
    ImageView imageView1 = new ImageView(basic);
    imageView1.setUserData("icons/empty_box.png");
    imageView1.setOnDragDetected((MouseEvent event) -> {
      if (!imageView1.getUserData().equals("icons/empty_box.png")) {
        Dragboard db = imageView1.startDragAndDrop(TransferMode.ANY);
        db.setDragView(imageView1.getImage());
        ClipboardContent content = new ClipboardContent();
        content.putString((String) imageView1.getUserData());
        db.setContent(content);
        activityService.leaveActivity(this.name, (String) imageView1.getUserData());
        Main.classroomController.updateActivityChange(
            String.format("%s left activity %s", imageView1.getUserData(), name));
        imageView1.setImage(basic);
        imageView1.setUserData("icons/empty_box.png");
      }
    });
    imageView1.setOnDragOver((DragEvent event) -> {
      List<Activity> activities = activityService.getActivities();
      List<String> blackList = activityService.fetchBlackList(this.name);
      if (activities.stream().anyMatch(a -> a.getName().equals(this.name) &&
          (a.getAvailableSpots() == 0 ||
              blackList.contains(event.getDragboard().getString().split(",")[0])))) {
        event.acceptTransferModes(TransferMode.NONE);
      } else if (event.getGestureSource() != imageView1 && event.getDragboard().hasString() &&
          imageView1.getUserData().equals("icons/empty_box.png")) {
        event.acceptTransferModes(TransferMode.ANY);
      }
      event.consume();
    });
    imageView1.setOnDragDropped((DragEvent event) -> {
      Dragboard db = event.getDragboard();
      if (db.hasString()) {
        if (imageView1.getUserData().equals("icons/empty_box.png")) {
          Image image2 =
              null;
          try {
            image2 =
                new Image(new FileInputStream(db.getString().split(",")[1]), 75, 75, false, false);
          } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
          }
          imageView1.setImage(image2);
          imageView1.setPreserveRatio(true);
          imageView1.setUserData(db.getString());

          activityService.joinActivity(name, userService.fetchUsers().stream().filter(
                  u -> u.getName().equals(db.getString().split(",")[0]) &&
                      u.getAvatar().equals(db.getString().split(",")[1]))
              .findFirst().get().getName());
          Main.classroomController.hideUser(db.getString().split(",")[0]);
          Main.classroomController.updateActivityChange(
              String.format("%s joined activity %s", db.getString().split(",")[0],
                  db.getString().split(",")[1]));
        } else {
          event.setDropCompleted(false);
        }
        event.setDropCompleted(true);
      } else {
        event.setDropCompleted(false);
      }
      event.consume();
    });
    return imageView1;
  }
}
