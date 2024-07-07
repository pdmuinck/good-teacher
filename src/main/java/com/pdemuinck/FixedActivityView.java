package com.pdemuinck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FixedActivityView extends VBox {

  private String name;

  List<ImageView> spots;

  private ActivityService activityService = new ActivityMockService(new FileDataStore());
  private UserService userService = new UserMockService(new FileDataStore());

  public FixedActivityView(String name, String imageUrl, int spots) {
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
      Dragboard db = imageView1.startDragAndDrop(TransferMode.ANY);
      db.setDragView(imageView1.getImage());
      ClipboardContent content = new ClipboardContent();
      content.putString((String) imageView1.getUserData());
      db.setContent(content);
      activityService.leaveActivity(this.name, (String) imageView1.getUserData());
      imageView1.setImage(basic);
      imageView1.setUserData("icons/empty_box.png");
    });
    imageView1.setOnDragOver((DragEvent event) -> {
      if (event.getGestureSource() != imageView1 && event.getDragboard().hasString() &&
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
            image2 = new Image(new FileInputStream(db.getString().split(",")[1]), 75, 75, false, false);
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
