package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ActivityServiceIntegrationTest {

  @TempDir
  File tempDir;

  @Test
  public void creates_activities_file_if_does_not_exist_yet_when_trying_to_add_an_activity() {
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("painting");
    File[] files = tempDir.listFiles();
    assertThat(files).anyMatch(file -> file.getName().equals("activities.csv"));
  }

  @Test
  public void stores_activity_as_visible_by_default() {
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("painting");
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).allMatch(Activity::isShow);
  }

  @Test
  public void updates_activity_if_needed() {
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("painting");
    activityService.updateActivity("painting", "image2.png", 15);
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).containsExactly(new Activity("painting", "image2.png", 15));
  }

  @Test
  public void hides_activity() {
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("painting");
    activityService.hideActivity("painting");
    List<Activity> activities = activityService.fetchActivities();
    assertThat(activities).allMatch(a -> !a.isShow());
  }

  @Test
  public void measures_activity_time() throws InterruptedException {
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("painting", "imageUrl", 10);
    activityService.joinActivity("painting", "charlie");
    activityService.startAllActivities();
    Thread.sleep(1000);
    activityService.pauseAllActivities();
    activityService.startAllActivities();
    Thread.sleep(1000);
    activityService.pauseAllActivities();
    List<TimeReportRow> timeReportForCharlie = activityService.fetchTimeReport("charlie");
    assertThat(timeReportForCharlie.stream().map(TimeReportRow::getTime)
        .reduce(0L, Long::sum)).isGreaterThan(2000L);
  }
}