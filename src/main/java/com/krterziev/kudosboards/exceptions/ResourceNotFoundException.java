package com.krterziev.kudosboards.exceptions;

public class ResourceNotFoundException extends Exception {

  public ResourceNotFoundException(final String resource, final String id) {
    super(String.format("Resource %s with id %s is not found.", resource, id));
  }

}
