package com.pdemuinck;

import java.util.List;
import java.util.Optional;

public interface UserService {

  List<User> fetchUsers();
  User fetchUserByAvatar(String avatar);

  void addUser(String name, String avatar);

  Optional<User> fetchUserByName(String name);

  void saveFeedback(String activityName, String user, String feedback);

}
