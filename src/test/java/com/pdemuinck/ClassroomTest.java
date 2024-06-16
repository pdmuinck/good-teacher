package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ClassroomTest {

  @Test
  public void syncs_the_number_of_spots_when_activity_is_added() {
    Classroom classroom =
        ClassroomMother.classroom().build();

    classroom.addActivity(new Activity("drawing", 4));

    assertThat(classroom.getTotalSpots()).isEqualTo(4);
  }

  @Test
  public void syncs_the_number_of_spots_when_activity_gets_removed() {
    Classroom classroom =
        ClassroomMother.classroom()
            .withActivities(activity -> activity.withName("ipads").withMaxSpots(5),
                activity1 -> activity1.withName("drawing").withMaxSpots(3)).build();

    classroom.removeActivity("drawing");
    assertThat(classroom.getTotalSpots()).isEqualTo(5);
  }
}
