package com.krterziev.kudosboards.exceptions;

public class UserAuthorisationException extends Exception {

    public UserAuthorisationException() {
      super("User not authorised");
    }
}
