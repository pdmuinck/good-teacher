package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class ActivityMockServiceTest {


  @Test
  public void transforms_activities_from_store(){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(List.of("painting,image1.png,3", "drawing,image2.png,5"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).hasSize(2);
  }

  @Test
  public void parses_activities_with_no_image_url(){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(List.of("painting,,3", "drawing,image2.png,5"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).anyMatch(a -> a.getImageUrl().isEmpty());
  }

  @Test
  public void parses_activities_with_no_spots(){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(List.of("painting,,abc", "drawing,image2.png,5"));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).anyMatch(a -> a.getMaxSpots() == 0);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "_", "@", ",", ",,"})
  public void empty_activities_when_activities_from_store_are_not_parseable(String content){
    FileDataStore dataStoreMock = Mockito.mock(FileDataStore.class);
    when(dataStoreMock.fetchActivities()).thenReturn(List.of(content));
    ActivityService activityService = new ActivityMockService(dataStoreMock);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).isEmpty();
  }
}
