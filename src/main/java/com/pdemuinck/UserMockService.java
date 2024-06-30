package com.pdemuinck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserMockService implements UserService {

  private DataStore dataStore;
  private List<User> users = new ArrayList<>();

  public UserMockService(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public List<User> fetchUsers() {
    this.users = dataStore.fetchUsers().stream().filter(x -> x.split(",").length > 1)
        .map(x -> new User(x.split(",")[0], x.split(",")[1])).collect(
            Collectors.toList());
    return this.users;
  }

  @Override
  public User fetchUserByAvatar(String avatar) {
    return null;
  }

  @Override
  public void addUser(String name, String avatar) {
    User user = new User(name, avatar);
    if (!users.contains(user)) {
      users.add(user);
      dataStore.saveUser(String.join(",", name, avatar));
    }
  }

  @Override
  public Optional<User> fetchUserByName(String name){
    return this.users.stream().filter(u -> u.getName().equals(name)).findFirst();
  }
}
