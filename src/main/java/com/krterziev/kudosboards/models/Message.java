package com.krterziev.kudosboards.models;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection = "messages")
public class Message {

  @Id
  private String id;
  private String text;
  private String image;
  @DateTimeFormat
  private Instant lastUpdated;
  @DBRef
  private User createdBy;

  public Message(final String text,
      final String image,
      final Instant lastUpdated,
      final User createdBy) {
    this.text = text;
    this.image = image;
    this.lastUpdated = lastUpdated;
    this.createdBy = createdBy;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Instant getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Instant lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }
    Message message = (Message) o;
    return Objects.equals(id, message.id) && Objects.equals(text, message.text) && Objects.equals(
        image, message.image) && Objects.equals(lastUpdated, message.lastUpdated) && Objects.equals(
        createdBy, message.createdBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, text, image, lastUpdated, createdBy);
  }
}

