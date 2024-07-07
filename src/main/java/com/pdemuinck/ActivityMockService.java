package com.pdemuinck;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    List<String>
        blackLists = dataStore.fetchBlackLists().stream().filter(s -> s.startsWith(activityName))
        .map(s -> Arrays.asList(s.split(","))).flatMap(Collection::stream).toList();
    activities.forEach(a -> {
      if(a.getName().equals(activityName)){
        a.setBlackList(blackLists);
      }
      a.join(LocalDateTime.now(), userName);
    });
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
        activities.stream().map(Activity::getDurationByKid).collect(Collectors.joining("\r\n"));
    dataStore.saveActivityTime(data + "\r\n");
  }

  @Override
  public List<Activity> getActivities(){
    return this.activities;
  }


  @Override
  public List<Activity> fetchActivities() {
    try {
      this.activities = dataStore.fetchActivities().stream().map(s -> {
        String[] split = s.split(",", -1);
        String name = split[0];
        String imageUrl = split[1];
        try {
          int spots = 0;
          if (split.length == 3) {
            spots = Integer.valueOf(split[2]);
          }
          if (split.length == 4) {
            spots = Integer.valueOf(split[2]);
            boolean show = Boolean.valueOf(split[3]);
            return new Activity(name, imageUrl, spots, show);
          } else {
            return new Activity(name, imageUrl, spots);
          }
        } catch (NumberFormatException e) {
          return new Activity(name, imageUrl, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList());
      activities = activities.stream().filter(a -> !a.getName().isBlank()).collect(Collectors.toList());
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
    if (this.activities.stream().noneMatch(a -> a.getName().equals(activity.getName()))) {
      this.activities.add(activity);
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
    List<String> data = new ArrayList<>();
    for(Activity activity : activities){
      if (name.equals(activity.getName())) {
        activity.setImageUrl(icon);
        activity.setMaxSpots(spots);
      }
      data.add(String.join(",", activity.getName(), activity.getImageUrl(),
          String.valueOf(activity.getMaxSpots()),
          String.valueOf(activity.isShow())));
    }
    dataStore.overWriteActivities(String.join("\r\n", data) + "\r\n");
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
    List<TimeReportRow> rows = timings.stream().map(this::parse).collect(Collectors.toList());
    return rows.stream().filter(r -> r.getUserName().equals(name)).collect(Collectors.toList());
  }

  @Override
  public void addToBlackList(String activity, String user) {
    dataStore.saveBlacklist(String.join(",", activity, user) + "\r\n");
  }

  private TimeReportRow parse(String data) {
    String[] split = data.split(",", -1);
    return new TimeReportRow(split[0], split[1], LocalDate.parse(split[2]), Long.valueOf(split[3]));
  }
}
