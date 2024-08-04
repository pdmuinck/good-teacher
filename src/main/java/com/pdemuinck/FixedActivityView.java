package com.pdemuinck;

import com.pdemuinck.views.ActivitySpot;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FixedActivityView extends VBox {

  private String name;

  List<ActivitySpot> spots;

  private ActivityService activityService;
  private UserService userService;
  private ClassroomController classroomController;

  public FixedActivityView(String name, String imageUrl, int spots, ActivityService activityService,
                           UserService userService, ClassroomController classroomController) {
    this.activityService = activityService;
    this.userService = userService;
    this.classroomController = classroomController;
    this.name = name;
    Image image = new Image(getClass().getClassLoader().getResourceAsStream("icons/empty_box.png"));
    this.spots = IntStream.range(0, spots).boxed().map(x -> new ActivitySpot(classroomController, userService, activityService, name, image, x)).collect(
        Collectors.toList());
    if (!imageUrl.isBlank()) {
      Image icon = null;
      icon = new Image(imageUrl, 150, 150, false, false);
      ImageView activityImage = new ImageView(icon);
      activityImage.setId("image_for" + name);
      super.getChildren().add(activityImage);
    }
    super.getChildren().add(fillSpotPane());
    super.getChildren().add(blackList());
  }

  private Node blackList() {
    ColorAdjust monochrome = new ColorAdjust();
    monochrome.setSaturation(-1);
    GridPane gridPane = new GridPane();
    gridPane.setId("blacklist_for_" + this.name);
    List<ImageView> blackList =
        activityService.fetchBlackList(this.name).stream().map(userService::fetchUserByName)
            .filter(Optional::isPresent)
            .map(Optional::get).map(u -> {
              ImageView imageView = new ImageView(
                  new Image(u.getAvatar(), 55, 55, false, false));
              imageView.setEffect(monochrome);
              return imageView;
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
}
