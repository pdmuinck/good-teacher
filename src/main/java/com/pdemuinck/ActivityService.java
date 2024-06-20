package com.pdemuinck;

import java.util.List;

public interface ActivityService {
  public void joinActivity(String activity, String avatar);
  public void leaveActivity(String activity, String avatar);

  public void startAllActivities();

  public List<Activity> fetchActivities();

  public void addActivity(String name);

  void saveBoard(List<Activity> activities);

}
