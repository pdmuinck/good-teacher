package com.pdemuinck;

import java.util.List;

public interface UserService {

  List<User> fetchUsers();
  User fetchUserByAvatar(String avatar);

}
