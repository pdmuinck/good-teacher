package com.pdemuinck;

import java.util.List;

public class UserMockService implements UserService {
  @Override
  public List<User> fetchUsers() {
    return List.of(new User("Peppa", "Pig", "icons/batman.png"),
        new User("Charlie", "Scott", "icons/spiderman.png"));
  }

  @Override
  public User fetchUserByAvatar(String avatar) {
    return null;
  }
}
