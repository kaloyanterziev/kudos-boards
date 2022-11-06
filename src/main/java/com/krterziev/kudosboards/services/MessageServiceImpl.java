package com.krterziev.kudosboards.services;

import com.krterziev.kudosboards.models.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public List<Message> getMessages() {
        return null;
    }

    @Override
    public List<Message> getPublicMessages() {
        return null;
    }

    @Override
    public void addMessage() {

    }
}
