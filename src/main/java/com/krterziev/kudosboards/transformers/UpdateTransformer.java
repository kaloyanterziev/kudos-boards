package com.krterziev.kudosboards.transformers;

import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.MessageRequest;
import java.time.Instant;

public class UpdateTransformer {

  private UpdateTransformer() {
  }

  public static void updateMessage(final Message message, final MessageRequest messageRequest) {
    message.setLastUpdated(Instant.now());
    message.setText(messageRequest.text());
    message.setImage(messageRequest.image());
  }

}
