package com.krterziev.kudosboards.controllers;

import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.services.MessageService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  @Autowired
  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping
  public List<Message> getMessages() {
    return messageService.getMessages();
  }

  @GetMapping("/public")
  public List<Message> getPublicMessages() {
    return messageService.getPublicMessages();
  }

  @PostMapping("")
  public void addMessage() {
    messageService.addMessage();
  }
}
