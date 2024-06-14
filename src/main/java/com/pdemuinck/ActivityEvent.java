package com.pdemuinck;

import java.time.LocalDateTime;

public class ActivityEvent {
  private LocalDateTime eventTs;
  private ActivityEventType eventType;

  public ActivityEvent(LocalDateTime eventTs, ActivityEventType eventType) {
    this.eventTs = eventTs;
    this.eventType = eventType;
  }

  public LocalDateTime getEventTs() {
    return eventTs;
  }

  public ActivityEventType getEventType() {
    return eventType;
  }
}
