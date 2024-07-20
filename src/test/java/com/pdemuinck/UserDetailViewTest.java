package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import atlantafx.base.controls.Tile;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.service.query.NodeQuery;

@ExtendWith(ApplicationExtension.class)
public class UserDetailViewTest {

  FileChooser fileChooser = Mockito.mock(FileChooser.class);

  @Start
  private void start(Stage stage) throws URISyntaxException {
    stage.setScene(new Scene(new UserDetailView("charlie", "", new HashMap<>(), fileChooser)));
    stage.show();
  }

  @Test
  public void displays_user_without_avatar(FxRobot robot){
    FxAssert.verifyThat(".image-view", (ImageView x) -> x.getImage() == null);
  }

  @Test
  public void displays_user_without_activities(FxRobot robot){
    ImageView lookup = robot.lookup(".image-view").queryAs(ImageView.class);
    robot.clickOn(lookup);
    Mockito.verify(fileChooser, times(1)).showOpenDialog(any());
  }

  @Test
  void should_contain_button_with_text(FxRobot robot) throws URISyntaxException {

//    FxAssert.verifyThat(button, LabeledMatchers.hasText("click me!"));
    // or (lookup by css id):
    FxAssert.verifyThat("#myButton", LabeledMatchers.hasText("click me!"));
    // or (lookup by css class):
  }
}
