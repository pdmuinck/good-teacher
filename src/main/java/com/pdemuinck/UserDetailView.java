package com.pdemuinck;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class UserDetailView extends Card {
  public UserDetailView(String name, String avatar) {
    super();
    this.getStyleClass().add(Styles.ELEVATED_1);
    this.setMinWidth(500);
    this.setMaxWidth(500);
    this.setMaxHeight(500);
    this.setMinHeight(500);

    ImageView avatarView = new ImageView();
    if(!avatar.isBlank()){
      try {
        avatarView = new ImageView(new Image(new FileInputStream(avatar), 75, 75, false, false));
        avatarView.setOnMouseClicked((MouseEvent event) -> {
          FileChooser fileChooser = new FileChooser();
          File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
          if (selectedFile != null) {
            FileInputStream fs = null;
            try {
              fs = new FileInputStream(selectedFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
              throw new RuntimeException(e);
            }
            Image image = new Image(fs, 75, 75, false, false);
            this.setHeader(new Tile(name, "", new ImageView(image)));
            Main.classroomController.saveUser(name, selectedFile.getAbsolutePath());
          }
        });
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    var header1 = new Tile(name, "", avatarView);
    this.setHeader(header1);


    ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
        new PieChart.Data("tekenen", 10),
        new PieChart.Data("knutselen", 20),
        new PieChart.Data("ipads", 40),
        new PieChart.Data("bouwen", 45)
    );

    var chart = new PieChart(data);
    chart.setMinHeight(300);
    chart.setLegendVisible(false);
    chart.setTitle("Tijdsbesteding");

    this.setBody(chart);
  }
}
