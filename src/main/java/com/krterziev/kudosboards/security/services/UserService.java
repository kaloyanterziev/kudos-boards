package com.krterziev.kudosboards.security.services;

import com.krterziev.kudosboards.models.User;
import java.util.Optional;

public interface UserService {
  Optional<User> getUser();
  User getAuthUser();
}
