package com.krterziev.kudosboards.security.services;

import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.models.User;
import java.util.Optional;

public interface UserService {
  Optional<User> getCurrentUser();
  User getCurrentAuthUser() throws UserAuthenticationException;

  Optional<User> getUser(String userId);
}
