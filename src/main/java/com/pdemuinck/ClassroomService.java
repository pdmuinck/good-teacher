package com.pdemuinck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassroomService {

  public List<Activity> fetchActivities() {
    return Arrays.asList(new Activity("drawing", 4), new Activity("puzzles", 4));
  }

  public List<KidsProfile> fetchKids() {
    return Arrays.asList(new KidsProfile("Charlie", "Last", "CH"),
        new KidsProfile("Peppa", "Pig", "PP"));
  }
}
