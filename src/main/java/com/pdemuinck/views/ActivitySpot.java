package com.pdemuinck.views;

import atlantafx.base.layout.InputGroup;
import com.pdemuinck.Activity;
import com.pdemuinck.ActivityService;
import com.pdemuinck.ClassroomController;
import com.pdemuinck.UserService;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class ActivitySpot extends VBox {

  private ImageView imageView;

  private ActivityService activityService;

  private UserService userService;

  private ClassroomController classroomController;

  private String activityName;

  private InputGroup feedbackGroup;

  private FontIcon smile = new FontIcon(Feather.SMILE);
  private FontIcon meh = new FontIcon(Feather.MEH);
  private FontIcon frown = new FontIcon(Feather.FROWN);

  private String currentUser;


  public ActivitySpot(ClassroomController classroomController, UserService userService, ActivityService activityService, String activityName, Image image, int x){
    this.activityService = activityService;
    this.activityName = activityName;
    this.classroomController = classroomController;
    this.userService = userService;

    this.feedbackGroup = new InputGroup(smile, meh, frown);
    smile.setOnMouseClicked(e -> {
      meh.setIconColor(Paint.valueOf("black"));
      frown.setIconColor(Paint.valueOf("black"));
      smile.setIconColor(Paint.valueOf("green"));
      userService.saveFeedback(activityName, currentUser, "smile");
    });
    meh.setOnMouseClicked(e -> {
      frown.setIconColor(Paint.valueOf("black"));
      smile.setIconColor(Paint.valueOf("black"));
      meh.setIconColor(Paint.valueOf("orange"));
      userService.saveFeedback(activityName, currentUser, "meh");
    });
    frown.setOnMouseClicked(e -> {
      frown.setIconColor(Paint.valueOf("red"));
      smile.setIconColor(Paint.valueOf("black"));
      meh.setIconColor(Paint.valueOf("black"));
      userService.saveFeedback(activityName, currentUser, "frown");
    });
    feedbackGroup.setVisible(false);
    super.getChildren().addAll(prepareImageView(image, x), feedbackGroup);
  }

  private ImageView prepareImageView(Image basic, int x) {
    ImageView imageView1 = new ImageView(basic);
    imageView1.setUserData("icons/empty_box.png");
    imageView1.setId(x + "_spot_for_" + activityName);
    imageView1.setOnDragDetected((MouseEvent event) -> {
      if (!imageView1.getUserData().equals("icons/empty_box.png")) {
        Dragboard db = imageView1.startDragAndDrop(TransferMode.ANY);
        db.setDragView(imageView1.getImage());
        ClipboardContent content = new ClipboardContent();
        content.putString((String) imageView1.getUserData());
        db.setContent(content);
        activityService.leaveActivity(this.activityName, (String) imageView1.getUserData());
        imageView1.setImage(basic);
        imageView1.setUserData("icons/empty_box.png");
        this.feedbackGroup.setVisible(false);
      }
    });
    imageView1.setOnDragOver((DragEvent event) -> {
      List<Activity> activities = activityService.getActivities();
      List<String> blackList = activityService.fetchBlackList(this.activityName);
      if (activities.stream().anyMatch(a -> a.getName().equals(this.activityName) &&
          (a.getAvailableSpots() == 0 ||
              blackList.contains(event.getDragboard().getString().split(",")[0])))) {
        event.acceptTransferModes(TransferMode.NONE);
      } else if (event.getGestureSource() != imageView1 && event.getDragboard().hasString() &&
          imageView1.getUserData().equals("icons/empty_box.png")) {
        event.acceptTransferModes(TransferMode.ANY);
      }
      event.consume();
    });
    imageView1.setOnDragDropped((DragEvent event) -> {
      Dragboard db = event.getDragboard();
      if (db.hasString()) {
        if (imageView1.getUserData().equals("icons/empty_box.png")) {
          Image image2 =
              null;
          image2 =
              new Image(db.getString().split(",")[1], 75, 75, false, false);
          imageView1.setImage(image2);
          imageView1.setPreserveRatio(true);
          imageView1.setUserData(db.getString());

          activityService.joinActivity(activityName, userService.fetchUsers().stream().filter(
                  u -> u.getName().equals(db.getString().split(",")[0]) &&
                      u.getAvatar().equals(db.getString().split(",")[1]))
              .findFirst().get().getName());
          currentUser = db.getString().split(",")[0];
          classroomController.hideUser(db.getString().split(",")[0]);
          this.feedbackGroup.setId("feedback_from_" + db.getString().split(",")[0]);
          this.feedbackGroup.setVisible(true);
        } else {
          event.setDropCompleted(false);
        }
        event.setDropCompleted(true);
      } else {
        event.setDropCompleted(false);
      }
      event.consume();
    });
    return imageView1;
  }
}
