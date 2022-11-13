package com.krterziev.kudosboards.exceptions;

public class UserAuthenticationException extends RuntimeException {

  public UserAuthenticationException(final Exception ex) {
    super("User not authenticated", ex);
  }

  public UserAuthenticationException() {
    super("User not authenticated");
  }
}
