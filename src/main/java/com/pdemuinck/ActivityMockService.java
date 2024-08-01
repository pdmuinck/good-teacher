package com.pdemuinck;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActivityMockService implements ActivityService {

  private DataStore dataStore;
  private List<Activity> activities = new ArrayList<>();

  public ActivityMockService(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public List<String> fetchBlackList(String activity) {
    return  dataStore.fetchBlackLists().stream().filter(s -> s.startsWith(activity))
        .map(s -> s.split(",")[1]).toList();
  }

  @Override
  public void joinActivity(String activityName, String userName) {
    List<String>
        blackLists = dataStore.fetchBlackLists().stream().filter(s -> s.startsWith(activityName))
        .map(s -> Arrays.asList(s.split(","))).flatMap(Collection::stream).toList();
    activities.forEach(a -> {
      if (a.getName().equals(activityName)) {
        a.setBlackList(blackLists);
        if(!a.isOnBlackList(userName)){
          a.join(LocalDateTime.now(), userName);
        }
      }
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
  public List<Activity> getActivities() {
    return this.activities;
  }

  @Override
  public void removeFromBlackList(String activity, String u) {
    String data =
        dataStore.fetchBlackLists().stream().filter(b -> !b.contains(activity) || !b.contains(u))
            .collect(
                Collectors.joining("\r\n"));
    dataStore.overwriteBlackList(data + "\r\n");
  }

  @Override
  public List<String> getParticipants(String name) {
    Optional<Activity> activity =
        this.activities.stream().filter(a -> a.getName().equals(name)).findFirst();
    return activity.map(Activity::participants).orElse(new ArrayList<>());
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
      activities =
          activities.stream().filter(a -> !a.getName().isBlank()).collect(Collectors.toList());
      return activities;
    } catch (ArrayIndexOutOfBoundsException e) {
      return new ArrayList<>();
    }
  }

  @Override
  public Activity addActivity(String name) {
    Activity activity = new Activity(name, "", 4);
    return addActivity(activity);
  }

  private Activity addActivity(Activity activity) {
    if (this.activities.stream().noneMatch(a -> a.getName().equals(activity.getName()))) {
      this.activities.add(activity);
      dataStore.writeActivity(
          String.join(",", activity.getName(), activity.getImageUrl(), "4", "true") + "\r\n");
      return activity;
    } else {
      return showActivity(activity.getName()).orElse(activity);
    }
  }

  @Override
  public Activity addActivity(String name, String imageUrl, int spots) {
    Activity activity = new Activity(name, imageUrl, spots);
    return addActivity(activity);
  }

  private Optional<Activity> showActivity(String name) {
    String data = activities.stream().distinct().map(a -> {
      if (a.getName().equals(name)) {
        a.setShow(true);
      }
      return String.join(",", a.getName(), a.getImageUrl(), String.valueOf(a.getMaxSpots()),
          String.valueOf(a.isShow()));
    }).collect(Collectors.joining("\r\n"));
    dataStore.overWriteActivities(data);
    return activities.stream().filter(a -> a.getName().equals(name)).findFirst();
  }

  @Override
  public void updateActivity(String name, String icon, int spots) {
    List<String> data = new ArrayList<>();
    for (Activity activity : activities) {
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
    List<String> timings = dataStore.fetchActivityTime().stream().filter(l -> l.contains(name)).toList();
    List<TimeReportRow> rows = timings.stream().map(this::parse).collect(Collectors.toList());
    return rows.stream().filter(r -> r.getUserName().equals(name)).collect(Collectors.toList());
  }

  @Override
  public Map<String, Optional<Long>> timeByActivity(String name) {
    List<TimeReportRow> timeReportRows = fetchTimeReport(name);
    Map<String, Map<String, Optional<TimeReportRow>>> collect = timeReportRows.stream().collect(
        groupingBy(TimeReportRow::getActivityName, groupingBy(TimeReportRow::getSession,
            maxBy(Comparator.comparingLong(TimeReportRow::getTime)))));

    return collect.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
        e -> e.getValue().values().stream()
            .filter(Optional::isPresent)
            .map(x -> x.get().getTime())
            .reduce(Long::sum)));
  }

  @Override
  public void addToBlackList(String activity, String user) {
    dataStore.saveBlacklist(String.join(",", activity, user) + "\r\n");
  }

  private TimeReportRow parse(String data) {
    String[] split = data.split(",", -1);
    return new TimeReportRow(split[0].trim(), split[1].trim(), split[2].trim(), LocalDate.parse(split[3].trim()),
        Long.valueOf(split[4].trim()));
  }
}
