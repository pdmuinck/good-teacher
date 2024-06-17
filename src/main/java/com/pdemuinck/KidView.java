package com.pdemuinck;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class KidView extends HBox {

  private String avatar;

  public KidView(String avatar) {
    this.avatar = avatar;
    Image image = new Image(getClass().getClassLoader().getResourceAsStream(avatar));
    ImageView imageView = new ImageView(image);
    imageView.setFitHeight(100);
    imageView.setFitWidth(100);
    imageView.setPreserveRatio(true);
    super.getChildren().add(imageView);
  }

  public String getAvatar() {
    return avatar;
  }
}
