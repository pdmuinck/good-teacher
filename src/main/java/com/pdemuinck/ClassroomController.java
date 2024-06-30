package com.pdemuinck;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

  @FXML
  private TextField newActivity;

  @FXML
  private TextField newUser;

  private ActivityService activityService = new ActivityMockService(new FileDataStore());
  private UserService userService = new UserMockService(new FileDataStore());
  List<UserView> userViews = new ArrayList<>();
  List<ActivityView> activityViews = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<Activity> activities = activityService.fetchActivities();
    this.activityViews = activities.stream().map(a -> new ActivityView(a.getName(), a.getImageUrl(), a.getMaxSpots())).collect(
        Collectors.toList());
    activitiesPane.setHgap(10);
    fillActivitiesPane();
    userViews = userService.fetchUsers().stream().map(k -> new UserView(k.getName(), k.getAvatar())).collect(
            Collectors.toList());
    kids.getChildren().addAll(userViews);
  }

  private void fillActivitiesPane() {
    activitiesPane.getChildren().clear();
    for (int i = 0; i < activityViews.size(); i += 5) {
      activitiesPane.add(activityViews.get(i), 0, i, 1, 1);
      if (i + 1 != activityViews.size()) {
        activitiesPane.add(activityViews.get(i + 1), 1, i, 1, 1);
      }
      if (i + 2 < activityViews.size()) {
        activitiesPane.add(activityViews.get(i + 2), 2, i, 1, 1);
      }
      if (i + 3 < activityViews.size()) {
        activitiesPane.add(activityViews.get(i + 3), 3, i, 1, 1);
      }
      if (i + 4 < activityViews.size()) {
        activitiesPane.add(activityViews.get(i + 4), 4, i, 1, 1);
      }
    }
    activitiesPane.setHgap(10);
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

  @FXML
  public void addActivity(KeyEvent event) {
    if(event.getCode() == KeyCode.ENTER && !newActivity.getText().isBlank()){
      String text = newActivity.getText();
      Activity activity = activityService.addActivity(text);
      activityViews.add(new ActivityView(activity.getName(), activity.getImageUrl(), activity.getMaxSpots()));
      fillActivitiesPane();
      newActivity.setText("");
    }
  }

  public void removeActivity(ActivityView activity) {
    this.activityViews.remove(activity);
    fillActivitiesPane();
  }

  @FXML
  public void saveBoard(MouseEvent event){
    List<Activity> activities =
        activityViews.stream().map(a -> new Activity(a.getName().getText(), a.getImageUrl(), a.getSpots().size()))
            .collect(Collectors.toList());
    activityService.saveBoard(activities);
  }

  @FXML
  public void addUser(KeyEvent event){
    if(event.getCode() == KeyCode.ENTER && !newUser.getText().isBlank()){
      String text = newUser.getText();
      Optional<User> user = userService.fetchUserByName(text);
      UserView userView = user.map(u -> new UserView(u.getName(), u.getAvatar())).orElse(new UserView(text, ""));
      newUser.setText("");
      if(user.isEmpty()){
        userViews.add(userView);
        userService.addUser(text, "");
      }
      kids.getChildren().clear();
      kids.getChildren().addAll(userViews);
    }
  }

  public void saveUsers(){
    this.userViews.forEach(uv -> userService.addUser(uv.getName(), uv.getAvatar()));
  }
}
