package com.krterziev.kudosboards.helpers;

import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserHelper {

  private UserHelper() {
  }

  public static String getUsername() throws UserAuthenticationException {
    try {
      final UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
          .getAuthentication().getPrincipal();
      return userDetails.getUsername();
    } catch (final Exception ex) {
      throw new UserAuthenticationException(ex);
    }
  }
}
