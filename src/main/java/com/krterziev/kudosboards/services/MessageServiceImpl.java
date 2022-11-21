package com.krterziev.kudosboards.services;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.models.User;
import com.krterziev.kudosboards.payload.request.MessageRequest;
import com.krterziev.kudosboards.repository.MessageRepository;
import com.krterziev.kudosboards.security.services.UserService;
import com.krterziev.kudosboards.transformers.UpdateTransformer;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final UserService userService;

  @Autowired
  public MessageServiceImpl(final MessageRepository messageRepository,
      final UserService userService) {
    this.messageRepository = messageRepository;
    this.userService = userService;
  }

  @Override
  public void updateMessage(String id, MessageRequest messageRequest)
      throws UserAuthenticationException, ResourceNotFoundException, UserAuthorisationException {
    final Message message = checkIfMessageOwnedByUser(id);

    UpdateTransformer.updateMessage(message, messageRequest);

    messageRepository.save(message);
  }

  @Override
  public Message createMessage(MessageRequest messageRequest) {
    final Optional<User> user = userService.getCurrentUser();
    final Message message = new Message(messageRequest.text(), messageRequest.image(),
        Instant.now(), user.orElse(null));

    messageRepository.save(message);
    return message;
  }

  @Override
  public void deleteMessage(String id)
      throws UserAuthenticationException, ResourceNotFoundException, UserAuthorisationException {
    final Message message = checkIfMessageOwnedByUser(id);

    messageRepository.deleteById(message.getId());
  }

  @Override
  public Message getMessage(String messageId) throws ResourceNotFoundException {
    return messageRepository.findById(messageId)
        .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
  }

  private Message checkIfMessageOwnedByUser(final String id)
      throws UserAuthenticationException, ResourceNotFoundException, UserAuthorisationException {
    final User user = userService.getCurrentAuthUser();
    final Message message = messageRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Message", id));

    if(message.getCreatedBy() != null
        && !Objects.equals(message.getCreatedBy().getId(), user.getId())) {
      throw new UserAuthorisationException();
    }
    return message;
  }
}
