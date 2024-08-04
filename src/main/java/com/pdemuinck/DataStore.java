package com.pdemuinck;

import java.util.List;

public interface DataStore {

  void writeActivity(String activity);

  void overWriteActivities(String data);

  void saveUser(String user);
  void overWriteUsers(String data);
  List<String> fetchActivities();

  List<String> fetchUsers();

  void saveActivityTime(String data);

  List<String> fetchActivityTime();

  void saveBlacklist(String data);

  List<String> fetchBlackLists();

  void overwriteBlackList(String data);

  void saveFeedback(String data);
}
