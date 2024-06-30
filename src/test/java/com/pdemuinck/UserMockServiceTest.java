package com.pdemuinck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserMockServiceTest {

  @Test
  public void only_save_user_in_data_store_when_new(){
    // Given
    DataStore dataStoreMock = Mockito.mock(DataStore.class);
    UserService userService = new UserMockService(dataStoreMock);

    // When
    userService.addUser("Charlie", "abc");
    userService.addUser("Charlie", "abc");

    // Then
    Mockito.verify(dataStoreMock, Mockito.times(1)).saveUser("Charlie,abc");
  }

  @Test
  public void fetches_users_from_data_store(){
    // Given
    DataStore dataStoreMock = Mockito.mock(DataStore.class);
    UserService userService = new UserMockService(dataStoreMock);

    // When
    when(dataStoreMock.fetchUsers()).thenReturn(List.of("Charlie,abc", "Peppa,def"));
    List<User> users = userService.fetchUsers();

    // Then
    assertThat(users).containsExactlyInAnyOrder(new User("Charlie", "abc"), new User("Peppa", "def"));
  }

  @Test
  public void fetches_no_users_when_nothing_in_store(){
    // Given
    DataStore dataStoreMock = Mockito.mock(DataStore.class);
    UserService userService = new UserMockService(dataStoreMock);

    // When
    when(dataStoreMock.fetchUsers()).thenReturn(new ArrayList<>());
    List<User> users = userService.fetchUsers();

    // Then
    assertThat(users).isEmpty();
  }

  @Test
  public void skips_users_with_incorrect_delimiter(){
    // Given
    DataStore dataStoreMock = Mockito.mock(DataStore.class);
    UserService userService = new UserMockService(dataStoreMock);

    // When
    when(dataStoreMock.fetchUsers()).thenReturn(List.of("Charlie/abc"));
    List<User> users = userService.fetchUsers();

    // Then
    assertThat(users).isEmpty();
  }

  @Test
  public void skips_user_with_no_values(){
    // Given
    DataStore dataStoreMock = Mockito.mock(DataStore.class);
    UserService userService = new UserMockService(dataStoreMock);

    // When
    when(dataStoreMock.fetchUsers()).thenReturn(List.of(","));
    List<User> users = userService.fetchUsers();

    // Then
    assertThat(users).isEmpty();
  }
}
