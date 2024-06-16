package com.pdemuinck;

public class KidsProfile {
  private String firstName;
  private String lastName;
  private String avatar;

  public KidsProfile(String firstName, String lastName, String avatar) {
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
