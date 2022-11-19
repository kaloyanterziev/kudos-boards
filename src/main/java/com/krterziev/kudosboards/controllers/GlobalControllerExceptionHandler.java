package com.krterziev.kudosboards.controllers;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Requested Resource Not Found")
  @ExceptionHandler(ResourceNotFoundException.class)
  public void notFound(final ResourceNotFoundException ex) {
    LOG.warn(ex.getMessage());
  }

  @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorised Request")
  @ExceptionHandler(UserAuthorisationException.class)
  public void unauthorised(final UserAuthorisationException ex) {
    LOG.error(ex.getMessage());
  }

  @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthenticated Request")
  @ExceptionHandler(UserAuthenticationException.class)
  public void unauthenticated(final UserAuthenticationException ex) {
    LOG.error(ex.getMessage());
  }
}
