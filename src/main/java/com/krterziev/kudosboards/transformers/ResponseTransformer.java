package com.krterziev.kudosboards.transformers;

import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.MessageRequest;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.payload.response.MessageResponse;

public class ResponseTransformer {

  private ResponseTransformer() {
  }

  public static BoardResponse toBoardResponse(final Board board) {
    return new BoardResponse(board.getId(), board.getName(),
        board.getMessages().stream().map(ResponseTransformer::toMessageResponse).toList());
  }

  public static MessageResponse toMessageResponse(final Message message) {
    return new MessageResponse(message.getId(), message.getText(), message.getImage());
  }
}
