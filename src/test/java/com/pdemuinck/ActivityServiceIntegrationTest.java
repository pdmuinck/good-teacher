package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ActivityServiceIntegrationTest {

  @TempDir
  File tempDir;

  @Test
  public void shows_activity_if_already_exists(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("drawing", "image", 4);
    Activity activity = activityService.addActivity("drawing", "", 4);
    assertThat(activity).isEqualTo(new Activity("drawing", "image", 4));
  }

  @Test
  public void creates_activities_file_if_does_not_exist_yet_when_trying_to_fetch_activities(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.fetchActivities();
    File[] files = tempDir.listFiles();
    assertThat(files).anyMatch(file -> file.getName().equals("activities.csv"));
  }

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
    assertThat(timeReportForCharlie.get(timeReportForCharlie.size() - 1).getTime()).isCloseTo(2000L,
        Offset.offset(3L));
  }

  @Test
  public void stops_measurement_when_user_leaves() throws InterruptedException {
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("painting", "imageUrl", 10);
    activityService.joinActivity("painting", "charlie");
    activityService.startAllActivities();
    Thread.sleep(1000);
    activityService.pauseAllActivities();
    activityService.leaveActivity("painting", "charlie");
    activityService.startAllActivities();
    Thread.sleep(1000);
    activityService.pauseAllActivities();
    List<TimeReportRow> timeReportForCharlie = activityService.fetchTimeReport("charlie");
    assertThat(timeReportForCharlie.get(timeReportForCharlie.size() - 1).getTime()).isCloseTo(1000L,
        Offset.offset(3L));
  }

  @Test
  public void adds_to_black_list(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addToBlackList("drawing", "charlie");
    List<String> strings = activityService.fetchBlackList("drawing");
    assertThat(strings).containsOnly("charlie");
  }

  @Test
  public void removes_from_black_list(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addToBlackList("drawing", "charlie");
    activityService.addToBlackList("drawing", "otto");
    activityService.removeFromBlackList("drawing", "otto");
    List<String> strings = activityService.fetchBlackList("drawing");
    assertThat(strings).containsOnly("charlie");
  }

  @Test
  public void ignores_when_removing_user_from_black_list_who_is_not_on_black_list(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addToBlackList("drawing", "charlie");
    activityService.removeFromBlackList("drawing", "otto");
    List<String> strings = activityService.fetchBlackList("drawing");
    assertThat(strings).containsOnly("charlie");
  }

  @Test
  public void retains_blacklist_for_other_activities(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addToBlackList("drawing", "charlie");
    activityService.addToBlackList("painting", "maxine");
    activityService.removeFromBlackList("drawing", "charlie");
    assertThat(activityService.fetchBlackList("drawing")).isEmpty();
    assertThat(activityService.fetchBlackList("painting")).hasSize(1);
  }

  @Test
  public void adds_user_to_blacklist_after_removal(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addToBlackList("drawing", "charlie");
    activityService.addToBlackList("painting", "maxine");
    activityService.removeFromBlackList("drawing", "charlie");
    activityService.addToBlackList("drawing", "charlie");
    assertThat(activityService.fetchBlackList("drawing")).hasSize(1);
    assertThat(activityService.fetchBlackList("painting")).hasSize(1);
  }

  @Test
  public void adds_same_user_to_multiple_blacklists(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addToBlackList("drawing", "charlie");
    activityService.addToBlackList("painting", "charlie");
    activityService.removeFromBlackList("drawing", "charlie");
    activityService.addToBlackList("drawing", "charlie");
    assertThat(activityService.fetchBlackList("drawing")).hasSize(1);
    assertThat(activityService.fetchBlackList("painting")).hasSize(1);
  }

}
