package com.pdemuinck;

import java.io.InputStream;
import java.util.List;

public interface DataStore {
  void saveKidsProfiles(List<KidsProfile> kidsProfileList);

  void uploadImages(InputStream is);

  void saveActivity(ActivityEvent activityEvent);

}
