package com.pdemuinck;

import java.time.LocalDate;
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
  public void joinActivity(String activityName, String userName) {
    activities = fetchActivities();
    activities.stream().filter(a -> a.getName().equals(activityName) && a.getAvailableSpots() > 0)
        .findFirst().ifPresent(a -> a.join(
            LocalDateTime.now(), userName));
  }

  @Override
  public void leaveActivity(String name, String avatar) {
    activities.stream().filter(a -> a.getName().equals(name) && a.getAvailableSpots() > 0)
        .findFirst().ifPresent(a -> a.leave(
            LocalDateTime.now(), avatar));
  }

  @Override
  public void startAllActivities() {
    activities.forEach(activity -> activity.start(LocalDateTime.now()));
  }

  @Override
  public void pauseAllActivities() {
    activities.forEach(activity -> activity.pause(LocalDateTime.now()));
    String data =
        activities.stream().map(a -> a.getDurationByKid()).collect(Collectors.joining("\r\n"));
    dataStore.saveActivityTime(data);
  }


  @Override
  public List<Activity> fetchActivities() {
    try {
      this.activities = dataStore.fetchActivities().stream().map(s -> {
        String name = s.split(",", -1)[0];
        String imageUrl = s.split(",", -1)[1];
        try {
          int spots = Integer.valueOf(s.split(",", -1)[2]);
          boolean show = Boolean.valueOf(s.split(",", -1)[3]);
          return new Activity(name, imageUrl, spots, show);
        } catch (Exception e) {
          return new Activity(name, imageUrl, 0);
        }
      }).collect(Collectors.toList());
      activities = activities.stream().filter(a -> !a.getName().isBlank()).toList();
      return activities;
    } catch (ArrayIndexOutOfBoundsException e) {
      return new ArrayList<>();
    }
  }

  @Override
  public Activity addActivity(String name) {
    Activity activity = new Activity(name, "", 4);
    addActivity(activity);
    return activity;
  }

  private void addActivity(Activity activity) {
    if (!activities.contains(activity)) {
      activities.add(activity);
      dataStore.writeActivity(
          String.join(",", activity.getName(), activity.getImageUrl(), "4", "true") + "\r\n");
    } else {
      showActivity(activity.getName());
    }
  }

  @Override
  public Activity addActivity(String name, String imageUrl, int spots) {
    Activity activity = new Activity(name, imageUrl, spots);
    addActivity(activity);
    return activity;
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
    String data = this.activities.stream().distinct().map(a -> {
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

  @Override
  public List<TimeReportRow> fetchTimeReport(String name) {
    List<String> timings = dataStore.fetchActivityTime();
    return timings.stream().map(this::parse).collect(Collectors.toList());
  }

  private TimeReportRow parse(String data){
    String[] split = data.split(",", -1);
    return new TimeReportRow(split[0], split[1], LocalDate.parse(split[2]), Long.valueOf(split[3]));
  }
}
