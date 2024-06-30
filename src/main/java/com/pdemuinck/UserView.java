package com.pdemuinck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class UserView extends HBox {

  private String avatar;
  private String name;
  private ImageView imageView;

  public UserView(String name, String avatar) {
    this.name = name;
    this.avatar = avatar;
    if(avatar.isBlank()){
      Label label = new Label(name);

      FileChooser fileChooser = new FileChooser();
      label.setOnMouseClicked(e -> {
        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        this.avatar = selectedFile.getAbsolutePath();
        try {
          FileInputStream fs = new FileInputStream(selectedFile.getAbsolutePath());
          Image image = new Image(fs, 75, 75, false, false);
          imageView = new ImageView(image);
          imageView.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = imageView.startDragAndDrop(TransferMode.ANY);
            db.setDragView(image);
            db.setDragViewOffsetX(50.0);
            db.setDragViewOffsetY(50.0);
            ClipboardContent content = new ClipboardContent();
            content.putString(avatar);
            db.setContent(content);
            imageView.setVisible(false);
          });
          imageView.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
          });
          Tooltip tp = new Tooltip(name);
          tp.setShowDelay(Duration.millis(100));
          tp.setShowDuration(Duration.millis(1500));
          Tooltip.install(imageView, tp);
          super.getChildren().add(imageView);
          super.getChildren().remove(label);
          Main.classroomController.saveUsers();
        } catch (FileNotFoundException ex) {
          throw new RuntimeException(ex);
        }
      });

      super.getChildren().add(label);
    } else {
      try {
        Image image = new Image(new FileInputStream(avatar), 75, 75, false, false);
        imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        Tooltip tp = new Tooltip(name);
        tp.setShowDelay(Duration.millis(100));
        tp.setShowDuration(Duration.millis(1500));
        Tooltip.install(imageView, tp);
        imageView.setOnDragDetected((MouseEvent event) -> {
          Dragboard db = imageView.startDragAndDrop(TransferMode.ANY);
          db.setDragView(image);
          db.setDragViewOffsetX(50.0);
          db.setDragViewOffsetY(50.0);
          ClipboardContent content = new ClipboardContent();
          content.putString(avatar);
          db.setContent(content);
          imageView.setVisible(false);
        });
        imageView.setOnMouseDragged((MouseEvent event) -> {
          event.setDragDetect(true);
        });
        super.getChildren().add(imageView);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }

    }
  }

  public String getAvatar() {
    return avatar;
  }

  public String getName() {
    return name;
  }

  public void reset(String avatar){
    super.getChildren().remove(this.imageView);
    Image image = null;
    try {
      image = new Image(new FileInputStream(avatar), 75, 75, false, false);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    imageView.setImage(image);
    imageView.setVisible(true);
    super.getChildren().add(imageView);
  }
}
