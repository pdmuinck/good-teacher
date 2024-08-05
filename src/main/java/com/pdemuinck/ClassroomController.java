package com.pdemuinck;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
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
  private ScrollPane activities, users;

  @FXML
  private SplitPane splitScreen;

  @FXML
  private VBox kids;

  @FXML
  private Button start;

  @FXML
  private TextField newActivity;

  @FXML
  private CustomTextField newUser;

  @FXML
  private ChoiceBox presentMode;

  @FXML
  private Button playActivities;

  @FXML
  private Button pauseActivities;

  @FXML
  private InputGroup activityButtons;

  @FXML
  private MenuButton moreBoards;

  private boolean present;

  private ActivityService activityService;
  private FileSystemService fileSystemService;
  private UserService userService;
  List<FixedUserView> fixedUserViews = new ArrayList<>();
  List<EditableUserView> editableUserViews = new ArrayList<>();
  List<EditableActivityView> activityViews = new ArrayList<>();

  public ClassroomController(ActivityService activityService, UserService userService,
                             FileSystemService fileSystemService) {
    this.activityService = activityService;
    this.userService = userService;
    this.fileSystemService = fileSystemService;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
//    moreBoards.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_OUTLINED);
    final double SPEED = 0.01;
    activities.getContent().setOnScroll(scrollEvent -> {
      double deltaY = scrollEvent.getDeltaY() * SPEED;
      activities.setVvalue(activities.getVvalue() - deltaY);
    });
    users.getContent().setOnScroll(scrollEvent -> {
      double deltaY = scrollEvent.getDeltaY() * SPEED;
      users.setVvalue(users.getVvalue() - deltaY);
    });
    playActivities.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.SUCCESS);
    pauseActivities.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
    playActivities.setVisible(false);
    pauseActivities.setVisible(false);
    activityButtons.getChildren().clear();
    List<Activity> activities = activityService.fetchActivities();
    this.activityViews = activities.stream().filter(Activity::isShow).map(
        a -> new EditableActivityView(a.getName(), a.getImageUrl(), a.getMaxSpots(),
            activityService, fileSystemService, this, userService)).collect(
        Collectors.toList());
    activitiesPane.setHgap(10);
    fillWithEditableActivities(this.activityViews);
    editableUserViews =
        userService.fetchUsers().stream().map(k -> new EditableUserView(k.getName(), k.getAvatar(),
                activityService.timeByActivity(k.getName()), userService, fileSystemService))
            .collect(
                Collectors.toList());
    kids.getChildren()
        .add(new Accordion(editableUserViews.toArray(new TitledPane[editableUserViews.size()])));
    kids.getChildren().addAll(editableUserViews);
    kids.setOnDragOver((DragEvent event) -> {
      event.acceptTransferModes(TransferMode.ANY);
      event.consume();
    });
    splitScreen.setOnDragDropped((DragEvent event) -> {
      resetUserListInPresentMode(event);
    });
    kids.setOnDragDropped((DragEvent event) -> {
      resetUserListInPresentMode(event);
    });
  }

  private void resetUserListInPresentMode(DragEvent event) {
    Dragboard db = event.getDragboard();
    if (db.hasString()) {
      String user = db.getString();
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
    event.consume();
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
  }

  @FXML
  public void reset(DragEvent event) {
    if (event.getTransferMode() == null || (event.getGestureSource() instanceof ImageView &&
        ((ImageView) event.getGestureSource()).getId().contains("avatar_")) &&
        !event.getPickResult().getIntersectedNode().getId().contains("spot")) {
      String name = event.getDragboard().getString().split(",")[0];
      this.fixedUserViews.forEach(uv -> {
        if (uv.getName().equals(name)) {
          uv.setVisible(true);
        }
      });
      kids.getChildren().clear();
      kids.getChildren().addAll(this.fixedUserViews);
    }
  }

  @FXML
  public void addActivity(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER && !newActivity.getText().isBlank()) {
      String text = newActivity.getText();
      Activity activity = activityService.addActivity(text);
      activityViews.add(new EditableActivityView(activity.getName(), activity.getImageUrl(),
          activity.getMaxSpots(), activityService, fileSystemService, this, userService));
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
        editableUserViews.add(
            new EditableUserView(text, "", new HashMap<>(), userService, fileSystemService));
        userService.addUser(text, "");
      }
      kids.getChildren().clear();
      kids.getChildren().addAll(editableUserViews);
    }
  }

  public List<String> fetchUsers(String search) {
    return this.editableUserViews.stream().map(EditableUserView::getName)
        .filter(u -> u.contains(search)).collect(Collectors.toList());
  }

  @FXML
  public void onPresentMode(ActionEvent event) {
    String newValue = (String) ((ChoiceBox) event.getTarget()).getValue();
    present = newValue.equals("Presenteer");
    if (present) {
      List<FixedActivityView> fixedActivityViewStream = activityService.fetchActivities().stream()
          .filter(Activity::isShow)
          .map(a -> new FixedActivityView(a.getName(), a.getImageUrl(), a.getMaxSpots(),
              activityService, userService, this))
          .collect(
              Collectors.toList());
      fillWithFixedActivities(fixedActivityViewStream);
      newActivity.setVisible(false);
      newUser.setVisible(false);
      fixedUserViews =
          userService.fetchUsers().stream().filter(u -> !u.getAvatar().isBlank())
              .map(k -> new FixedUserView(k.getName(), k.getAvatar()))
              .collect(Collectors.toList());
      kids.getChildren().clear();
      kids.getChildren().addAll(fixedUserViews);
      activityButtons.getChildren().clear();
      playActivities.setVisible(true);
      pauseActivities.setVisible(true);
      activityButtons.getChildren().addAll(playActivities, pauseActivities);
    } else {
      fillWithEditableActivities(this.activityViews);
      newActivity.setVisible(true);
      newUser.setVisible(true);
      editableUserViews = userService.fetchUsers().stream()
          .map(k -> new EditableUserView(k.getName(), k.getAvatar(),
              activityService.timeByActivity(k.getName()), userService, fileSystemService)).collect(
              Collectors.toList());
      kids.getChildren().clear();
      kids.getChildren()
          .add(new Accordion(editableUserViews.toArray(new TitledPane[editableUserViews.size()])));
      kids.getChildren().addAll(editableUserViews);
      activityButtons.getChildren().clear();
    }
  }

  public void pauseAllActivities(MouseEvent actionEvent) {
    activityService.pauseAllActivities();
  }

  public void hideUser(String name) {
    this.fixedUserViews.forEach(uv -> {
      if (uv.getName().equals(name)) {
        uv.setVisible(false);
      }
    });
  }

  @FXML
  public void saveBoardName(KeyEvent event) {

  }
}
