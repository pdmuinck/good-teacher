package com.pdemuinck;

import atlantafx.base.layout.InputGroup;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class EditableUserView extends TitledPane {

  private String avatar;
  private String name;
  private ImageView imageView;

  public EditableUserView(String name, String avatar, Map<String, Optional<Long>> timeByActivity) {
    super(name, new InputGroup(new UserDetailView(name, avatar, timeByActivity)));
    this.name = name;
    this.avatar = avatar;
    this.imageView = new ImageView();
    if (avatar.isBlank()) {
      Label label = new Label(name);

      FileChooser fileChooser = new FileChooser();
      label.setOnMouseClicked(e -> {
        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (selectedFile != null) {
          this.avatar = selectedFile.getAbsolutePath();
          try {
            FileInputStream fs = new FileInputStream(selectedFile.getAbsolutePath());
            Image image = new Image(fs, 75, 75, false, false);
            imageView = new ImageView(image);
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
        super.getChildren().add(imageView);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    this.imageView.setOnMouseClicked((MouseEvent event) -> {
      FileChooser fileChooser = new FileChooser();
      File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
      if (selectedFile != null) {
        this.avatar = selectedFile.getAbsolutePath();
        FileInputStream fs = null;
        try {
          fs = new FileInputStream(selectedFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
        Image image = new Image(fs, 75, 75, false, false);
        imageView = new ImageView(image);
        Tooltip tp = new Tooltip(name);
        tp.setShowDelay(Duration.millis(100));
        tp.setShowDuration(Duration.millis(1500));
        Tooltip.install(imageView, tp);
        super.getChildren().clear();
        super.getChildren().add(imageView);
        Main.classroomController.saveUsers();
      }
    });
  }

  public String getAvatar() {
    return avatar;
  }

  public String getName() {
    return name;
  }
}
