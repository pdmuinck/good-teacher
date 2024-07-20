package com.pdemuinck;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ActivityService {

  List<String> fetchBlackList(String activity);
  void joinActivity(String activity, String userName);
  void leaveActivity(String activity, String userName);

  void startAllActivities();
  void pauseAllActivities();

  List<Activity> fetchActivities();

  Activity addActivity(String name);
  Activity addActivity(String name, String imageUrl, int spots);

  void updateActivity(String name, String icon, int spots);
  void hideActivity(String name);

  List<TimeReportRow> fetchTimeReport(String name);

  Map<String, Optional<Long>> timeByActivity(String name);

  void addToBlackList(String activity, String user);

  List<Activity> getActivities();

  void removeFromBlackList(String activity, String u);

  List<String> getParticipants(String name);
}
