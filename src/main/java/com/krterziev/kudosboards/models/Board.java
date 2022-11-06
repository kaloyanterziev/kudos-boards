package com.krterziev.kudosboards.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Document(collection = "boards")
public class Board {
    @Id
    private String id;
    @NotBlank
    private String name;
    @DBRef
    private List<Message> messages;
    @DBRef
    private List<User> users;
    private EBoardAccessLevel accessLevel;

    public Board(final String name, final List<Message> messages, final List<User> users, final EBoardAccessLevel accessLevel) {
        this.name = name;
        this.messages = messages;
        this.users = users;
        this.accessLevel = accessLevel;
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

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public EBoardAccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(EBoardAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Objects.equals(id, board.id) && Objects.equals(name, board.name) && Objects.equals(messages, board.messages) && Objects.equals(users, board.users) && accessLevel == board.accessLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, messages, users, accessLevel);
    }
}
