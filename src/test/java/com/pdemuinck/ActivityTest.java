package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActivityTest {

  @Test
  public void keeps_track_of_activity_duration() {
    Activity activity = new Activity("drawing", 4);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 0));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 30));
    assertThat(activity.getDuration()).isEqualTo(30);
  }

  @Test
  public void does_not_allow_pause_events_with_tstamp_before_start_event() {
    Activity activity = new Activity("drawing", 4);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 30));
    assertThrows(RuntimeException.class, () -> activity.pause(LocalDateTime.of(2024, 6, 12, 0, 0)));
  }

  @Test
  public void does_not_allow_pause_events_when_no_start_event(){
    Activity activity = new Activity("drawing", 4);
    assertThrows(RuntimeException.class, () -> activity.pause(LocalDateTime.now()));
  }

  @Test
  public void multiple_start_pauses_should_be_measured_correctly(){
    Activity activity = new Activity("drawing", 4);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 0));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 30));
    activity.start(LocalDateTime.of(2024, 6, 12, 1, 0));
    activity.pause(LocalDateTime.of(2024, 6, 12, 1, 45));
    assertThat(activity.getDuration()).isEqualTo(75);
  }

  @Test
  public void cannot_add_kids_when_activity_is_full(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.now(), "Charlie");
    activity.join(LocalDateTime.now(), "Bob");
    assertThrows(RuntimeException.class, () -> activity.join(LocalDateTime.now(), "Peppa"));
  }

  @Test
  public void keeps_track_of_available_spots(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.now(), "Charlie");
    assertThat(activity.hasSpotsLeft()).isTrue();
    activity.join(LocalDateTime.now(), "Bob");
    assertThat(activity.hasSpotsLeft()).isFalse();
  }

  @Test
  public void tracks_kids_activity_duration_when_joins_from_start(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 0), "Charlie");
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 10));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(5);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 15));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 20));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(10);
  }

  @Test
  public void tracks_kids_activity_duration_when_joins_mid_activity_but_paused(){
    Activity activity = new Activity("drawing", 2);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 10));
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 12), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(0);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 15));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 20));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(5);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 25));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 30));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(10);
  }

  @Test
  public void tracks_kids_activity_duration_when_joins_mid_activity_when_not_paused(){
    Activity activity = new Activity("drawing", 2);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 7), "Charlie");
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 10));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(3);
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 15));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 20));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(8);
  }

  @Test
  public void when_a_kid_leaves_a_spot_becomes_free(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.now(), "Charlie");
    activity.leave(LocalDateTime.now(), "Charlie");
    assertThat(activity.getAvailableSpots()).isEqualTo(2);
  }

  @Test
  public void no_duration_when_kid_leaves_before_start(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.now(), "Charlie");
    activity.leave(LocalDateTime.now(), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(0L);
  }

  @Test
  public void tracks_duration_when_kid_leaves_before_pause(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 0), "Charlie");
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.leave(LocalDateTime.of(2024, 6, 12, 0, 10), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(5);
  }

  @Test
  public void tracks_duration_when_kid_leaves_after_pause(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 0), "Charlie");
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 10));
    activity.leave(LocalDateTime.of(2024, 6, 12, 0, 15), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(5);
  }

  @Test
  public void tracks_when_kid_rejoins_when_activity_is_busy(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 0), "Charlie");
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.leave(LocalDateTime.of(2024, 6, 12, 0, 10), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(5L);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 15), "Charlie");
    activity.pause(LocalDateTime.of(2024, 6, 12, 0, 20));
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(10L);
  }

  @Test
  public void tracks_when_kid_leaves_twice_when_activity_is_busy(){
    Activity activity = new Activity("drawing", 2);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 0), "Charlie");
    activity.start(LocalDateTime.of(2024, 6, 12, 0, 5));
    activity.leave(LocalDateTime.of(2024, 6, 12, 0, 10), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(5L);
    activity.join(LocalDateTime.of(2024, 6, 12, 0, 15), "Charlie");
    activity.leave(LocalDateTime.of(2024, 6, 12, 0, 20), "Charlie");
    assertThat(activity.getDurationForKid("Charlie")).isEqualTo(10L);
  }

  @Test
  public void does_not_allow_kids_on_black_list(){
    Activity activity = new Activity("drawing", 2);
    activity.setBlackList(List.of("Charlie"));
    assertThrows(RuntimeException.class, () -> activity.join(LocalDateTime.now(), "Charlie"));
  }
}
