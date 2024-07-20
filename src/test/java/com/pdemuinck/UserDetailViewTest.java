package com.pdemuinck;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.net.URISyntaxException;
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
  public void opens_dialog_when_click_on_image_view(FxRobot robot) throws InterruptedException {
    ImageView lookup = robot.lookup(".image-view").queryAs(ImageView.class);
    robot.clickOn(lookup);
    Thread.sleep(1000);
    Mockito.verify(fileChooser, times(1)).showOpenDialog(any());
  }
}
