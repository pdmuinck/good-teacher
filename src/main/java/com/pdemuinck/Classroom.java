package com.pdemuinck;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Classroom {

  private final List<Activity> activities = new ArrayList<>();
  private final List<User> kids;
  private int totalSpots = 0;

  public Classroom(List<User> kids) {
    this.kids = kids;
  }

  public void addActivity(Activity activity) {
    activities.add(activity);
    totalSpots += activity.getMaxSpots();
  }

  public void removeActivity(String name) {
    activities.stream().filter(act -> act.getName().equals(name)).findFirst().ifPresent(act -> {
      totalSpots -= act.getMaxSpots();
      activities.remove(act);
    });
  }

  public void startAllActivities(){
    activities.stream().forEach(act -> act.start(LocalDateTime.now()));
  }

  public List<Activity> getActivities() {
    return activities;
  }

  public List<User> getKids() {
    return kids;
  }

  public int getTotalSpots() {
    return totalSpots;
  }
}
