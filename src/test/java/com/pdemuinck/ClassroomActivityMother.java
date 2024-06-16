package com.pdemuinck;

public class ClassroomActivityMother {

  private ClassroomActivityMother(){}

  public static Builder activity(){
    return new Builder();
  }

  public static class Builder{

    String name = "";
    int maxSpots = 0;

    public Builder withName(String name){
      this.name = name;
      return this;
    }

    public Builder withMaxSpots(int spots){
      this.maxSpots = spots;
      return this;
    }

    public ClassroomActivity build(){
      return new ClassroomActivity(name, maxSpots);
    }
  }
}

