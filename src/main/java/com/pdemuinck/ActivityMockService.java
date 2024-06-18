package com.pdemuinck;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityMockService implements ActivityService {

  private List<Activity> activities =
      List.of(new Activity("drawing", 4), new Activity("puzzles", 3));

  @Override
  public void joinActivity(String name, String avatar) {
    activities.stream().filter(a -> a.getName().equals(name) && a.getAvailableSpots() > 0).findFirst().ifPresent(a -> a.join(
        LocalDateTime.now(), avatar));
    Main.classroomController.updateActivityChange(String.format("%s joined activity %s", avatar, name));
  }

  @Override
  public void leaveActivity(String name, String avatar) {
    activities.stream().filter(a -> a.getName().equals(name) && a.getAvailableSpots() > 0).findFirst().ifPresent(a -> a.leave(
        LocalDateTime.now(), avatar));
    Main.classroomController.updateActivityChange(String.format("%s left activity %s", avatar, name));
  }

  @Override
  public void startAllActivities() {
    activities.forEach(activity -> activity.start(LocalDateTime.now()));
    Main.classroomController.updateActivityChange("All activities got started");
  }

  @Override
  public List<Activity> fetchActivities() {
    return activities;
  }
}
