package com.pdemuinck;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityMockService implements ActivityService {

  private DataStore dataStore;
  private List<Activity> activities = new ArrayList<>();

  public ActivityMockService(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void joinActivity(String name, String avatar) {
    activities.stream().filter(a -> a.getName().equals(name) && a.getAvailableSpots() > 0)
        .findFirst().ifPresent(a -> a.join(
            LocalDateTime.now(), avatar));
    Main.classroomController.updateActivityChange(
        String.format("%s joined activity %s", avatar, name));
  }

  @Override
  public void leaveActivity(String name, String avatar) {
    activities.stream().filter(a -> a.getName().equals(name) && a.getAvailableSpots() > 0)
        .findFirst().ifPresent(a -> a.leave(
            LocalDateTime.now(), avatar));
    Main.classroomController.updateActivityChange(
        String.format("%s left activity %s", avatar, name));
  }

  @Override
  public void startAllActivities() {
    activities.forEach(activity -> activity.start(LocalDateTime.now()));
    Main.classroomController.updateActivityChange("All activities got started");
  }

  @Override
  public List<Activity> fetchActivities() {
    try {
      this.activities = dataStore.fetchActivities().stream().map(s -> {
        String name = s.split(",", -1)[0];
        String imageUrl = s.split(",", -1)[1];
        try {
          int spots = Integer.valueOf(s.split(",", -1)[2]);
          return new Activity(name, imageUrl, spots);
        } catch (NumberFormatException e) {
          return new Activity(name, imageUrl, 0);
        }
      }).collect(Collectors.toList());
      return activities;
    } catch (ArrayIndexOutOfBoundsException e) {
      return new ArrayList<>();
    }
  }

  @Override
  public Activity addActivity(String name) {
    Activity activity = new Activity(name, "", 4);
    if (!activities.contains(activity)) {
      activities.add(activity);
      dataStore.writeActivity(
          String.join(",", activity.getName(), activity.getImageUrl(), "4", "true") + "\r\n");
      return activity;
    } else {
      showActivity(name);
      return activities.stream().filter(a -> a.getName().equals(name)).findFirst().get();
    }
  }

  private void showActivity(String name) {
    String data = activities.stream().distinct().map(a -> {
      if (name.equals(a.getName())) {
        a.setShow(true);
      }
      return String.join(",", a.getName(), a.getImageUrl(), String.valueOf(a.getMaxSpots()),
          String.valueOf(a.isShow()));
    }).collect(Collectors.joining("\r\n"));
    dataStore.overWriteActivities(data + "\r\n");
  }

  @Override
  public void updateActivity(String name, String icon, int spots) {
    this.activities = fetchActivities();
    String data = activities.stream().distinct().map(a -> {
      if (name.equals(a.getName())) {
        a.setImageUrl(icon);
        a.setMaxSpots(spots);
      }
      return String.join(",", a.getName(), a.getImageUrl(), String.valueOf(a.getMaxSpots()),
          String.valueOf(a.isShow()));
    }).collect(Collectors.joining("\r\n"));
    dataStore.overWriteActivities(data + "\r\n");
  }

  @Override
  public void hideActivity(String name) {
    this.activities = fetchActivities();
    String data = activities.stream().distinct().map(a -> {
      if (name.equals(a.getName())) {
        a.setShow(false);
      }
      return String.join(",", a.getName(), a.getImageUrl(), String.valueOf(a.getMaxSpots()),
          String.valueOf(a.isShow()));
    }).collect(Collectors.joining("\r\n"));
    dataStore.overWriteActivities(data + "\r\n");
  }
}
