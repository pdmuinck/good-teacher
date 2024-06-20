package com.pdemuinck;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ActivityView extends VBox {

  private Label name;

  private List<ImageView> spots;

  private ActivityService activityService = new ActivityMockService();

  public ActivityView(String name, int spots) {
    this.name = new Label(name);
    this.name.setAlignment(Pos.TOP_CENTER);
    Text cancel = new Text("x");
    cancel.setOnMouseClicked((MouseEvent event) -> {
      Main.classroomController.removeActivity(this);
    });
    Image image = new Image(getClass().getClassLoader().getResourceAsStream("icons/empty_box.png"));
    this.spots = IntStream.range(0, spots).boxed().map(x -> prepareImageView(image)).collect(
        Collectors.toList());
    Text plus = new Text("+");
    Text minus = new Text("-");
    plus.setOnMouseClicked((MouseEvent event) -> {
      this.spots.add(prepareImageView(image));
      super.getChildren().clear();
      HBox box = new HBox();
      box.getChildren().addAll(this.name, cancel, plus, minus);
      super.getChildren().add(box);
      super.getChildren().add(fillSpotPane());
    });
    minus.setOnMouseClicked((MouseEvent event) -> {
      if(this.spots.size() > 0){
        this.spots.removeLast();
        super.getChildren().clear();
        HBox box = new HBox();
        box.getChildren().addAll(this.name, cancel, plus, minus);
        super.getChildren().add(box);
        super.getChildren().add(fillSpotPane());
      }
    });

    HBox box = new HBox();
    box.getChildren().addAll(this.name, cancel, plus, minus);
    super.getChildren().add(box);
    super.getChildren().add(fillSpotPane());
  }

  private GridPane fillSpotPane(){
    GridPane gridPane = new GridPane();
    for (int i = 0; i < spots.size(); i += 2) {
      gridPane.add(this.spots.get(i), 0, i / 2, 1, 1);
      if (i + 1 != spots.size()) {
        gridPane.add(this.spots.get(i + 1), 1, i / 2, 1, 1);
      }
    }
    return gridPane;
  }

  private ImageView prepareImageView(Image basic) {
    ImageView imageView1 = new ImageView(basic);
    imageView1.setUserData("icons/empty_box.png");
    imageView1.setOnDragDetected((MouseEvent event) -> {
      Dragboard db = imageView1.startDragAndDrop(TransferMode.ANY);
      db.setDragView(imageView1.getImage());
      ClipboardContent content = new ClipboardContent();
      content.putString((String) imageView1.getUserData());
      db.setContent(content);
      activityService.leaveActivity(this.name.getText(), (String) imageView1.getUserData());
      imageView1.setImage(basic);
      imageView1.setUserData("icons/empty_box.png");
    });
    imageView1.setOnDragOver((DragEvent event) -> {
      if (event.getGestureSource() != imageView1 && event.getDragboard().hasString() && imageView1.getUserData().equals("icons/empty_box.png")) {
        event.acceptTransferModes(TransferMode.ANY);
      }
      event.consume();
    });
    imageView1.setOnDragDropped((DragEvent event) -> {
      Dragboard db = event.getDragboard();
      if (db.hasString()) {
        if (imageView1.getUserData().equals("icons/empty_box.png")) {
          Image image2 =
              new Image(this.getClass().getClassLoader().getResourceAsStream(db.getString()), 75, 75, false, false);
          imageView1.setImage(image2);
          imageView1.setPreserveRatio(true);
          imageView1.setUserData(db.getString());
          activityService.joinActivity(this.name.getText(), db.getString());
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
