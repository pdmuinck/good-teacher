package com.pdemuinck;

import java.util.List;

public interface ActivityService {
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

  void addToBlackList(String activity, String user);

  List<Activity> getActivities();
}
