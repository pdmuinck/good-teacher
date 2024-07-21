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
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
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

  FileChooser fileChooser = Mockito.mock(FileChooser.class);
  UserService userService = Mockito.mock(UserService.class);

  @Start
  private void start(Stage stage) throws URISyntaxException {
    stage.setScene(new Scene(new UserDetailView("charlie", "", new HashMap<>(), fileChooser, userService)));
    stage.show();
  }

  @Test
  public void displays_user_without_avatar(FxRobot robot){
    FxAssert.verifyThat(".image-view", (ImageView x) -> x.getImage() == null);
  }

  @Test
  public void opens_file_dialog_when_click_on_image_view(FxRobot robot) throws InterruptedException {
    ImageView imageView = robot.lookup(".image-view").queryAs(ImageView.class);
    robot.clickOn(imageView);
    Thread.sleep(1000);
    Mockito.verify(fileChooser, times(1)).showOpenDialog(any());
  }

  @Test
  public void opens_file_dialog_when_click_on_header_tile(FxRobot robot) throws InterruptedException {
    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);
    Thread.sleep(1000);
    Mockito.verify(fileChooser, times(1)).showOpenDialog(any());
  }

  @Test
  public void sets_user_icon_when_file_is_chosen(FxRobot robot)
      throws IOException, URISyntaxException, InterruptedException {
    File file = Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    when(fileChooser.showOpenDialog(any())).thenReturn(file);

    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);

    Thread.sleep(1000);

    ImageView imageView = robot.lookup("#image_view_charlie").queryAs(ImageView.class);
    assertThat(imageView.getImage().getUrl()).contains("blue_box.png");
  }

  @Test
  public void saves_user_when_avatar_gets_changed(FxRobot robot)
      throws InterruptedException, URISyntaxException {
    // Given
    File file = Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    when(fileChooser.showOpenDialog(any())).thenReturn(file);

    // When
    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);
    Thread.sleep(1000);

    // Then
    verify(userService, times(1)).addUser(eq("charlie"), contains("blue_box.png"));
  }

  @Test
  public void be_able_to_change_avatar_after_setting_it(FxRobot robot)
      throws URISyntaxException, InterruptedException {
    File file = Paths.get(this.getClass().getClassLoader().getResource("blue_box.png").toURI()).toFile();
    when(fileChooser.showOpenDialog(any())).thenReturn(file);

    Tile tile = robot.lookup("#user_detail_header_charlie").queryAs(Tile.class);
    robot.clickOn(tile);

    Thread.sleep(1000);

  }
}
