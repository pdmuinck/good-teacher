package com.pdemuinck;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class FixedUserView extends HBox {

  private String avatar;
  private String name;


  private ImageView imageView;

  public FixedUserView(String name, String avatar) {
    this.name = name;
    this.avatar = avatar;
    super.setOnDragOver((DragEvent event) -> {
      event.acceptTransferModes(TransferMode.NONE);
      event.consume();
    });
    if (!avatar.isBlank()) {
      Image image = new Image(avatar, 75, 75, false, false);
      imageView = new ImageView(image);
      imageView.setId("avatar_" + name);
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
        content.putString(String.join(",", name, avatar));
        db.setContent(content);
        this.setVisible(false);
      });
      imageView.setOnMouseDragged((MouseEvent event) -> {
        event.setDragDetect(true);
      });
      super.getChildren().add(imageView);
    }
  }

  public String getAvatar() {
    return avatar;
  }

  public String getName() {
    return name;
  }
}
