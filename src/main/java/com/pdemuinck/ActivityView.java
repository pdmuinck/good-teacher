package com.pdemuinck;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ActivityView extends VBox {

  public ActivityView(String name, int spots) {
    this.name = new Label(name);
    this.name.setAlignment(Pos.TOP_CENTER);
    Image image = new Image(getClass().getClassLoader().getResourceAsStream("icons/empty_box.png"));
    this.spots = IntStream.range(0, spots).boxed().map(x -> {
      ImageView imageView1 = new ImageView(image);
      imageView1.setUserData("icons/empty_box.png");
      return imageView1;
    }).collect(
        Collectors.toList());
    super.getChildren().add(this.name);
    GridPane gridPane = new GridPane();
    for(int i = 0; i < spots; i+=2){
      gridPane.add(this.spots.get(i), 0, i / 2, 1, 1);
      if(i + 1 != spots){
        gridPane.add(this.spots.get(i + 1), 1, i / 2, 1, 1);
      }
    }
    super.getChildren().add(gridPane);
  }

  private Label name;

  private List<ImageView> spots;

  public Label getName() {
    return name;
  }

  public void setName(Label name) {
    this.name = name;
  }

  public List<ImageView> getSpots() {
    return spots;
  }

  public void setSpots(List<ImageView> spots) {
    this.spots = spots;
  }
}
