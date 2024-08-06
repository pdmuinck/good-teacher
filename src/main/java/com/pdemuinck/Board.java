package com.pdemuinck;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Board {
  private String name;
  private LocalDateTime lastUpdated;
  private List<Activity> activities;

  public Board(String name, LocalDateTime lastUpdated, List<Activity> activities) {
    this.name = name;
    this.lastUpdated = lastUpdated;
    this.activities = activities;
  }



  public String getName() {
    return name;
  }

  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  public List<Activity> getActivities() {
    return activities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Board board = (Board) o;
    return Objects.equals(name, board.name) &&
        Objects.equals(lastUpdated, board.lastUpdated) &&
        Objects.equals(activities, board.activities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, lastUpdated, activities);
  }
}
