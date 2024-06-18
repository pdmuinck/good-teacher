package com.pdemuinck;

public class User {
  private String firstName;
  private String lastName;
  private String avatar;

  public User(String firstName, String lastName, String avatar) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.avatar = avatar;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getAvatar() {
    return avatar;
  }
}
