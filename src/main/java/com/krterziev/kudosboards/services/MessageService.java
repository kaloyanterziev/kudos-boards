package com.krterziev.kudosboards.services;

import com.krterziev.kudosboards.models.Message;

import java.util.List;

public interface MessageService {
    List<Message> getMessages();

    List<Message> getPublicMessages();

    void addMessage();
}
