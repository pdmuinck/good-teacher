package com.pdemuinck;

import java.time.LocalDate;

public class TimeReportRow {
  private final String userName;
  private final String activityName;
  private final LocalDate activityDate;
  private final long time;

  public TimeReportRow(String userName, String activityName, LocalDate activityDate, long time) {
    this.userName = userName;
    this.activityName = activityName;
    this.activityDate = activityDate;
    this.time = time;
  }

  public String getUserName() {
    return userName;
  }

  public String getActivityName() {
    return activityName;
  }

  public LocalDate getActivityDate() {
    return activityDate;
  }

  public long getTime() {
    return time;
  }
}
