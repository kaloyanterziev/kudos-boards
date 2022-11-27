package com.krterziev.kudosboards.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.models.User;
import com.krterziev.kudosboards.payload.request.MessageRequest;
import com.krterziev.kudosboards.repository.MessageRepository;
import com.krterziev.kudosboards.security.services.UserService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class MessageServiceTest {

  static final String MESSAGE_ID = "6368075d1fec0c3e0a03394e";
  static final String UNKNOWN_MESSAGE_ID = "6368075d1fec0c3e0a03ffff";
  static final String USER_ID = "abcdef1234567890fedcba12";
  static final User USER = givenUser(USER_ID);

  static final Message MESSAGE = givenMessage();

  final MessageService messageService;

  final MessageRepository messageRepository;
  final UserService userService;

  public MessageServiceTest() {
    this.messageRepository = mock(MessageRepository.class);
    this.userService = mock(UserService.class);
    this.messageService = new MessageServiceImpl(messageRepository, userService);
  }

  @BeforeEach
  void setup() {
    when(messageRepository.findById(MESSAGE_ID)).thenReturn(Optional.of(givenMessage()));
    when(messageRepository.findById(UNKNOWN_MESSAGE_ID)).thenReturn(Optional.empty());
  }

  @Test
  void givenMessageId_whenGetMessage_thenReturnMessage() throws ResourceNotFoundException {
    final Message actualMessage = messageService.getMessage(MESSAGE_ID);
    assertMessagesAreEqual(actualMessage, MESSAGE);
    verify(messageRepository, times(1)).findById(MESSAGE_ID);
  }

  @Test
  void givenUnknownMessage_whenGetMessage_ThrowResourceNotFoundException() {
    final Exception exception = assertThrows(ResourceNotFoundException.class, () ->
        messageService.getMessage(UNKNOWN_MESSAGE_ID));
    assertThat(exception.getMessage(),
        equalTo("Resource Message with id " + UNKNOWN_MESSAGE_ID + " is not found."));
    verify(messageRepository, times(1)).findById(UNKNOWN_MESSAGE_ID);
  }

  @Test
  void givenUserLoggedIn_whenCreateMessage_ReturnNewMessage() {
    final MessageRequest messageRequest = givenMessageRequest();
    when(userService.getCurrentUser()).thenReturn(Optional.of(USER));

    final Message actualMessage = messageService.createMessage(messageRequest);
    final Message expectedMessage = givenMessageWithUser(USER);
    assertMessagesAreEqual(actualMessage, expectedMessage);
  }

  @Test
  void givenUserNotLoggedIn_whenCreateMessage_ReturnNewMessage() {
    final MessageRequest messageRequest = givenMessageRequest();
    when(userService.getCurrentUser()).thenReturn(Optional.empty());

    final Message actualMessage = messageService.createMessage(messageRequest);
    final Message expectedMessage = givenMessage();
    assertMessagesAreEqual(actualMessage, expectedMessage);
  }

  private static void assertMessagesAreEqual(final Message actualMessage,
      final Message expectedMessage) {
    assertThat(actualMessage.getText(), equalTo(expectedMessage.getText()));
    assertThat(actualMessage.getImage(), equalTo(expectedMessage.getImage()));
    assertThat(actualMessage.getCreatedBy(), equalTo(expectedMessage.getCreatedBy()));
  }

  private static Message givenMessage() {
    final Message message = new Message("text", "image.gif", Instant.now(), null);
    message.setId(MESSAGE_ID);
    return message;
  }

  private static MessageRequest givenMessageRequest() {
    return new MessageRequest("text", "image.gif");
  }

  private static Message givenMessageWithUser(final User user) {
    final Message message = new Message("text", "image.gif", Instant.now(), user);
    message.setId(MESSAGE_ID);
    return message;
  }

  private static User givenUser(final String userId) {
    final User user = new User("username", "email", "password");
    user.setId(userId);
    return user;
  }
}
