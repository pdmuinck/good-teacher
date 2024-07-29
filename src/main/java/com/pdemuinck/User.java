package com.pdemuinck;

import java.util.Objects;

public class User {
  private String name;
  private String avatar;

  public User(String name, String avatar) {
    this.name = name;
    this.avatar = avatar;
  }


  public String getAvatar() {
    return avatar;
  }

  public String getName() {
    return name;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(name, user.name) && Objects.equals(avatar, user.avatar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, avatar);
  }
}
