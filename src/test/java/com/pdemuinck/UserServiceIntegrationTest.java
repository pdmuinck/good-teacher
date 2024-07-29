package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserServiceIntegrationTest {

  @TempDir
  File tempDir;

  @Test
  public void creates_user_file_if_not_exists(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    UserService userService = new UserMockService(dataStore);
    userService.addUser("charlie", "avatar");
    File[] files = tempDir.listFiles();
    assertThat(files).anyMatch(file -> file.getName().equals("users.csv"));
  }

  @Test
  public void saves_user_in_file(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    UserService userService = new UserMockService(dataStore);
    userService.addUser("charlie", "avatar");
    List<User> users = userService.fetchUsers();
    assertThat(users).containsOnly(new User("charlie", "avatar"));
  }

  @Test
  public void fetches_users_by_name(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    UserService userService = new UserMockService(dataStore);
    userService.addUser("charlie", "avatar");
    Optional<User> user = userService.fetchUserByName("charlie");
    assertThat(user).isPresent();
  }

  @Test
  public void updates_existing_user(){
    DataStore dataStore = new FileDataStore(tempDir.getAbsolutePath());
    UserService userService = new UserMockService(dataStore);
    userService.addUser("charlie", "avatar");
    userService.addUser("charlie", "avatar1");
    Optional<User> user = userService.fetchUserByName("charlie");
    assertThat(user.get().getAvatar()).isEqualTo("avatar1");
  }


}
