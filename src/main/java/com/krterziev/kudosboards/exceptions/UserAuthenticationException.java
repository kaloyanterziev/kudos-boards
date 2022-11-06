package com.krterziev.kudosboards.exceptions;

public class UserAuthenticationException extends Exception{
    public UserAuthenticationException(final Exception ex) {
        super("User Authentication Exception", ex);
    }
}
