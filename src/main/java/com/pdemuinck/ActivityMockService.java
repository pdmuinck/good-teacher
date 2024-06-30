package com.pdemuinck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
    this.activities = dataStore.readActivities().stream().map(s -> {
      String name = s.split(",")[0];
      String imageUrl = s.split(",", -1)[1];
      return new Activity(name, imageUrl, 4);
    }).collect(Collectors.toList());
    return activities;
  }

  @Override
  public Activity addActivity(String name) {
    Activity activity = new Activity(name, "", 4);
    if (!activities.contains(activity)) {
      activities.add(activity);
      dataStore.writeActivity(
          String.join(",", activity.getName(), activity.getImageUrl()) + "\r\n");
      return activity;
    } else {
      return activities.stream().filter(a -> a.getName().equals(name)).findFirst().get();
    }
  }

  @Override
  public void updateActivityIcon(String name, String icon) {
    this.activities = fetchActivities();
    String data = activities.stream().distinct().map(a -> {
      if (name.equals(a.getName())) {
        a.setImageUrl(icon);
      }
      return String.join(",", a.getName(), a.getImageUrl());
    }).collect(Collectors.joining("\r\n"));
    dataStore.overWriteActivities(data + "\r\n");
  }

  @Override
  public void saveBoard(List<Activity> activities) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      LocalDate now = LocalDate.now();
      String s = objectMapper.writeValueAsString(activities);
      dataStore.writeActivityBoard(s,
          String.join(File.separator, System.getProperty("user.home"), "AppData", "Roaming",
              "GoodTeacher", String.valueOf(now.getYear()),
              String.valueOf(now.getMonthValue()), String.valueOf(now.getDayOfMonth())),
          String.join("_", String.valueOf(new Date().getTime()), "activities.json"));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }


}
