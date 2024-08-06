package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ActivityServiceIntegrationTest {

  @TempDir
  File tempDir;

  @Test
  public void fetches_most_up_to_date_board(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    Board board = new Board("board1", LocalDateTime.now().minusDays(1), new ArrayList<>());
    activityService.saveBoard(board);
    Board updatedBoard = new Board("board1", LocalDateTime.now(), new ArrayList<>());
    activityService.saveBoard(updatedBoard);
    List<Board> boards = activityService.fetchBoards();
    assertThat(boards).containsOnly(updatedBoard);
  }

  @Test
  public void sorts_most_recent_boards_first(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    Board oldBoard = new Board("board1", LocalDateTime.now().minusDays(1), new ArrayList<>());
    activityService.saveBoard(oldBoard);
    Board recentBoard = new Board("board2", LocalDateTime.now(), new ArrayList<>());
    activityService.saveBoard(recentBoard);
    Board veryOldBoard = new Board("board3", LocalDateTime.now().minusYears(3), new ArrayList<>());
    activityService.saveBoard(veryOldBoard);
    List<Board> boards = activityService.fetchBoards();
    assertThat(boards).isSortedAccordingTo(Collections.reverseOrder(Comparator.comparing(Board::getLastUpdated)));
  }

  @Test
  public void stores_new_activities_to_board(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("drawing");
    Board board = new Board("board1", LocalDateTime.now().minusDays(1), new ArrayList<>());
    activityService.saveBoard(board);
    Board updateBoard = new Board("board1", LocalDateTime.now(), List.of(new Activity("drawing", "", 5)));
    activityService.saveBoard(updateBoard);
    List<Board> boards = activityService.fetchBoards();
    assertThat(boards.get(0).getActivities()).hasSize(1);
  }

  @Test
  public void removes_activities_from_board(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("drawing");
    Board board = new Board("board1", LocalDateTime.now().minusDays(1), List.of(new Activity("drawing", "", 5)));
    activityService.saveBoard(board);
    Board update = new Board("board1", LocalDateTime.now(), new ArrayList<>());
    activityService.saveBoard(update);
    List<Board> boards = activityService.fetchBoards();
    assertThat(boards.get(0).getActivities()).isEmpty();
  }

  @Test
  public void changes_number_of_activity_spots_in_board(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("drawing");
    Board board = new Board("board1", LocalDateTime.now().minusDays(1), List.of(new Activity("drawing", "", 5)));
    activityService.saveBoard(board);
    Board update = new Board("board1", LocalDateTime.now().minusDays(1), List.of(new Activity("drawing", "", 1)));
    activityService.saveBoard(update);
    List<Board> boards = activityService.fetchBoards();
    assertThat(boards.get(0).getActivities().get(0).getMaxSpots()).isEqualTo(1);
  }

  @Test
  public void changes_activity_image_url_in_board(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    ActivityService activityService = new ActivityMockService(dataStore);
    activityService.addActivity("drawing");
    Board board = new Board("board1", LocalDateTime.now().minusDays(1), List.of(new Activity("drawing", "", 5)));
    activityService.saveBoard(board);
    Board update = new Board("board1", LocalDateTime.now().minusDays(1), List.of(new Activity("drawing", "bla", 1)));
    activityService.saveBoard(update);
    List<Board> boards = activityService.fetchBoards();
    assertThat(boards.get(0).getActivities().get(0).getImageUrl()).isEqualTo("bla");
  }

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
