package com.pdemuinck;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import atlantafx.base.controls.Tile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class UserDetailViewTest {

  FileSystemService fileSystemService = Mockito.mock(FileSystemService.class);
  UserService userService = Mockito.mock(UserService.class);

  @Start
  private void start(Stage stage) throws URISyntaxException {
    VBox vBox = new VBox();
    UserDetailView charlie =
        new UserDetailView("charlie", "", new HashMap<>(), fileSystemService, userService);
    UserDetailView valerie = new UserDetailView("valerie",
        this.getClass().getClassLoader().getResource("red_box.png").toExternalForm(),
        new HashMap<>(),
        fileSystemService, userService);
    UserDetailView maxine =
        new UserDetailView("maxine", "", Map.of("tekenen", Optional.of(100L)), fileSystemService,
            userService);
    vBox.getChildren().addAll(charlie, valerie, maxine);
    stage.setScene(new Scene(vBox));
    stage.show();
  }

  @Test
  public void displays_user_without_avatar(FxRobot robot) {
    FxAssert.verifyThat(".image-view", (ImageView x) -> x.getImage() == null);
  }

  @Test
  public void opens_file_dialog_when_click_on_image_view(FxRobot robot)
      throws InterruptedException {
    ImageView imageView = robot.lookup(".image-view").queryAs(ImageView.class);
    robot.clickOn(imageView);
    Thread.sleep(2000);
    Mockito.verify(fileSystemService, times(1)).openFile(any());
  }

  @Test
  public void opens_file_dialog_when_click_on_header_tile(FxRobot robot)
      throws InterruptedException {
    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);
    Thread.sleep(1000);
    Mockito.verify(fileSystemService, times(1)).openFile(any());
  }

  @Test
  public void sets_user_icon_when_file_is_chosen(FxRobot robot)
      throws IOException, URISyntaxException, InterruptedException {
    File file =
        Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    when(fileSystemService.openFile(any())).thenReturn(file.toURI().toURL().toExternalForm());

    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);

    Thread.sleep(1000);

    ImageView imageView = robot.lookup("#avatar_charlie").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("blue_box.png");
  }

  @Test
  public void saves_user_when_avatar_gets_changed(FxRobot robot)
      throws InterruptedException, URISyntaxException, MalformedURLException {
    // Given
    File file =
        Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    when(fileSystemService.openFile(any())).thenReturn(file.toURI().toURL().toExternalForm());

    // When
    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);
    Thread.sleep(1000);

    // Then
    verify(userService, times(1)).addUser(eq("charlie"), contains("blue_box.png"));
  }

  @Test
  public void be_able_to_change_avatar_after_setting_it(FxRobot robot)
      throws URISyntaxException, InterruptedException, MalformedURLException {
    File blueBox =
        Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    File redBox =
        Paths.get(this.getClass().getClassLoader().getResource("red_box.png").toURI()).toFile();
    when(fileSystemService.openFile(any())).thenReturn(blueBox.toURI().toURL().toExternalForm(), redBox.toURI().toURL().toExternalForm());

    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);
    Thread.sleep(1000);

    ImageView icon = robot.lookup("#avatar_charlie").queryAs(ImageView.class);
    robot.clickOn(icon);
    Thread.sleep(1000);


    ImageView newIcon = robot.lookup("#avatar_charlie").queryAs(ImageView.class);

    assertThat(newIcon.getImage().getUrl()).contains("red_box.png");
  }

  @Test
  public void sets_avatar_when_already_configured(FxRobot robot) {
    ImageView imageView = robot.lookup("#avatar_valerie").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("red_box.png");
  }

  @Test
  public void be_able_to_change_avatar_when_already_set(FxRobot robot)
      throws InterruptedException, URISyntaxException, MalformedURLException {
    File blueBox =
        Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    ImageView imageView = robot.lookup("#avatar_valerie").queryAs(ImageView.class);
    when(fileSystemService.openFile(any())).thenReturn(blueBox.toURI().toURL().toExternalForm());
    robot.clickOn(imageView);
    Thread.sleep(1000);

    // Then
    ImageView newIcon = robot.lookup("#avatar_valerie").queryAs(ImageView.class);
    assertThat(newIcon.getImage().getUrl()).contains("blue_box.png");
  }

  @Test
  public void body_contains_chart_if_activities_are_recorded(FxRobot robot) {
    assertThat(robot.lookup("#activity_timing_maxine").queryAs(Chart.class)).isNotNull();
  }
}
