package com.krterziev.kudosboards.services;

import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.CreateMessageRequest;
import java.util.List;
import java.util.Optional;

public interface BoardService {

  Optional<Board> getBoard(String id);

  List<Board> getAllBoards();

  Board createBoard(CreateBoardRequest board);

  Message addMessageToBoard(Board board, CreateMessageRequest messageRequest);
}
