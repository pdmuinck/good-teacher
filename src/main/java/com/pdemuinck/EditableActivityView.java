package com.pdemuinck;

import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class EditableActivityView extends VBox {


  private final String name;

  private final List<ImageView> spots;

  private final ActivityService activityService;

  private String imageUrl;

  public EditableActivityView(String name, String imageUrl, int spots, ActivityService activityService) {
    this.activityService = activityService;
    this.name = name;
    this.imageUrl = imageUrl;

    Button cancel = new Button("", new FontIcon(Feather.X_CIRCLE));
    cancel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
    Button plus = new Button("", new FontIcon(Feather.PLUS));
    cancel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.SUCCESS);
    Button minus = new Button("", new FontIcon(Feather.MINUS));
    cancel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
    Button addImage = new Button("", new FontIcon(Feather.IMAGE));
    cancel.setOnMouseClicked((MouseEvent event) -> {
      Main.classroomController.removeActivity(this);
    });
    Image image = new Image(getClass().getClassLoader().getResourceAsStream("icons/empty_box.png"));
    this.spots = IntStream.range(0, spots).boxed().map(x -> addEmptyBox(image)).collect(
        Collectors.toList());
    var group = new InputGroup(addImage, plus, minus, cancel);
    plus.setOnMouseClicked((MouseEvent event) -> {
      this.spots.add(addEmptyBox(image));
      activityService.updateActivity(name, this.imageUrl, this.spots.size());
      super.getChildren().clear();
      super.getChildren().add(group);
      prepActivityImage().map(i -> super.getChildren().add(i));
      super.getChildren().add(fillSpotPane());
    });
    minus.setOnMouseClicked((MouseEvent event) -> {
      if (!this.spots.isEmpty()) {
        this.spots.removeLast();
        activityService.updateActivity(name, this.imageUrl, this.spots.size());
        super.getChildren().clear();
        super.getChildren().add(group);
        prepActivityImage().map(i -> super.getChildren().add(i));
        super.getChildren().add(fillSpotPane());
      }
    });

    addImage.setOnMouseClicked((MouseEvent event) -> {
      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showOpenDialog(this.getScene().getWindow());
      if(file != null){
        activityService.updateActivity(name, file.getAbsolutePath(), this.spots.size());
        this.imageUrl = file.getAbsolutePath();
        try {
          Image icon = new Image(new FileInputStream(file.getAbsolutePath()), 150, 150, false, false);
          ImageView activityImage = new ImageView(icon);
          activityImage.setOnMouseClicked((MouseEvent e) -> {
            File newImage = fileChooser.showOpenDialog(this.getScene().getWindow());
            if(newImage != null){
              activityService.updateActivity(name, newImage.getAbsolutePath(), this.spots.size());
              this.imageUrl = newImage.getAbsolutePath();
              Image newIcon = null;
              try {
                newIcon =
                    new Image(new FileInputStream(newImage.getAbsolutePath()), 150, 150, false, false);
              } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
              }
              activityImage.setImage(newIcon);
            }
          });
          super.getChildren().clear();
          super.getChildren().add(group);
          super.getChildren().add(activityImage);
          super.getChildren().add(fillSpotPane());
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    });
    super.getChildren().add(group);
    if (!imageUrl.isBlank()) {
      Image icon = null;
      try {
        icon = new Image(new FileInputStream(imageUrl), 150, 150, false, false);
        ImageView activityImage = new ImageView(icon);
        activityImage.setOnMouseClicked((MouseEvent event) -> {
          FileChooser fileChooser = new FileChooser();
          File file = fileChooser.showOpenDialog(this.getScene().getWindow());
          if(file != null){
            activityService.updateActivity(name, file.getAbsolutePath(), this.spots.size());
            this.imageUrl = file.getAbsolutePath();
            try {
              Image newIcon = new Image(new FileInputStream(file.getAbsolutePath()), 150, 150, false, false);
              activityImage.setImage(newIcon);
            } catch (FileNotFoundException e) {
              throw new RuntimeException(e);
            }
          }
        });
        super.getChildren().add(activityImage);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      super.getChildren().add(new Label(name));
    }
    super.getChildren().add(fillSpotPane());
  }

  private GridPane fillSpotPane() {
    GridPane gridPane = new GridPane();
    for (int i = 0; i < spots.size(); i += 2) {
      gridPane.add(this.spots.get(i), 0, i / 2, 1, 1);
      if (i + 1 != spots.size()) {
        gridPane.add(this.spots.get(i + 1), 1, i / 2, 1, 1);
      }
    }
    return gridPane;
  }

  private Optional<ImageView> prepActivityImage(){
    if (!this.imageUrl.isBlank()) {
      Image icon = null;
      try {
        icon = new Image(new FileInputStream(this.imageUrl), 150, 150, false, false);
        return Optional.of(new ImageView(icon));
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return Optional.empty();
  }

  private ImageView addEmptyBox(Image basic) {
    ImageView imageView1 = new ImageView(basic);
    imageView1.setUserData("icons/empty_box.png");
    return imageView1;
  }

  public String getName() {
    return name;
  }

  public List<ImageView> getSpots() {
    return spots;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}
