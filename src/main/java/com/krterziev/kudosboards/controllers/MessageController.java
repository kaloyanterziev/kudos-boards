package com.krterziev.kudosboards.controllers;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.MessageRequest;
import com.krterziev.kudosboards.payload.response.IdResponse;
import com.krterziev.kudosboards.payload.response.MessageResponse;
import com.krterziev.kudosboards.services.BoardService;
import com.krterziev.kudosboards.services.MessageService;
import com.krterziev.kudosboards.transformers.ResponseTransformer;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/boards/{boardId}/messages")
public class MessageController {

  private final MessageService messageService;
  private final BoardService boardService;

  @Autowired
  public MessageController(MessageService messageService, BoardService boardService) {
    this.messageService = messageService;
    this.boardService = boardService;
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<MessageResponse> getMessage(@PathVariable final String messageId)
      throws ResourceNotFoundException {
    final Message message = messageService.getMessage(messageId);
    return ResponseEntity.ok(ResponseTransformer.toMessageResponse(message));
  }

  @PostMapping()
  public ResponseEntity<IdResponse> createMessageInBoard(@PathVariable final String boardId,
      @RequestBody final MessageRequest messageRequest) throws ResourceNotFoundException {
    final Message message = messageService.createMessage(messageRequest);
    boardService.addMessageToBoard(boardId, message);
    return ResponseEntity.created(
        URI.create(String.format("/api/boards/%s/messages/%s", boardId, message.getId()))).build();
  }

  @PutMapping("/{messageId}")
  public ResponseEntity<Void> updateMessage(@PathVariable final String messageId,
      @RequestBody final MessageRequest messageRequest)
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    messageService.updateMessage(messageId, messageRequest);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> deleteMessage(@PathVariable final String messageId,
      @PathVariable final String boardId)
      throws ResourceNotFoundException, UserAuthenticationException, UserAuthorisationException {
    boardService.deleteMessageFromBoard(boardId, messageId);
    messageService.deleteMessage(messageId);
    return ResponseEntity.ok().build();
  }

}
