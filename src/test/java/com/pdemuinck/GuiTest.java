package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ToggleSwitch;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class GuiTest {

  ActivityService activityService = Mockito.mock(ActivityService.class);
  UserService userService = Mockito.mock(UserService.class);
  FileSystemService fileSystemService = Mockito.mock(FileSystemService.class);
  List<Activity> activities = new ArrayList<>();


  @Start
  private void start(Stage stage) throws URISyntaxException, IOException {
    when(activityService.fetchActivities()).thenReturn(activities);
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("activities.fxml"));
    ClassroomController controller = new ClassroomController(activityService, userService, fileSystemService);
    loader.setController(controller);
    Parent root = loader.load();
    stage.setScene(new Scene(root));
    stage.show();
  }

  @Test
  public void adds_users_to_the_list(FxRobot robot) throws InterruptedException {
    // When
    robot.clickOn("#newUser").write("charlie").push(KeyCode.ENTER);
    robot.clickOn("#newUser").write("maxine").push(KeyCode.ENTER);
    robot.clickOn("#newUser").write("otto").push(KeyCode.ENTER);

    // Then
    VBox users = robot.lookup("#kids").queryAs(VBox.class);
    assertThat(users.getChildren()).hasSize(3);
  }

  @Test
  public void saves_users_when_added(FxRobot robot) {
    // When
    robot.clickOn("#newUser").write("charlie").push(KeyCode.ENTER);
    robot.clickOn("#newUser").write("maxine").push(KeyCode.ENTER);
    robot.clickOn("#newUser").write("otto").push(KeyCode.ENTER);

    // Then
    verify(userService, times(3)).addUser(any(), any());
  }

  @Test
  public void adds_activities_to_list(FxRobot robot) throws InterruptedException {
    // Given
    when(activityService.addActivity(any())).thenReturn(new Activity("drawing", "", 3));

    // When
    robot.clickOn("#newActivity").write("drawing");
    Thread.sleep(500);
    robot.push(KeyCode.ENTER);
    Thread.sleep(500);

    // Then
    GridPane activities = robot.lookup("#activitiesPane").queryAs(GridPane.class);
    assertThat(activities.getChildren()).hasSize(1);
  }

  @Test
  public void hides_activity_input_field_when_switched_to_present_mode(FxRobot robot)
      throws InterruptedException {
    switchToPresentMode(robot);
    assertThat(robot.lookup("#newActivity").queryAs(CustomTextField.class).isVisible()).isFalse();
  }

  @Test
  public void hides_new_user_input_field_when_switched_to_present_mode(FxRobot robot)
      throws InterruptedException {
    switchToPresentMode(robot);
    assertThat(robot.lookup("#newUser").queryAs(CustomTextField.class).isVisible()).isFalse();
  }

  @Test
  public void shows_play_activity_button_in_present_mode(FxRobot robot){
    switchToPresentMode(robot);
    assertThat(robot.lookup("#playActivities").queryAs(Button.class).isVisible()).isTrue();
  }

  @Test
  public void shows_pause_activity_button_in_present_mode(FxRobot robot){
    switchToPresentMode(robot);
    assertThat(robot.lookup("#pauseActivities").queryAs(Button.class).isVisible()).isTrue();
  }

  @Test
  public void shows_new_added_activities_in_present_mode(FxRobot robot){
    addActivity(robot, "drawing");
    addActivity(robot, "painting");
    switchToPresentMode(robot);
    assertThat(robot.lookup("#activitiesPane").queryAs(GridPane.class).getChildren()).allMatch(
        Node::isVisible).hasSize(2);
  }

  @Test
  public void shows_activities_when_going_back_to_edit_mode(FxRobot robot){
    addActivity(robot, "drawing");
    switchToPresentMode(robot);
    switchToEditMode(robot);
    assertThat(robot.lookup("#activitiesPane").queryAs(GridPane.class).getChildren()).allMatch(
        Node::isVisible).hasSize(1);
  }

  @Test
  public void can_add_spots_to_activity_in_edit_mode(FxRobot robot) throws InterruptedException {
    addActivity(robot, "drawing", 3);
    Thread.sleep(100);
    addActivitySpot(robot, "drawing");
    assertThat(robot.lookup("#grid_for_drawing").queryAs(GridPane.class).getChildren()).hasSize(4);
  }

  @Test
  public void updates_activity_in_database_when_spots_are_added(FxRobot robot)
      throws InterruptedException {
    addActivity(robot, "drawing", 3);
    Thread.sleep(100);
    addActivitySpot(robot, "drawing");
    verify(activityService, times(1)).updateActivity("drawing", "", 4);
  }

  @Test
  public void can_remove_spots_from_activity_in_edit_mode(FxRobot robot)
      throws InterruptedException {
    addActivity(robot, "drawing", 3);
    Thread.sleep(100);
    removeActivitySpot(robot, "drawing");
    assertThat(robot.lookup("#grid_for_drawing").queryAs(GridPane.class).getChildren()).hasSize(2);
  }

  @Test
  public void updates_activity_in_database_when_spots_are_removed(FxRobot robot)
      throws InterruptedException {
    addActivity(robot, "drawing", 3);
    Thread.sleep(100);
    removeActivitySpot(robot, "drawing");
    verify(activityService, times(1)).updateActivity("drawing", "", 2);
  }

  @Test
  public void can_change_activity_image_in_edit_mode(FxRobot robot)
      throws URISyntaxException, MalformedURLException {
    // Given
    addActivity(robot, "drawing");
    File file =
        Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    when(fileSystemService.openFile(any())).thenReturn(file.toURI().toURL().toExternalForm());

    // When
    robot.clickOn("#add_image_for_drawing");

    // Then
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("blue_box.png");
  }


  private void addActivity(FxRobot robot, String name){
    addActivity(robot, name, 3);
  }

  private void addActivity(FxRobot robot, String name, int spots){
    Activity activity = new Activity(name, "", spots);
    activities.add(activity);
    when(activityService.addActivity(any())).thenReturn(activity);
    robot.clickOn("#newActivity").write(name);
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    robot.push(KeyCode.ENTER);
  }

  private void switchToPresentMode(FxRobot robot){
    ToggleSwitch toggleSwitch = robot.lookup("#presentMode").queryAs(ToggleSwitch.class);
    toggleSwitch.setSelected(true);
    robot.clickOn(toggleSwitch);
  }

  private void switchToEditMode(FxRobot robot){
    ToggleSwitch toggleSwitch = robot.lookup("#presentMode").queryAs(ToggleSwitch.class);
    toggleSwitch.setSelected(false);
    robot.clickOn(toggleSwitch);
  }

  private void addActivitySpot(FxRobot robot, String name){
    robot.clickOn("#add_spot_for_" + name);
  }

  private void removeActivitySpot(FxRobot robot, String name){
    robot.clickOn("#remove_spot_for_" + name);
  }
}
