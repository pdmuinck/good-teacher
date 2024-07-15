package com.pdemuinck;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class UserDetailView extends Card {

  public UserDetailView(String name, String avatar, Map<String, Optional<Long>> timeByActivity) {
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

    header1.setOnMouseClicked(e -> {
      File selectedFile = new FileChooser().showOpenDialog(this.getScene().getWindow());
      if (selectedFile != null) {
        try {
          FileInputStream fs = new FileInputStream(selectedFile.getAbsolutePath());
          Image image = new Image(fs, 75, 75, false, false);
          ImageView view = new ImageView(image);
          view.setOnMouseClicked(x -> {
            File file = new FileChooser().showOpenDialog(this.getScene().getWindow());
            if(file != null){
              try{
                view.setImage(new Image(new FileInputStream(file.getAbsolutePath()), 75, 75, false, false));
                this.setHeader(new Tile(name, "", view));
                Main.classroomController.saveUser(name, file.getAbsolutePath());
              } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
              }
            }
          });
          this.setHeader(new Tile(name, "", view));
          Main.classroomController.saveUser(name, selectedFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
    this.setHeader(header1);

    if(timeByActivity.isEmpty()){
      this.setBody(new Label("Nog geen registratie van activiteiten"));
    } else {
      ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
      timeByActivity.entrySet().stream().filter(t -> t.getValue().isPresent()).forEach(e -> {
        data.add(new PieChart.Data(e.getKey(), e.getValue().get()));
      });

      var chart = new PieChart(data);
      chart.setMinHeight(300);
      chart.setLegendVisible(false);
      chart.setTitle("Tijdsbesteding");

      this.setBody(chart);
    }
  }
}
