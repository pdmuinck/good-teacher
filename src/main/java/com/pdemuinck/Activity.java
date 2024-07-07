package com.pdemuinck;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Activity {
  private String name;
  private int availableSpots;
  private int maxSpots;
  private boolean show = true;
  private String imageUrl;
  private LocalDateTime firstStartTs, lastStartTs, endTs;
  private long totalDuration = 0;
  private final Map<String, LocalDateTime> joinTimeByKid = new HashMap<>();
  private final Map<String, Long> durationByKid = new HashMap<>();
  private List<String> blackList = new ArrayList<>();

  public Activity(String name, String imageUrl, int maxSpots) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.maxSpots = maxSpots;
    this.availableSpots = maxSpots;
  }

  public Activity(String name, String imageUrl, int maxSpots, boolean show) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.maxSpots = maxSpots;
    this.availableSpots = maxSpots;
    this.show = show;
  }

  public Activity(String name, int maxSpots) {
    this.name = name;
    this.maxSpots = maxSpots;
    this.availableSpots = maxSpots;
  }

  public void start(LocalDateTime eventTs) {
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
    endTs = eventTs;
    totalDuration += Duration.between(lastStartTs, endTs).toMillis();
    joinTimeByKid.forEach((name, startTs) -> {
      if (startTs.isAfter(lastStartTs)) {
        durationByKid.computeIfPresent(name,
            (key, duration) -> duration + Duration.between(startTs, eventTs).toMillis());
      } else {
        durationByKid.computeIfPresent(name,
            (key, duration) -> duration + Duration.between(lastStartTs, eventTs).toMillis());
      }
    });
  }

  public void join(LocalDateTime eventTs, String name) {
    if (blackList.contains(name)) {
      throw new RuntimeException("User is not allowed to join");
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
    availableSpots++;
    LocalDateTime joinTs = joinTimeByKid.get(name);
    if (lastStartTs != null) {
      if (endTs == null || !endTs.isBefore(eventTs)) {
        if (joinTs.isBefore(eventTs) && joinTs.isAfter(lastStartTs)) {
          durationByKid.computeIfPresent(name,
              (key, duration) -> duration + Duration.between(joinTs, eventTs).toMillis());
        } else {
          durationByKid.computeIfPresent(name,
              (key, duration) -> duration + Duration.between(lastStartTs, eventTs).toMillis());
        }
      }
    }
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

  public String getDurationByKid() {
    return this.durationByKid.entrySet().stream()
        .map(e -> String.join(",", e.getKey(), this.name,
            LocalDate.now().format(DateTimeFormatter.ISO_DATE), String.valueOf(e.getValue())))
        .collect(
            Collectors.joining("\r\n"));
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Activity activity = (Activity) o;
    return maxSpots == activity.maxSpots && Objects.equals(name, activity.name) &&
        Objects.equals(imageUrl, activity.imageUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, maxSpots, imageUrl);
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

  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  @Override
  public String toString() {
    return "Activity{" +
        "name='" + name + '\'' +
        ", maxSpots=" + maxSpots +
        ", imageUrl='" + imageUrl + '\'' +
        '}';
  }
}
