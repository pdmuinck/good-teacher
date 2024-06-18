package com.pdemuinck;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ClassroomController implements Initializable {

  @FXML
  private GridPane activitiesPane;

  @FXML
  private VBox kids;

  @FXML
  private Button start;

  @FXML
  private Label changelog;

  private ActivityService activityService = new ActivityMockService();
  private UserService userService = new UserMockService();
  List<UserView> userViews = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<ActivityView> activities =
        activityService.fetchActivities().stream()
            .map(a -> new ActivityView(a.getName(), a.getMaxSpots()))
            .collect(
                Collectors.toList());
    for (int i = 0; i < activities.size(); i += 4) {
      activitiesPane.add(activities.get(i), i, 0, 1, 1);
      if (i + 1 != activities.size()) {
        activitiesPane.add(activities.get(i + 1), i + 1, 0, 1, 1);
      }
      if (i + 2 < activities.size()) {
        activitiesPane.add(activities.get(i + 2), i + 2, 0, 1, 1);
      }
      if (i + 3 < activities.size()) {
        activitiesPane.add(activities.get(i + 3), i + 3, 0, 1, 1);
      }
    }
    activitiesPane.setHgap(10);
    userViews = userService.fetchUsers().stream().map(k -> new UserView(k.getAvatar()))
        .filter(k -> !k.isHide()).collect(
            Collectors.toList());
    kids.getChildren().addAll(userViews);
  }

  @FXML
  public void startAllActivities(MouseEvent event) {
    activityService.startAllActivities();
  }

  public void updateActivityChange(String change) {
    this.changelog.setText(String.join("\n", this.changelog.getText(), change));
  }

  @FXML
  public void reset(DragEvent event) {
    if (event.getTransferMode() == null) {
      String avatar = event.getDragboard().getString();
      this.userViews.stream().filter(uv -> uv.getAvatar().equals(avatar)).findFirst()
          .ifPresent(uv -> uv.reset(avatar));
    }
  }
}
