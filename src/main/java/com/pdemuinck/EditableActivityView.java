package com.pdemuinck;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Tile;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class EditableActivityView extends VBox {


  private final String name;

  private final List<ImageView> spots;

  private final ActivityService activityService;

  private final FileSystemService fileSystemService;

  private final ClassroomController classroomController;

  private final UserService userService;

  private String imageUrl;

  public EditableActivityView(String name, String imageUrl, int spots,
                              ActivityService activityService,
                              FileSystemService fileSystemService,
                              ClassroomController classroomController,
                              UserService userService) {
    this.activityService = activityService;
    this.fileSystemService = fileSystemService;
    this.classroomController = classroomController;
    this.userService = userService;
    this.name = name;
    this.imageUrl = imageUrl;

    Button cancel = new Button("", new FontIcon(Feather.X_CIRCLE));
    cancel.setId("remove_activity_" + name);
    cancel.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER);
    cancel.setOnMouseClicked((MouseEvent event) -> {
      classroomController.removeActivity(this);
    });

    Button plus = new Button("", new FontIcon(Feather.PLUS));
    plus.setId(String.join("_", "add_spot_for", name));

    Button minus = new Button("", new FontIcon(Feather.MINUS));
    minus.setId(String.join("_", "remove_spot_for", name));

    Button addImage = new Button("", new FontIcon(Feather.IMAGE));
    addImage.setId("add_image_for_" + name);

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
      super.getChildren().add(blackList());
    });
    minus.setOnMouseClicked((MouseEvent event) -> {
      if (!this.spots.isEmpty()) {
        this.spots.remove(this.spots.get(this.spots.size() - 1));
        activityService.updateActivity(name, this.imageUrl, this.spots.size());
        super.getChildren().clear();
        super.getChildren().add(group);
        prepActivityImage().map(i -> super.getChildren().add(i));
        super.getChildren().add(fillSpotPane());
        super.getChildren().add(blackList());
      }
    });

    addImage.setOnMouseClicked((MouseEvent event) -> {
      String file = fileSystemService.openFile(this.getScene().getWindow());
      if (file != null) {
        activityService.updateActivity(name, file, this.spots.size());
        this.imageUrl = file;
        Image icon = new Image(file, 150, 150, false, false);
        ImageView activityImage = new ImageView(icon);
        activityImage.setId("image_for_" + name);
        activityImage.setOnMouseClicked((MouseEvent e) -> {
          String newImage = fileSystemService.openFile(this.getScene().getWindow());
          if (newImage != null) {
            activityService.updateActivity(name, newImage, this.spots.size());
            this.imageUrl = newImage;
          }
          Image newIcon = null;
          newIcon =
              new Image(newImage, 150,
                  150, false, false);
          activityImage.setImage(newIcon);
        });
        super.getChildren().clear();
        super.getChildren().add(group);
        super.getChildren().add(activityImage);
        super.getChildren().add(fillSpotPane());
        super.getChildren().add(blackList());
      }
    });
    super.getChildren().add(group);

    if (!imageUrl.isBlank()) {
      Image icon = null;
      icon = new Image(imageUrl, 150, 150, false, false);
      ImageView activityImage = new ImageView(icon);
      activityImage.setId("image_for_" + name);
      activityImage.setOnMouseClicked((MouseEvent event) -> {
        String file = fileSystemService.openFile(this.getScene().getWindow());
        if (file != null) {
          activityService.updateActivity(name, file,
              this.spots.size());
          this.imageUrl = file;
          Image newIcon =
              new Image(file, 150, 150,
                  false, false);
          activityImage.setImage(newIcon);
        }
      });
      super.getChildren().add(activityImage);
    } else {
      super.getChildren().add(new Label(name));
    }
    super.getChildren().add(fillSpotPane());
    super.getChildren().add(blackList());
  }

  private Card blackList() {
    var card = new Card();
    card.getStyleClass().add(Styles.ELEVATED_1);
    card.setMinWidth(300);
    card.setMaxWidth(300);

    var header2 = new Tile(
        "Black list",
        "Weiger deelnemers tot deelname aan deze activiteit"
    );
    card.setHeader(header2);

    var tf2 = new CustomTextField();
    tf2.setPromptText("Zoek persoon");
    tf2.setLeft(new FontIcon(Feather.SEARCH));
    tf2.setOnKeyReleased(e -> {
      List<String> strings = classroomController.fetchUsers(tf2.getText());
      List<String> blackList = activityService.fetchBlackList(this.getName());
      var body2 = new VBox(10);
      card.setBody(body2);
      strings.stream().forEach(u -> {
        var cb = new CheckBox();
        if (blackList.contains(u)) {
          cb.setSelected(true);
        }
        cb.setOnMouseClicked(r -> {
          if (cb.isSelected()) {
            this.activityService.addToBlackList(this.getName(), u);
          } else {
            this.activityService.removeFromBlackList(this.getName(), u);
          }
        });
        var lbl = new Label(u);
        var circle = new Circle(
            8, Color.web("red"));
        HBox hBox = new HBox(10, circle, cb, lbl);
        hBox.setAlignment(Pos.CENTER_LEFT);
        body2.getChildren().add(hBox);
      });
    });

    card.setSubHeader(tf2);

    var body2 = new VBox(10);
    card.setBody(body2);
    List<String> strings = activityService.fetchBlackList(this.getName());
    strings.stream().forEach(u -> {
      var cb = new CheckBox();
      cb.setSelected(true);
      cb.setOnMouseClicked(r -> {
        if (cb.isSelected()) {
          this.activityService.addToBlackList(this.getName(), u);
        } else {
          this.activityService.removeFromBlackList(this.getName(), u);
        }
      });
      var lbl = new Label(u);
      var circle = new Circle(
          8, Color.web("red"));
      HBox hBox = new HBox(10, circle, cb, lbl);
      hBox.setAlignment(Pos.CENTER_LEFT);
      body2.getChildren().add(hBox);
    });
    return card;
  }

  private GridPane fillSpotPane() {
    GridPane gridPane = new GridPane();
    gridPane.setId("grid_for_" + this.getName());
    for (int i = 0; i < spots.size(); i += 4) {
      gridPane.add(this.spots.get(i), 0, i, 1, 1);
      if (i + 1 != spots.size()) {
        gridPane.add(this.spots.get(i + 1), 1, i, 1, 1);
      }
      if (i + 2 < spots.size()) {
        gridPane.add(this.spots.get(i + 2), 2, i, 1, 1);
      }
      if (i + 3 < spots.size()) {
        gridPane.add(this.spots.get(i + 3), 3, i, 1, 1);
      }
    }
    return gridPane;
  }

  private Optional<ImageView> prepActivityImage() {
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
