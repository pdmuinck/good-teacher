package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
  List<User> users = new ArrayList<>();
  List<String> blackList = new ArrayList<>();


  @Start
  private void start(Stage stage) throws URISyntaxException, IOException {
    when(activityService.fetchActivities()).thenReturn(activities);
    when(activityService.fetchBlackList(anyString())).thenReturn(blackList);
    when(userService.fetchUsers()).thenReturn(users);
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("activities.fxml"));
    ClassroomController controller =
        new ClassroomController(activityService, userService, fileSystemService);
    loader.setController(controller);
    Parent root = loader.load();
    stage.setScene(new Scene(root));
    stage.show();
  }

  @Test
  public void adds_users_to_the_list(FxRobot robot) throws InterruptedException {
    // When
    addUser(robot, "charlie");
    addUser(robot, "maxine");
    addUser(robot, "otto");

    // Then
    VBox users = robot.lookup("#kids").queryAs(VBox.class);
    assertThat(users.getChildren()).hasSize(3);
  }

  @Test
  public void saves_users_when_added(FxRobot robot) {
    // When
    addUser(robot, "charlie");
    addUser(robot, "maxine");
    addUser(robot, "otto");

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
  public void shows_play_activity_button_in_present_mode(FxRobot robot) {
    switchToPresentMode(robot);
    assertThat(robot.lookup("#playActivities").queryAs(Button.class).isVisible()).isTrue();
  }

  @Test
  public void shows_pause_activity_button_in_present_mode(FxRobot robot) {
    switchToPresentMode(robot);
    assertThat(robot.lookup("#pauseActivities").queryAs(Button.class).isVisible()).isTrue();
  }

  @Test
  public void shows_new_added_activities_in_present_mode(FxRobot robot) {
    addActivity(robot, "drawing");
    addActivity(robot, "painting");
    switchToPresentMode(robot);
    assertThat(robot.lookup("#activitiesPane").queryAs(GridPane.class).getChildren()).allMatch(
        Node::isVisible).hasSize(2);
  }

  @Test
  public void shows_activities_when_going_back_to_edit_mode(FxRobot robot) {
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
    assertThat(robot.lookup("#grid_for_drawing").queryAs(GridPane.class).getChildren()).hasSize(4)
        .allMatch(Node::isVisible);
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
    assertThat(robot.lookup("#grid_for_drawing").queryAs(GridPane.class).getChildren()).hasSize(2)
        .allMatch(
            Node::isVisible);
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
  public void loads_first_activity_image_in_edit_mode(FxRobot robot) {
    addActivity(robot, "drawing");
    addActivityImage(robot, "drawing", "blue_box.png");
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("blue_box.png");
  }

  @Test
  public void shows_first_activity_image_in_edit_mode(FxRobot robot) {
    addActivity(robot, "drawing");
    addActivityImage(robot, "drawing", "blue_box.png");
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.isVisible()).isTrue();
  }

  @Test
  public void loads_new_activity_image_in_edit_mode(FxRobot robot) {
    addActivity(robot, "drawing");
    addActivityImage(robot, "drawing", "blue_box.png");
    changeActivityImage(robot, "drawing", "red_box.png");
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("red_box.png");
  }

  @Test
  public void shows_new_activity_image_in_edit_mode(FxRobot robot) {
    addActivity(robot, "drawing");
    addActivityImage(robot, "drawing", "blue_box.png");
    changeActivityImage(robot, "drawing", "red_box.png");
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.isVisible()).isTrue();
  }

  @Test
  public void updates_activity_when_image_gets_added(FxRobot robot) {
    addActivity(robot, "drawing", 1);
    addActivityImage(robot, "drawing", "blue_box.png");
    verify(activityService, times(1)).updateActivity(eq("drawing"), contains("blue_box.png"),
        eq(1));
  }

  @Test
  public void updates_activity_when_image_changes(FxRobot robot){
    addActivity(robot, "drawing", 2);
    addActivityImage(robot, "drawing", "blue_box.png");
    changeActivityImage(robot, "drawing", "red_box.png");
    verify(activityService, times(1)).updateActivity(eq("drawing"), contains("blue_box.png"), eq(2));
    verify(activityService, times(1)).updateActivity(eq("drawing"), contains("red_box.png"), eq(2));
  }

  @Test
  public void loads_activity_images(FxRobot robot) {
    addActivity(robot, "drawing", "blue_box.png");
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("blue_box.png");
  }

  @Test
  public void shows_activity_images(FxRobot robot) {
    addActivity(robot, "drawing", "blue_box.png");
    ImageView imageView = robot.lookup("#image_for_drawing").queryAs(ImageView.class);
    assertThat(imageView.isVisible()).isTrue();
  }

  @Test
  public void hides_activity_when_removed_from_grid(FxRobot robot){
    addActivity(robot, "drawing", "blue_box.png");
    removeActivity(robot, "drawing");
    assertThat(robot.lookup("#activitiesPane").queryAs(GridPane.class).getChildren()).hasSize(0);
  }

  @Test
  public void drags_user_to_activity(FxRobot robot){
    addUser(robot, "charlie", "batman.png");
    addActivity(robot, "drawing");
    switchToPresentMode(robot);
    ImageView firstSpot = robot.lookup("#0_spot_for_drawing").queryAs(ImageView.class);
    robot.drag("#avatar_charlie").dropTo(firstSpot);
    assertThat(firstSpot.getImage().getUrl()).contains("batman.png");
    assertThat(firstSpot.isVisible()).isTrue();
  }

  @Test
  public void hides_user_from_user_list_when_dropped_to_an_activity(FxRobot robot){
    addUser(robot, "charlie", "batman.png");
    addActivity(robot, "drawing");
    switchToPresentMode(robot);
    robot.drag("#avatar_charlie").dropTo("#0_spot_for_drawing");
    VBox users = robot.lookup("#kids").queryAs(VBox.class);
    assertThat(users.getChildren().get(0).isVisible()).isFalse();
  }

  @Test
  public void shows_activity_image_in_present_mode(FxRobot robot){
    addActivity(robot, "drawing", "blue_box.png");
    switchToPresentMode(robot);
    FixedActivityView fixedActivityView =
        (FixedActivityView) robot.lookup("#activitiesPane").queryAs(GridPane.class).getChildren()
            .get(0);
    ImageView activityImage = (ImageView) fixedActivityView.getChildren().get(0);
    assertThat(activityImage.getImage().getUrl()).contains("blue_box.png");
  }

  @Test
  public void resets_user_in_list_when_dropped_outside_an_activity(FxRobot robot){
    addUser(robot, "charlie", "batman.png");
    addActivity(robot, "drawing", "red_box.png");
    switchToPresentMode(robot);
    robot.drag("#avatar_charlie").dropTo("#activities");
    VBox users = robot.lookup("#kids").queryAs(VBox.class);
    assertThat(users.getChildren().get(0).isVisible()).isTrue();
  }

  @Test
  public void resets_user_in_list_when_dropped_in_user_left_pane(FxRobot robot){
    addUser(robot, "charlie", "batman.png");
    addActivity(robot, "drawing", "red_box.png");
    switchToPresentMode(robot);
    robot.drag("#avatar_charlie").dropTo("#kids");
    VBox users = robot.lookup("#kids").queryAs(VBox.class);
    assertThat(users.getChildren().get(0).isVisible()).isTrue();
  }

  @Test
  public void resets_user_in_list_when_dropped_on_another_user(FxRobot robot){
    addUser(robot, "charlie", "batman.png");
    addUser(robot, "maxine", "red_box.png");
    switchToPresentMode(robot);
    robot.drag("#avatar_charlie").dropTo("#avatar_maxine");
    VBox users = robot.lookup("#kids").queryAs(VBox.class);
    assertThat(users.getChildren().get(0).isVisible()).isTrue();
  }

  @Test
  public void shows_black_list_for_each_activity_in_edit_mode(FxRobot robot){
    registerBlackList("drawing", "charlie");
    addActivity(robot, "drawing");
    VBox blackList = robot.lookup("#blacklist_for_drawing").queryAs(VBox.class);
    assertThat(blackList.getChildren()).hasSize(1);
  }

  @Test
  public void shows_users_for_blacklist_search(FxRobot robot){
    addActivity(robot, "drawing");
    addUser(robot, "charlie");
    robot.clickOn("#blacklist_search_for_drawing").write("charlie");
    CheckBox checkBox = robot.lookup("#user_for_blacklist_charlie").queryAs(CheckBox.class);
    assertThat(checkBox.isVisible()).isTrue();
  }

  @Test
  public void shows_selected_users_for_blacklist_search(FxRobot robot){
    registerBlackList("drawing", "charlie");
    addActivity(robot, "drawing");
    addUser(robot, "charlie");
    robot.clickOn("#blacklist_search_for_drawing").write("charlie");
    CheckBox checkBox = robot.lookup("#user_for_blacklist_charlie").queryAs(CheckBox.class);
    assertThat(checkBox.isSelected()).isTrue();
  }

  @Test
  public void shows_selected_users_for_blacklist_by_default(FxRobot robot){
    registerBlackList("drawing", "charlie");
    addActivity(robot, "drawing");
    addUser(robot, "charlie");
    CheckBox checkBox = robot.lookup("#user_for_blacklist_charlie").queryAs(CheckBox.class);
    assertThat(checkBox.isSelected()).isTrue();
  }

  @Test
  public void saves_user_added_to_blacklist(FxRobot robot){
    addUser(robot, "charlie");
    addActivity(robot, "drawing");
    robot.clickOn("#blacklist_search_for_drawing").write("charlie");
    robot.clickOn("#user_for_blacklist_charlie");
    verify(activityService, times(1)).addToBlackList("drawing", "charlie");
  }

  @Test
  public void adds_user_to_activity_blacklist(FxRobot robot){
    addUser(robot, "charlie");
    addActivity(robot, "drawing");
    robot.clickOn("#blacklist_search_for_drawing").write("charlie");
    robot.clickOn("#user_for_blacklist_charlie");
    CheckBox checkBox = robot.lookup("#user_for_blacklist_charlie").queryAs(CheckBox.class);
    assertThat(checkBox.isSelected()).isTrue();
  }

  @Test
  public void removes_user_from_blacklist(FxRobot robot){
    registerBlackList("drawing", "charlie");
    addUser(robot, "charlie");
    addActivity(robot, "drawing");
    removeFromBlackList(robot, "drawing", "charlie");
    CheckBox checkBox = robot.lookup("#user_for_blacklist_charlie").queryAs(CheckBox.class);
    assertThat(checkBox.isSelected()).isFalse();
  }

  @Test
  public void saves_removal_from_blacklist(FxRobot robot){
    registerBlackList("drawing", "charlie");
    addUser(robot, "charlie");
    addActivity(robot, "drawing");
    removeFromBlackList(robot, "drawing", "charlie");
    verify(activityService, times(1)).removeFromBlackList("drawing", "charlie");
  }

  @Test
  public void shows_black_list_for_each_activity_in_present_mode(FxRobot robot){
    registerBlackList("drawing", "charlie");
    addActivity(robot, "drawing", "batman.png");
    addUser(robot, "charlie", "batman.png");
    switchToPresentMode(robot);
    GridPane blackList = robot.lookup("#blacklist_for_drawing").queryAs(GridPane.class);
    assertThat(blackList.getChildren()).hasSize(1);
  }

  @Test
  public void drag_user_from_activity_to_activity_in_present_mode(FxRobot robot){
    addActivity(robot, "drawing");
    addActivity(robot, "painting");
    addUser(robot, "charlie", "batman.png");
    switchToPresentMode(robot);
    robot.drag("#avatar_charlie").dropTo("#0_spot_for_drawing");
    robot.drag("#0_spot_for_drawing").dropTo("#0_spot_for_painting");

    ImageView firstSpot = robot.lookup("#0_spot_for_painting").queryAs(ImageView.class);
    assertThat(firstSpot.getImage().getUrl()).contains("batman.png");
  }

  private void removeActivity(FxRobot robot, String activityName) {
    robot.clickOn("#remove_activity_" + activityName);
    activities = activities.stream().filter(a -> !a.getName().equals(activityName)).collect(
        Collectors.toList());;
  }

  private void changeActivityImage(FxRobot robot, String activityName, String image) {
    try {
      File file =
          Paths.get(this.getClass().getClassLoader().getResource(image).toURI()).toFile();
      when(fileSystemService.openFile(any())).thenReturn(file.toURI().toURL().toExternalForm());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    robot.clickOn("#image_for_" + activityName);
  }

  private void addActivityImage(FxRobot robot, String name, String image) {
    try {
      File file =
          Paths.get(this.getClass().getClassLoader().getResource(image).toURI()).toFile();
      when(fileSystemService.openFile(any())).thenReturn(file.toURI().toURL().toExternalForm());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    robot.clickOn("#add_image_for_" + name);
  }

  private void addActivity(FxRobot robot, String name, String image) {
    addActivity(robot, name, 3, image);
  }

  private void addActivity(FxRobot robot, String name, int spots) {
    addActivity(robot, name, spots, "");
  }

  private void addActivity(FxRobot robot, String name) {
    addActivity(robot, name, 3, "");
  }

  private void addActivity(FxRobot robot, String name, int spots, String image) {
    Activity activity = new Activity(name, image, spots);
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

  private void switchToPresentMode(FxRobot robot) {
    ToggleSwitch toggleSwitch = robot.lookup("#presentMode").queryAs(ToggleSwitch.class);
    toggleSwitch.setSelected(true);
    robot.clickOn(toggleSwitch);
  }

  private void switchToEditMode(FxRobot robot) {
    ToggleSwitch toggleSwitch = robot.lookup("#presentMode").queryAs(ToggleSwitch.class);
    toggleSwitch.setSelected(false);
    robot.clickOn(toggleSwitch);
  }

  private void addActivitySpot(FxRobot robot, String name) {
    robot.clickOn("#add_spot_for_" + name);
  }

  private void removeActivitySpot(FxRobot robot, String name) {
    robot.clickOn("#remove_spot_for_" + name);
  }

  private void addUser(FxRobot robot, String name){
    robot.clickOn("#newUser").write(name).push(KeyCode.ENTER);
    users.add(new User(name, ""));
  }

  private void addUser(FxRobot robot, String name, String avatar){
    robot.clickOn("#newUser").write(name).push(KeyCode.ENTER);
    try {
      File file =
          Paths.get(this.getClass().getClassLoader().getResource(avatar).toURI()).toFile();
      when(fileSystemService.openFile(any())).thenReturn(file.toURI().toURL().toExternalForm());
      User user = new User(name, file.toURI().toURL().toExternalForm());
      when(userService.fetchUserByName(name)).thenReturn(Optional.of(user));
      users.add(user);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    robot.clickOn("#user_detail_header_" + name);
  }

  private void registerBlackList(String activityName, String user){
    blackList.add(user);
  }

  private void removeFromBlackList(FxRobot robot, String activity, String user){
    robot.clickOn("#blacklist_search_for_" + activity).write(user);
    robot.clickOn("#user_for_blacklist_charlie");
  }
}
