package com.krterziev.kudosboards.payload.response;

import com.krterziev.kudosboards.models.Message;

import java.util.List;

public class BoardResponse {

    private String id;
    private String name;
    private List<MessageResponse> messages;

    public BoardResponse(String id, String name, List<MessageResponse> messages) {
        this.id = id;
        this.name = name;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }
}

