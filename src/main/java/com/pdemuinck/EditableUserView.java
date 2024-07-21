package com.pdemuinck;

import atlantafx.base.layout.InputGroup;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

public class EditableUserView extends TitledPane {

  private String avatar;
  private String name;

  public EditableUserView(String name, String avatar, Map<String, Optional<Long>> timeByActivity, UserService userService) {
    super(name, new InputGroup(new UserDetailView(name, avatar, timeByActivity, userService)));
    this.name = name;
    this.avatar = avatar;
    Label label = new Label(name);
    super.getChildren().add(label);
  }

  public String getAvatar() {
    return avatar;
  }

  public String getName() {
    return name;
  }
}
