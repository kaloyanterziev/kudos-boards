package com.krterziev.kudosboards.transformers;

import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.payload.response.MessageResponse;

public class ResponseTransformer {

    private ResponseTransformer() {
    }

    public static BoardResponse toBoardResponse(final Board board) {
        return new BoardResponse(board.getId(), board.getName(), board.getMessages().stream().map(message ->
                new MessageResponse(message.getText())).toList());
    }
}
