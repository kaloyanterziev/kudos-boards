package com.krterziev.kudosboards.controllers;

import static com.krterziev.kudosboards.controllers.BoardControllerTest.BOARD_ID_1;
import static com.krterziev.kudosboards.matchers.ResponseBodyMatchers.responseBody;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.MessageRequest;
import com.krterziev.kudosboards.payload.response.MessageResponse;
import com.krterziev.kudosboards.services.BoardService;
import com.krterziev.kudosboards.services.MessageService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(value = MessageController.class)
class MessageControllerTest extends ControllerTest {

  private static final String PATH = "/api/boards/" + BOARD_ID_1 + "/messages/";
  private static final String MESSAGE_ID = "1234-qwerty-5678";
  private static final String MESSAGE_TEXT = "Greetings from London";
  private static final String MESSAGE_IMAGE = "https://somewebserver.com/gifs/funny_cat.gif";


  @MockBean
  private BoardService boardService;

  @MockBean
  private MessageService messageService;

  @Test
  void givenExistingMessage_whenGetMessageById_thenReturnMessage() throws Exception {
    final Message message = givenMessage();

    when(messageService.getMessage(MESSAGE_ID)).thenReturn(message);

    mvc.perform(get(PATH + MESSAGE_ID))
        .andExpect(status().isOk())
        .andExpect(responseBody().containsObjectAsJson(message, MessageResponse.class));

    verify(messageService, times(1)).getMessage(MESSAGE_ID);
  }

  @Test
  void givenNonExistingMessage_whenGetMessageById_thenReturnNotFound() throws Exception {
    when(messageService.getMessage(MESSAGE_ID))
        .thenThrow(new ResourceNotFoundException("Message", MESSAGE_ID));

    mvc.perform(get(PATH + MESSAGE_ID))
        .andExpect(status().isNotFound());

    verify(messageService, times(1)).getMessage(MESSAGE_ID);
  }

  @Test
  void givenBoardIdAndMessage_whenCreateMessageInBoard_thenReturnIdOfNewMessage()
      throws Exception {
    final Message message = givenMessage();
    final MessageRequest messageRequest = givenMessageRequest();

    when(messageService.createMessage(messageRequest)).thenReturn(message);

    mvc.perform(post(PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", PATH + MESSAGE_ID));

    verify(messageService, times(1)).createMessage(messageRequest);
    verify(boardService, times(1)).addMessageToBoard(BOARD_ID_1, message);
  }

  @Test
  void givenNonExistentBoardIdAndMessage_whenCreateMessageInBoard_thenReturnIdOfNewMessage()
      throws Exception {
    final Message message = givenMessage();
    final MessageRequest messageRequest = givenMessageRequest();

    when(messageService.createMessage(messageRequest)).thenReturn(message);
    doThrow(new ResourceNotFoundException("Board", BOARD_ID_1))
        .when(boardService).addMessageToBoard(BOARD_ID_1, message);

    mvc.perform(post(PATH)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isNotFound());

    verify(messageService, times(1)).createMessage(messageRequest);
    verify(boardService, times(1)).addMessageToBoard(BOARD_ID_1, message);
  }

  @Test
  void givenMessage_whenUpdateMessage_thenUpdateMessage() throws Exception {
    final MessageRequest messageRequest = givenMessageRequest();

    mvc.perform(put(PATH + MESSAGE_ID)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isOk());

    verify(messageService, times(1)).updateMessage(MESSAGE_ID, messageRequest);
  }

  @Test
  void givenMessageAndNotExistingMessageId_whenUpdateMessage_thenReturnNotFound() throws Exception {
    final MessageRequest messageRequest = givenMessageRequest();

    doThrow(new ResourceNotFoundException("Message", MESSAGE_ID))
        .when(messageService).updateMessage(MESSAGE_ID, messageRequest);

    mvc.perform(put(PATH + MESSAGE_ID)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isNotFound());

    verify(messageService, times(1)).updateMessage(MESSAGE_ID, messageRequest);
  }

  @Test
  void givenMessageAndNotLoggedUser_whenUpdateMessage_thenReturnUnauthorized() throws Exception {
    final MessageRequest messageRequest = givenMessageRequest();

    doThrow(new UserAuthenticationException())
        .when(messageService).updateMessage(MESSAGE_ID, messageRequest);

    mvc.perform(put(PATH + MESSAGE_ID)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isUnauthorized());

    verify(messageService, times(1)).updateMessage(MESSAGE_ID, messageRequest);
  }

  @Test
  void givenMessageAndUserNotOwnerOfMessage_whenUpdateMessage_thenReturnUnauthorized() throws Exception {
    final MessageRequest messageRequest = givenMessageRequest();

    doThrow(new UserAuthenticationException())
        .when(messageService).updateMessage(MESSAGE_ID, messageRequest);

    mvc.perform(put(PATH + MESSAGE_ID)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(messageRequest)))
        .andExpect(status().isUnauthorized());

    verify(messageService, times(1)).updateMessage(MESSAGE_ID, messageRequest);
  }

  @Test
  void givenMessageId_whenDeleteMessage_thenDeleteMessage() throws Exception {

    mvc.perform(delete(PATH + MESSAGE_ID))
        .andExpect(status().isOk());

    verify(messageService, times(1)).deleteMessage(MESSAGE_ID);
  }

  @Test
  void givenNotExistingMessageId_whenDeleteMessage_thenReturnNotFound() throws Exception {

    doThrow(new ResourceNotFoundException("Message", MESSAGE_ID))
        .when(messageService).deleteMessage(MESSAGE_ID);

    mvc.perform(delete(PATH + MESSAGE_ID))
        .andExpect(status().isNotFound());

    verify(messageService, times(1)).deleteMessage(MESSAGE_ID);
  }

  @Test
  void givenUserNotOwnerOfMessage_whenDeleteMessage_thenReturnUnauthorized() throws Exception {
    doThrow(new UserAuthorisationException())
        .when(messageService).deleteMessage(MESSAGE_ID);

    mvc.perform(delete(PATH + MESSAGE_ID))
        .andExpect(status().isUnauthorized());

    verify(messageService, times(1)).deleteMessage(MESSAGE_ID);
  }

  @Test
  void givenNotLoggedUser_whenUpdateMessage_thenReturnUnauthorized() throws Exception {

    doThrow(new UserAuthorisationException())
        .when(messageService).deleteMessage(MESSAGE_ID);

    mvc.perform(delete(PATH + MESSAGE_ID))
        .andExpect(status().isUnauthorized());

    verify(messageService, times(1)).deleteMessage(MESSAGE_ID);
  }

  private static Message givenMessage() {
    final Message message = new Message(MESSAGE_TEXT, MESSAGE_IMAGE, Instant.now(), null);
    message.setId(MESSAGE_ID);
    return message;
  }

  private static MessageRequest givenMessageRequest() {
    return new MessageRequest(MESSAGE_TEXT, MESSAGE_IMAGE);
  }

}
