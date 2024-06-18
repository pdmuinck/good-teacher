package com.pdemuinck;

import java.io.InputStream;
import java.util.List;

public interface DataStore {
  void saveKidsProfiles(List<User> kidsProfileList);

  void uploadImages(InputStream is);

  void saveActivity(ActivityEvent activityEvent);

}
