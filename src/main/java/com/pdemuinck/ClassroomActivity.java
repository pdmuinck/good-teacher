package com.pdemuinck;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassroomActivity {
  private final String name;
  private int availableSpots;
  private final int maxSpots;
  private final List<ActivityEvent> events = new ArrayList<>();
  private LocalDateTime firstStartTs, lastStartTs, endTs;
  private long totalDuration = 0;
  private final Map<String, LocalDateTime> joinTimeByKid = new HashMap<>();
  private final Map<String, Long> durationByKid = new HashMap<>();
  private final Map<String, ActivityFeedback> feedbackByKid = new HashMap<>();
  private List<String> blackList = new ArrayList<>();

  public ClassroomActivity(String name, int maxSpots) {
    this.name = name;
    this.maxSpots = maxSpots;
    this.availableSpots = maxSpots;
  }

  public void start(LocalDateTime eventTs) {
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_STARTED));
    if (lastStartTs == null && firstStartTs == null) {
      firstStartTs = lastStartTs = eventTs;
    } else {
      lastStartTs = eventTs;
    }
  }

  public void pause(LocalDateTime eventTs) {
    if (eventTs.isBefore(lastStartTs)) {
      throw new RuntimeException("Cannot accept a pause event that is before the start event");
    }
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_PAUSED));
    endTs = eventTs;
    totalDuration += Duration.between(lastStartTs, endTs).toMinutes();
    joinTimeByKid.forEach((name, startTs) -> {
      if(startTs.isAfter(lastStartTs)){
        durationByKid.computeIfPresent(name,
            (key, duration) -> duration + Duration.between(startTs, eventTs).toMinutes());
      } else {
        durationByKid.computeIfPresent(name,
            (key, duration) -> duration + Duration.between(lastStartTs, eventTs).toMinutes());
      }
    });
  }

  public void join(LocalDateTime eventTs, String name) {
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_JOINED));
    if(blackList.contains(name)){
      events.add(new ActivityEvent(eventTs,ActivityEventType.ACTIVITY_NOT_ALLOWED));
      throw new RuntimeException("Kid is not allowed to join");
    }
    if (availableSpots > 0) {
      joinTimeByKid.put(name, eventTs);
      durationByKid.putIfAbsent(name, 0L);
      availableSpots--;
    } else {
      throw new RuntimeException("Cannot add kid when activity is full.");
    }
  }

  public void leave(LocalDateTime eventTs, String name){
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_LEFT));
    availableSpots++;
    LocalDateTime joinTs = joinTimeByKid.get(name);
    if(lastStartTs != null){
      if (endTs == null || !endTs.isBefore(eventTs)) {
        if(joinTs.isBefore(eventTs) && joinTs.isAfter(lastStartTs)){
          durationByKid.computeIfPresent(name, (key, duration) -> duration + Duration.between(joinTs, eventTs).toMinutes());
        } else {
          durationByKid.computeIfPresent(name, (key, duration) -> duration + Duration.between(lastStartTs, eventTs).toMinutes());
        }
      }
    }
  }

  public void feedback(LocalDateTime eventTs, String name, ActivityFeedback feedback){
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_FEEDBACK));
    feedbackByKid.put(name, feedback);
  }

  public long getDuration() {
    return totalDuration;
  }

  public boolean hasSpotsLeft() {
    return availableSpots > 0;
  }

  public long getDurationForKid(String name) {
    return durationByKid.get(name);
  }

  public int getMaxSpots() {
    return maxSpots;
  }

  public int getAvailableSpots() {
    return availableSpots;
  }

  public void setBlackList(List<String> blackList) {
    this.blackList = blackList;
  }

  public String getName() {
    return name;
  }
}
