package com.krterziev.kudosboards.security.services;

import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.models.User;
import com.krterziev.kudosboards.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserServiceImpl(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<User> getCurrentUser() {
    final String username;
    try {
      username = getUsername();
    } catch (final UserAuthenticationException ex) {
      return Optional.empty();
    }
    return userRepository.findByUsername(username);
  }

  @Override
  public Optional<User> getUser(final String userId) {
    return userRepository.findById(userId);
  }

  @Override
  public User getCurrentAuthUser() throws UserAuthenticationException {
    final String username = getUsername();
    return userRepository.findByUsername(username).orElseThrow(UserAuthenticationException::new);
  }

  private static String getUsername() throws UserAuthenticationException {
    try {
      final UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
          .getAuthentication().getPrincipal();
      return userDetails.getUsername();
    } catch (final Exception ex) {
      throw new UserAuthenticationException(ex);
    }
  }

}
