package com.pdemuinck;

import java.util.List;

public interface DataStore {

  void writeActivity(String activity);
  List<String> readActivities();

  void writeActivityBoard(String board, String path);
}
