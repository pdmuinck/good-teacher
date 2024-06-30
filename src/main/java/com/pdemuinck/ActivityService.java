package com.pdemuinck;

import java.util.List;

public interface ActivityService {
  void joinActivity(String activity, String avatar);
  void leaveActivity(String activity, String avatar);

  void startAllActivities();

  List<Activity> fetchActivities();

  Activity addActivity(String name);

  void updateActivityIcon(String name, String icon);

  void saveBoard(List<Activity> activities);

}
