package com.pdemuinck;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Activity {
  private String name;
  @JsonIgnore
  private int availableSpots;
  private int maxSpots;
  private String imageUrl;
  @JsonIgnore
  private final List<ActivityEvent> events = new ArrayList<>();
  @JsonIgnoreProperties
  private LocalDateTime firstStartTs, lastStartTs, endTs;
  @JsonIgnore
  private long totalDuration = 0;
  @JsonIgnore
  private final Map<String, LocalDateTime> joinTimeByKid = new HashMap<>();
  @JsonIgnore
  private final Map<String, Long> durationByKid = new HashMap<>();
  @JsonIgnore
  private final Map<String, ActivityFeedback> feedbackByKid = new HashMap<>();
  private List<String> blackList = new ArrayList<>();

  public Activity() {
  }

  public Activity(String name, String imageUrl, int maxSpots) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.maxSpots = maxSpots;
  }

  public Activity(String name, int maxSpots) {
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
      if (startTs.isAfter(lastStartTs)) {
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
    if (blackList.contains(name)) {
      events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_NOT_ALLOWED));
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

  public void leave(LocalDateTime eventTs, String name) {
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_LEFT));
    availableSpots++;
    LocalDateTime joinTs = joinTimeByKid.get(name);
    if (lastStartTs != null) {
      if (endTs == null || !endTs.isBefore(eventTs)) {
        if (joinTs.isBefore(eventTs) && joinTs.isAfter(lastStartTs)) {
          durationByKid.computeIfPresent(name,
              (key, duration) -> duration + Duration.between(joinTs, eventTs).toMinutes());
        } else {
          durationByKid.computeIfPresent(name,
              (key, duration) -> duration + Duration.between(lastStartTs, eventTs).toMinutes());
        }
      }
    }
  }

  public void feedback(LocalDateTime eventTs, String name, ActivityFeedback feedback) {
    events.add(new ActivityEvent(eventTs, ActivityEventType.ACTIVITY_FEEDBACK));
    feedbackByKid.put(name, feedback);
  }

  public long getTotalDuration() {
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

  public String getImageUrl() {
    return imageUrl;
  }

  public String getName() {
    return name;
  }

  public String toJsonString() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Activity activity = (Activity) o;
    return Objects.equals(name, activity.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMaxSpots(int maxSpots) {
    this.maxSpots = maxSpots;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
