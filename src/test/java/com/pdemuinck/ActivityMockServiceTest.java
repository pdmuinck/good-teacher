package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class ActivityMockServiceTest {


  @Test
  public void transforms_activities_from_store() {
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(
        List.of("painting,image1.png,3", "drawing,image2.png,5"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).hasSize(2);
  }

  @Test
  public void parses_activities_with_no_image_url() {
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(
        List.of("painting,,3", "drawing,image2.png,5"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).containsExactlyInAnyOrder(new Activity("painting", "", 3),
        new Activity("drawing", "image2.png", 5));
  }

  @Test
  public void parses_activities_with_no_spots() {
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(
        List.of("painting,,abc"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).containsExactly(new Activity("painting", "", 0));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "_", "@", ",", ",,"})
  public void empty_activities_when_activities_from_store_are_not_parseable(String content) {
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(List.of(content));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).isEmpty();
  }

  @Test
  public void can_not_join_activity_when_on_black_list(){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(List.of("painting,,10"));
    when(dataStoreMock.fetchBlackLists()).thenReturn(List.of("painting,charlie"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    activityService.addActivity("painting");
    assertThrows(RuntimeException.class, () -> activityService.joinActivity("painting", "charlie"));
  }

  @Test
  public void can_not_join_when_activity_is_full(){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    activityService.addActivity("painting", "", 1);
    activityService.joinActivity("painting", "charlie");
    assertThrows(RuntimeException.class, () -> activityService.joinActivity("painting", "robin"));
  }

}
