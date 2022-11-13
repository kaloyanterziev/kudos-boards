package com.krterziev.kudosboards.services;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import java.util.List;
import java.util.Optional;

public interface BoardService {

  Optional<Board> getBoard(String id);

  List<Board> getAllBoards();

  Board createBoard(CreateBoardRequest board) throws UserAuthenticationException;

  Message addMessageToBoard(String boardId, Message message) throws ResourceNotFoundException;

  void deleteMessageFromBoard(String boardId, String messageId) throws ResourceNotFoundException;
}
