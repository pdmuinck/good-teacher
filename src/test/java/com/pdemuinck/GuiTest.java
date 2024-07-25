package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import atlantafx.base.controls.ToggleSwitch;
import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
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


  @Start
  private void start(Stage stage) throws URISyntaxException, IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("activities.fxml"));
    ClassroomController controller = new ClassroomController(activityService, userService);
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
  public void saves_users_when_added(FxRobot robot){
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
  public void no_activity_input_field_when_switched_to_present_mode(FxRobot robot) throws InterruptedException {
    ToggleSwitch toggleSwitch = robot.lookup("#presentMode").queryAs(ToggleSwitch.class);
    toggleSwitch.setSelected(true);
    assertThat(robot.lookup("#newActivity").queryAs(TextField.class)).isNull();
  }
}
