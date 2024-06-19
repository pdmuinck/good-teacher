package com.pdemuinck;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

public class UserView extends HBox {

  private String avatar;
  private boolean hide;
  private ImageView imageView;

  public UserView(String avatar) {
    this.avatar = avatar;
    Image image = new Image(getClass().getClassLoader().getResourceAsStream(avatar), 75, 75, false, false);
    imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
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
  }

  public String getAvatar() {
    return avatar;
  }

  public boolean isHide() {
    return hide;
  }

  public void setHide(boolean hide) {
    this.hide = hide;
  }

  public void reset(String avatar){
    super.getChildren().remove(this.imageView);
    Image image = new Image(getClass().getClassLoader().getResourceAsStream(avatar), 75, 75, false, false);
    imageView.setImage(image);
    imageView.setVisible(true);
    super.getChildren().add(imageView);
  }
}
