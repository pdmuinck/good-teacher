package com.pdemuinck;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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

  @FXML
  private ToggleButton presentMode;

  @FXML
  private Button saveBoardButton;

  private ActivityService activityService = new ActivityMockService(new FileDataStore());
  private UserService userService = new UserMockService(new FileDataStore());
  List<FixedUserView> fixedUserViews = new ArrayList<>();
  List<EditableUserView> editableUserViews = new ArrayList<>();
  List<EditableActivityView> activityViews = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<Activity> activities = activityService.fetchActivities();
    this.activityViews = activities.stream().filter(Activity::isShow).map(a -> new EditableActivityView(a.getName(), a.getImageUrl(), a.getMaxSpots(), activityService)).collect(
        Collectors.toList());
    activitiesPane.setHgap(10);
    fillWithEditableActivities(this.activityViews);
    editableUserViews = userService.fetchUsers().stream().map(k -> new EditableUserView(k.getName(), k.getAvatar())).collect(
            Collectors.toList());
    kids.getChildren().addAll(editableUserViews);
  }

  private void fillWithEditableActivities(List<EditableActivityView> activities) {
    activitiesPane.getChildren().clear();
    for (int i = 0; i < activities.size(); i += 5) {
      activitiesPane.add(activities.get(i), 0, i, 1, 1);
      if (i + 1 != activities.size()) {
        activitiesPane.add(activities.get(i + 1), 1, i, 1, 1);
      }
      if (i + 2 < activities.size()) {
        activitiesPane.add(activities.get(i + 2), 2, i, 1, 1);
      }
      if (i + 3 < activities.size()) {
        activitiesPane.add(activities.get(i + 3), 3, i, 1, 1);
      }
      if (i + 4 < activities.size()) {
        activitiesPane.add(activities.get(i + 4), 4, i, 1, 1);
      }
    }
    activitiesPane.setHgap(10);
  }

  private void fillWithFixedActivities(List<FixedActivityView> activities) {
    activitiesPane.getChildren().clear();
    for (int i = 0; i < activities.size(); i += 5) {
      activitiesPane.add(activities.get(i), 0, i, 1, 1);
      if (i + 1 != activities.size()) {
        activitiesPane.add(activities.get(i + 1), 1, i, 1, 1);
      }
      if (i + 2 < activities.size()) {
        activitiesPane.add(activities.get(i + 2), 2, i, 1, 1);
      }
      if (i + 3 < activities.size()) {
        activitiesPane.add(activities.get(i + 3), 3, i, 1, 1);
      }
      if (i + 4 < activities.size()) {
        activitiesPane.add(activities.get(i + 4), 4, i, 1, 1);
      }
    }
    activitiesPane.setHgap(10);
  }


  @FXML
  public void startAllActivities(MouseEvent event) {
    activityService.startAllActivities();
    updateActivityChange("All activities got started");
  }

  public void updateActivityChange(String change) {
    this.changelog.setText(String.join("\n", this.changelog.getText(), change));
  }

  @FXML
  public void reset(DragEvent event) {
    if (event.getTransferMode() == null) {
      String avatar = event.getDragboard().getString();
      this.fixedUserViews.stream().filter(uv -> uv.getAvatar().equals(avatar)).findFirst()
          .ifPresent(uv -> uv.reset(avatar));
    }
  }

  @FXML
  public void addActivity(KeyEvent event) {
    if(event.getCode() == KeyCode.ENTER && !newActivity.getText().isBlank()){
      String text = newActivity.getText();
      Activity activity = activityService.addActivity(text);
      activityViews.add(new EditableActivityView(activity.getName(), activity.getImageUrl(), activity.getMaxSpots(), activityService));
      fillWithEditableActivities(this.activityViews);
      newActivity.setText("");
    }
  }

  public void removeActivity(EditableActivityView activity) {
    this.activityViews.remove(activity);
    fillWithEditableActivities(this.activityViews);
    activityService.hideActivity(activity.getName().getText());
  }

  @FXML
  public void addUser(KeyEvent event){
    if(event.getCode() == KeyCode.ENTER && !newUser.getText().isBlank()){
      String text = newUser.getText();
      Optional<User> user = userService.fetchUserByName(text);
      FixedUserView
          fixedUserView = user.map(u -> new FixedUserView(u.getName(), u.getAvatar())).orElse(new FixedUserView(text, ""));
      newUser.setText("");
      if(user.isEmpty()){
        fixedUserViews.add(fixedUserView);
        userService.addUser(text, "");
      }
      kids.getChildren().clear();
      kids.getChildren().addAll(fixedUserViews);
    }
  }

  public void saveUsers(){
    this.editableUserViews.forEach(uv -> userService.addUser(uv.getName(), uv.getAvatar()));
  }

  @FXML
  public void onPresentMode(ActionEvent event){
    if(this.presentMode.isSelected()){
      List<FixedActivityView> fixedActivityViewStream = activityViews.stream()
          .map(a -> new FixedActivityView(a.getName().getText(), a.getImageUrl(), a.getSpots().size())).collect(
              Collectors.toList());
      fillWithFixedActivities(fixedActivityViewStream);
      newActivity.setVisible(false);
      newUser.setVisible(false);
      fixedUserViews = userService.fetchUsers().stream().map(k -> new FixedUserView(k.getName(), k.getAvatar())).collect(
          Collectors.toList());
      kids.getChildren().clear();
      kids.getChildren().addAll(fixedUserViews);
    } else {
      fillWithEditableActivities(this.activityViews);
      newActivity.setVisible(true);
      newUser.setVisible(true);
      editableUserViews = userService.fetchUsers().stream().map(k -> new EditableUserView(k.getName(), k.getAvatar())).collect(
          Collectors.toList());
      kids.getChildren().clear();
      kids.getChildren().addAll(editableUserViews);
    }
  }
}
