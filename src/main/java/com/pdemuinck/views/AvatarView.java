package com.pdemuinck.views;

import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AvatarView extends ImageView {
  private static final int DEFAULT_SIZE = 75;
  public AvatarView(Image image) {
    super(image);
    fitHeightProperty().set(DEFAULT_SIZE);
    fitWidthProperty().set(DEFAULT_SIZE);
  }

  public AvatarView(String path, String name){
    this(new Image(path));
    this.setId(String.join("_", "avatar", name));
  }
}
