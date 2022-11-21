package com.krterziev.kudosboards.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.EBoardAccessLevel;
import com.krterziev.kudosboards.repository.BoardRepository;
import com.krterziev.kudosboards.security.services.UserService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;

@TestComponent
class BoardServiceTest {
  final static String BOARD_ID_1 = "asdfg-12345-hjkl";
  final static String BOARD_ID_2 = "qwert-67890-uiop";
  static final String BOARD_NAME = "Board Name";

  final BoardService boardService;

  BoardRepository boardRepository;
  UserService userService;
  MongoTemplate mongoTemplate;

  public BoardServiceTest() {
    boardRepository = mock(BoardRepository.class);
    userService = mock(UserService.class);
    mongoTemplate = mock(MongoTemplate.class);
    this.boardService = new BoardServiceImpl(boardRepository, userService, mongoTemplate);
  }

  @Test
  void givenPublicBoard_whenGetBoardById_thenReturnBoardResponse()
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    final Board board = givenBoardWithAccessLevel(EBoardAccessLevel.PUBLIC);
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));

    final Board actualBoard = boardService.getBoard(BOARD_ID_1);
    assertThat(actualBoard, equalTo(board));
  }

  public static Board givenBoardWithAccessLevel(final EBoardAccessLevel accessLevel) {
    final Board board = new Board(BOARD_NAME, Collections.emptyList(), Collections.emptyList(),
        accessLevel);
    board.setId(BOARD_ID_1);
    return board;
  }

}
