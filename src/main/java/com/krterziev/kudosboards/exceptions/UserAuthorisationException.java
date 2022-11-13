package com.krterziev.kudosboards.exceptions;

public class UserAuthorisationException extends RuntimeException {

    public UserAuthorisationException() {
      super("User not authorised");
    }
}
