package com.pdemuinck;

public class KidsProfile {
  private String firstName;
  private String lastName;
  private String symbolUri;

  public KidsProfile(String firstName, String lastName, String symbolUri) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.symbolUri = symbolUri;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getSymbolUri() {
    return symbolUri;
  }
}
