package com.pdemuinck;

import java.util.List;

public interface DataStore {

  void writeActivity(String activity);

  void overWriteActivities(String data);

  void saveUser(String user);
  List<String> readActivities();

  void writeActivityBoard(String board, String path, String fileName);

  List<String> fetchUsers();
}
