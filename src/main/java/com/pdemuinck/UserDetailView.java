package com.pdemuinck;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import com.pdemuinck.views.AvatarView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
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

  private UserService userService;

  public UserDetailView(String name, String avatar, Map<String, Optional<Long>> timeByActivity,
                        UserService userService) {
    this(name, avatar, timeByActivity, new FileChooser(), userService);
  }

  public UserDetailView(String name, String avatar, Map<String, Optional<Long>> timeByActivity,
                        FileChooser fileChooser, UserService userService) {
    super();
    this.getStyleClass().add(Styles.ELEVATED_1);
    this.setMinWidth(500);
    this.setMaxWidth(500);
    this.setMaxHeight(500);
    this.setMinHeight(500);

    ImageView avatarView = new ImageView();
    if (!avatar.isBlank()) {
      avatarView = new AvatarView(avatar, name);
      avatarView.setOnMouseClicked((MouseEvent event) -> {
        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (selectedFile != null) {
          try {
            ImageView imageView =
                new AvatarView(selectedFile.toURI().toURL().toExternalForm(), name);
            this.setHeader(new Tile(name, "", imageView));
            userService.addUser(name, imageView.getImage().getUrl());
          } catch (MalformedURLException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }
    Tile header1 = new Tile(name, "", avatarView);
    header1.setId(String.join("_", "user_detail_header", name));

    header1.setOnMouseClicked(e -> {
      File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
      if (selectedFile != null) {
        try {
          ImageView view = new AvatarView(selectedFile.toURI().toURL().toExternalForm(), name);
          view.setOnMouseClicked(x -> {
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
              try {
                ImageView source = (ImageView) x.getSource();
                loadNewImage((ImageView) x.getSource(), file);
                this.setHeader(new Tile(name, "", source));
                userService.addUser(name, source.getImage().getUrl());
              } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
              }
            }
          });
          this.setHeader(new Tile(name, "", view));
          userService.addUser(name, view.getImage().getUrl());
        } catch (MalformedURLException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
    this.setHeader(header1);

    if (timeByActivity.isEmpty()) {
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
      chart.setId(String.join("_", "activity_timing", name));

      this.setBody(chart);
    }
  }

  private void loadNewImage(ImageView imageView, File selectedFile) throws MalformedURLException {
    String externalForm = selectedFile.toURI().toURL().toExternalForm();
    Image image = new Image(externalForm, 75, 75, false, false);
    imageView.setImage(image);
  }
}
