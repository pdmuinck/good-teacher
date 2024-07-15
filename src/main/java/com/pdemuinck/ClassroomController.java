package com.pdemuinck;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
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
  private TextField newActivity;

  @FXML
  private CustomTextField newUser;

  @FXML
  private ToggleSwitch presentMode;

  @FXML
  private Button playActivities;

  @FXML
  private Button pauseActivities;

  @FXML
  private InputGroup activityButtons;

  private ActivityService activityService = new ActivityMockService(new FileDataStore());
  private UserService userService = new UserMockService(new FileDataStore());
  List<FixedUserView> fixedUserViews = new ArrayList<>();
  List<EditableUserView> editableUserViews = new ArrayList<>();
  List<EditableActivityView> activityViews = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    playActivities.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.SUCCESS);
    pauseActivities.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
    playActivities.setVisible(false);
    pauseActivities.setVisible(false);
    activityButtons.getChildren().clear();
    activityButtons.getChildren().add(presentMode);
    List<Activity> activities = activityService.fetchActivities();
    this.activityViews = activities.stream().filter(Activity::isShow).map(
        a -> new EditableActivityView(a.getName(), a.getImageUrl(), a.getMaxSpots(),
            activityService)).collect(
        Collectors.toList());
    activitiesPane.setHgap(10);
    fillWithEditableActivities(this.activityViews);
    editableUserViews =
        userService.fetchUsers().stream().map(k -> new EditableUserView(k.getName(), k.getAvatar(), activityService.timeByActivity(k.getName())))
            .collect(
                Collectors.toList());
    kids.getChildren().add(new Accordion(editableUserViews.toArray(new TitledPane[editableUserViews.size()])));
    kids.getChildren().addAll(editableUserViews);
    kids.setOnDragOver((DragEvent event) -> {
      event.acceptTransferModes(TransferMode.ANY);
      event.consume();
    });
    kids.getParent().setOnDragDropped((DragEvent event) -> {
      Dragboard db = event.getDragboard();
      if (db.hasString()) {
        String user = db.getString();
        if (presentMode.isSelected()) {
          this.fixedUserViews.stream().filter(u -> u.getAvatar().equals(user.split(",")[1]))
              .findFirst()
              .ifPresent(x -> x.setVisible(true));
          kids.getChildren().clear();
          kids.getChildren().addAll(this.fixedUserViews.stream().filter(Node::isVisible).collect(
              Collectors.toList()));
          event.setDropCompleted(true);
        } else {
          event.setDropCompleted(false);
        }
      } else {
        event.setDropCompleted(false);
      }
      event.consume();
    });
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
//    this.changelog.setText(String.join("\n", this.changelog.getText(), change));
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
    if (event.getCode() == KeyCode.ENTER && !newActivity.getText().isBlank()) {
      String text = newActivity.getText();
      Activity activity = activityService.addActivity(text);
      activityViews.add(new EditableActivityView(activity.getName(), activity.getImageUrl(),
          activity.getMaxSpots(), activityService));
      fillWithEditableActivities(this.activityViews);
      newActivity.setText("");
    }
  }

  public void removeActivity(EditableActivityView activity) {
    this.activityViews.remove(activity);
    fillWithEditableActivities(this.activityViews);
    activityService.hideActivity(activity.getName());
  }

  @FXML
  public void addUser(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER && !newUser.getText().isBlank()) {
      String text = newUser.getText();
      newUser.setText("");
      Optional<User> user = userService.fetchUserByName(text);
      if (user.isEmpty()) {
        editableUserViews.add(new EditableUserView(text, "", new HashMap<>()));
        userService.addUser(text, "");
      }
      kids.getChildren().clear();
      kids.getChildren().addAll(editableUserViews);
    }
  }

  public void saveUser(String name, String avatar){
    userService.addUser(name, avatar);
  }

  public void saveUsers() {
    this.editableUserViews.forEach(uv -> userService.addUser(uv.getName(), uv.getAvatar()));
  }

  @FXML
  public void onPresentMode(MouseEvent event) {
    if (this.presentMode.isSelected()) {
      List<FixedActivityView> fixedActivityViewStream = activityService.fetchActivities().stream()
          .filter(Activity::isShow).map(a -> new FixedActivityView(a.getName(), a.getImageUrl(), a.getMaxSpots(),
              activityService))
          .collect(
              Collectors.toList());
      fillWithFixedActivities(fixedActivityViewStream);
      newActivity.setVisible(false);
      newUser.setVisible(false);
      fixedUserViews =
          userService.fetchUsers().stream().map(k -> new FixedUserView(k.getName(), k.getAvatar()))
              .collect(
                  Collectors.toList());
      kids.getChildren().clear();
      kids.getChildren().addAll(fixedUserViews);
      activityButtons.getChildren().clear();
      playActivities.setVisible(true);
      pauseActivities.setVisible(true);
      activityButtons.getChildren().addAll(playActivities, pauseActivities, presentMode);
    } else {
      fillWithEditableActivities(this.activityViews);
      newActivity.setVisible(true);
      newUser.setVisible(true);
      editableUserViews = userService.fetchUsers().stream()
          .map(k -> new EditableUserView(k.getName(), k.getAvatar(), activityService.timeByActivity(k.getName()))).collect(
              Collectors.toList());
      kids.getChildren().clear();
      kids.getChildren().add(new Accordion(editableUserViews.toArray(new TitledPane[editableUserViews.size()])));
      kids.getChildren().addAll(editableUserViews);
      activityButtons.getChildren().clear();
      activityButtons.getChildren().addAll(presentMode);
    }
  }

  public void pauseAllActivities(MouseEvent actionEvent) {
    activityService.pauseAllActivities();
    updateActivityChange("All activities got paused");
  }

  private static class Dialog extends VBox {

    public Dialog(int width, int height) {
      super();

      setSpacing(10);
      setAlignment(Pos.CENTER);
      setMinSize(width, height);
      setMaxSize(width, height);
      setStyle("-fx-background-color: -color-bg-default;");
    }
  }
}
