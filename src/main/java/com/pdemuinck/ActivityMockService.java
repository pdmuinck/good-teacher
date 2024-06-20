package com.pdemuinck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityMockService implements ActivityService {

  private DataStore dataStore = new FileDataStore();
  private List<Activity> activities = new ArrayList<>();

  public ActivityMockService(){
    fetchActivities();
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
      return new Activity(name, imageUrl);
    }).collect(Collectors.toList());
    return activities;
  }

  @Override
  public void addActivity(String name) {
    Activity activity = new Activity(name, "");
    if(!activities.contains(activity)){
      activities.add(activity);
      dataStore.writeActivity(String.join(",", activity.getName(), activity.getImageUrl()) + "\r\n");
    }
  }

  @Override
  public void saveBoard(List<Activity> activities) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String s = objectMapper.writeValueAsString(activities);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }


}
