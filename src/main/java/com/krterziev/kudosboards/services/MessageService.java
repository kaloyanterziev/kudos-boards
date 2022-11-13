package com.krterziev.kudosboards.services;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.MessageRequest;

public interface MessageService {

  void updateMessage(String id, MessageRequest messageRequest)
      throws UserAuthenticationException, ResourceNotFoundException, UserAuthorisationException;

  Message createMessage(MessageRequest messageRequest);

  void deleteMessage(String id)
      throws UserAuthenticationException, ResourceNotFoundException, UserAuthorisationException;
}
