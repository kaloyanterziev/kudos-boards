package com.krterziev.kudosboards.controllers;

import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.response.MessageResponse;
import com.krterziev.kudosboards.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
