package com.krterziev.kudosboards.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.EBoardAccessLevel;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.models.User;
import com.krterziev.kudosboards.repository.BoardRepository;
import com.krterziev.kudosboards.security.services.UserService;
import com.mongodb.client.result.UpdateResult;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@TestComponent
class BoardServiceTest {

  static final String BOARD_ID_1 = "1234567890abcdef12345678";
  static final String BOARD_ID_2 = "qwert-67890-uiop";
  static final String USER_ID = "zxcv-9876-mnbv";
  static final String USER_ID_2 = "abcdef1234567890fedcba12";
  static final String UNKNOWN_USER_ID = "fedcba120123456789abcdef";
  static final String MESSAGE_ID = "6368075d1fec0c3e0a03394e";
  static final String USERNAME = "username";
  static final String EMAIL = "email@gmail.com";
  static final String PASSWORD = "pa55word";
  static final String BOARD_NAME = "Board Name";


  final BoardService boardService;

  final BoardRepository boardRepository;
  final UserService userService;
  final MongoTemplate mongoTemplate;

  public BoardServiceTest() {
    boardRepository = mock(BoardRepository.class);
    userService = mock(UserService.class);
    mongoTemplate = mock(MongoTemplate.class);
    this.boardService = new BoardServiceImpl(boardRepository, userService, mongoTemplate);
  }

  // getBoard
  @ParameterizedTest
  @EnumSource(value = EBoardAccessLevel.class, names = {"LINK", "PUBLIC"})
  void givenBoard_whenGetBoardById_thenReturnBoardResponse(final EBoardAccessLevel level)
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    final Board board = givenBoard(BOARD_ID_1, level
    );
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));

    final Board actualBoard = boardService.getBoard(BOARD_ID_1);
    assertThat(actualBoard, equalTo(board));

    verify(boardRepository, times(1)).findById(BOARD_ID_1);
  }

  @Test
  void givenNonExistingBoard_whenGetBoardById_thenThrowResourceNotFoundException() {
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.empty());

    Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
      boardService.getBoard(BOARD_ID_1);
    });
    assertThat(exception.getMessage(),
        is("Resource Board with id " + BOARD_ID_1 + " is not found."));

    verify(boardRepository, times(1)).findById(BOARD_ID_1);
  }

  @Test
  void givenPrivateBoardAndUserIsPartOfBoard_whenGetBoardById_thenReturnBoard()
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    final Board board = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE
    );
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));
    final User user = givenUser(USER_ID);
    when(userService.getCurrentAuthUser()).thenReturn(user);

    final Board actualBoard = boardService.getBoard(BOARD_ID_1);
    assertThat(actualBoard, equalTo(board));

    verify(boardRepository, times(1)).findById(BOARD_ID_1);
    verify(userService, times(1)).getCurrentAuthUser();
  }

  @Test
  void givenPrivateBoardAndUserIsNotPartOfBoard_whenGetBoardById_thenThrowUserAuthorizationException()
      throws UserAuthenticationException {
    final Board board = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE
    );
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));
    final User user = givenUser(UNKNOWN_USER_ID);
    when(userService.getCurrentAuthUser()).thenReturn(user);

    Exception exception = assertThrows(UserAuthorisationException.class, () -> {
      boardService.getBoard(BOARD_ID_1);
    });
    assertThat(exception.getMessage(), is("User not authorised"));

    verify(boardRepository, times(1)).findById(BOARD_ID_1);
    verify(userService, times(1)).getCurrentAuthUser();
  }

  @Test
  void givenPrivateBoardAndNotLoggedInUser_whenGetBoardById_thenThrowUserAuthenticationException()
      throws UserAuthenticationException {
    final Board board = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE
    );
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));
    when(userService.getCurrentAuthUser()).thenThrow(new UserAuthenticationException());

    Exception exception = assertThrows(UserAuthenticationException.class, () -> {
      boardService.getBoard(BOARD_ID_1);
    });
    assertThat(exception.getMessage(), is("User not authenticated"));

    verify(boardRepository, times(1)).findById(BOARD_ID_1);
    verify(userService, times(1)).getCurrentAuthUser();
  }

  @Test
  void givenPresentUser_whenGetAllBoards_thenReturnBothBoards() {
    final User user = givenUser(USER_ID);
    final Board privateBoard = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE);
    final Board publicBoard = givenBoard(BOARD_ID_2, EBoardAccessLevel.PUBLIC);
    List<Board> boards = List.of(privateBoard, publicBoard);

    when(userService.getCurrentUser()).thenReturn(Optional.of(user));
    when(mongoTemplate.find(any(Query.class), eq(Board.class))).thenReturn(boards);

    final List<Board> actualBoards = boardService.getAllBoards();

    assertThat(actualBoards, equalTo(boards));
    verify(userService, times(1)).getCurrentUser();
    verify(mongoTemplate, times(1)).find(any(Query.class), eq(Board.class));
  }

  @Test
  void givenNotLoggedInUser_whenGetAllBoards_thenReturnPublicBoard() {
    final Board publicBoard = givenBoard(BOARD_ID_2, EBoardAccessLevel.PUBLIC);
    List<Board> boards = List.of(publicBoard);

    when(userService.getCurrentUser()).thenReturn(Optional.empty());
    when(mongoTemplate.find(any(Query.class), eq(Board.class))).thenReturn(boards);

    final List<Board> actualBoards = boardService.getAllBoards();

    assertThat(actualBoards, equalTo(boards));
    verify(userService, times(1)).getCurrentUser();
    verify(mongoTemplate, times(1)).find(any(Query.class), eq(Board.class));
  }

  @Test
  void givenBoardAndMessage_whenAddMessageToBoard_thenOneBoardIsUpdated()
      throws ResourceNotFoundException {
    final UpdateResult resultMock = mock(UpdateResult.class);
    when(resultMock.getMatchedCount()).thenReturn(1L);
    when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Board.class)))
        .thenReturn(resultMock);

    boardService.addMessageToBoard(BOARD_ID_1, givenMessage());

    verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class),
        eq(Board.class));
  }

  @Test
  void givenNonExistentBoardAndMessage_whenAddMessageToBoard_thenNoBoardIsUpdatedAndThrowException() {
    final UpdateResult resultMock = mock(UpdateResult.class);
    when(resultMock.getMatchedCount()).thenReturn(0L);
    when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Board.class)))
        .thenReturn(resultMock);

    final Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
      boardService.addMessageToBoard(BOARD_ID_1, givenMessage());
    });
    assertThat(exception.getMessage(),
        is("Resource Board with id " + BOARD_ID_1 + " is not found."));

    verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class),
        eq(Board.class));
  }

  @Test
  void givenBoardAndMessage_whenDeleteMessageFromBoard_thenOneBoardIsUpdated()
      throws ResourceNotFoundException {
    final UpdateResult resultMock = mock(UpdateResult.class);
    when(resultMock.getMatchedCount()).thenReturn(1L);
    when(resultMock.getModifiedCount()).thenReturn(1L);

    when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Board.class)))
        .thenReturn(resultMock);

    boardService.deleteMessageFromBoard(BOARD_ID_1, MESSAGE_ID);

    verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class),
        eq(Board.class));
  }

  @Test
  void givenNonExistentBoardAndMessage_whenDeleteMessageFromBoard_thenNoBoardIsUpdatedAndThrowException() {
    final UpdateResult resultMock = mock(UpdateResult.class);
    when(resultMock.getMatchedCount()).thenReturn(0L);
    when(resultMock.getModifiedCount()).thenReturn(1L);
    when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Board.class)))
        .thenReturn(resultMock);

    final Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
      boardService.deleteMessageFromBoard(BOARD_ID_1, MESSAGE_ID);
    });
    assertThat(exception.getMessage(),
        is("Resource Board with id " + BOARD_ID_1 + " is not found."));

    verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class),
        eq(Board.class));
  }

  @Test
  void givenNonExistentMessageAndBoard_whenDeleteMessageFromBoard_thenNoBoardIsUpdatedAndThrowException() {
    final UpdateResult resultMock = mock(UpdateResult.class);
    when(resultMock.getMatchedCount()).thenReturn(1L);
    when(resultMock.getModifiedCount()).thenReturn(0L);
    when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Board.class)))
        .thenReturn(resultMock);

    final Exception exception = assertThrows(ResourceNotFoundException.class, () ->
        boardService.deleteMessageFromBoard(BOARD_ID_1, MESSAGE_ID));
    assertThat(exception.getMessage(),
        is("Resource Message with id " + MESSAGE_ID + " is not found."));

    verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class),
        eq(Board.class));
  }

  @Test
  void givenUserLoggedInAndValidBoardAndUser_whenAddUserToBoard_thenModifyBoard()
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    final Board board = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE);
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));

    final User futureBoardUser = givenUser(USER_ID_2);
    when(userService.getUser(USER_ID_2)).thenReturn(Optional.of(futureBoardUser));

    final User user = givenUser(USER_ID);
    when(userService.getCurrentAuthUser()).thenReturn(user);

    boardService.addUserToBoard(USER_ID_2, BOARD_ID_1);
    verify(boardRepository, times(1)).findById(BOARD_ID_1);
    verify(userService, times(1)).getUser(USER_ID_2);
    verify(userService, times(1)).getCurrentAuthUser();
  }

  @Test
  void givenUserNotLoggedIn_whenAddUserToBoard_thenThrowUserAuthenticationException()
      throws UserAuthenticationException {
    when(userService.getCurrentAuthUser()).thenThrow(new UserAuthenticationException());

    final Exception exception = assertThrows(UserAuthenticationException.class, () ->
        boardService.addUserToBoard(USER_ID_2, BOARD_ID_1));
    assertThat(exception.getMessage(), is("User not authenticated"));

    verify(userService, times(1)).getCurrentAuthUser();
  }

  @Test
  void givenNonExistingBoard_whenAddUserToBoard_thenThrowResourceNotFoundException()
      throws UserAuthenticationException {
    final User user = givenUser(USER_ID);
    when(userService.getCurrentAuthUser()).thenReturn(user);
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.empty());

    final Exception exception = assertThrows(ResourceNotFoundException.class, () ->
        boardService.addUserToBoard(USER_ID_2, BOARD_ID_1));
    assertThat(exception.getMessage(),
        is("Resource Board with id " + BOARD_ID_1 + " is not found."));

    verify(userService, times(1)).getCurrentAuthUser();
    verify(boardRepository, times(1)).findById(BOARD_ID_1);
  }

  @Test
  void givenBoardAndLoggedInUserButNotPartOfBoard_whenAddUserToBoard_thenThrowUserAuthorizationException()
      throws UserAuthenticationException {
    final User user = givenUser(UNKNOWN_USER_ID);
    when(userService.getCurrentAuthUser()).thenReturn(user);

    final Board board = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE);
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));

    final Exception exception = assertThrows(UserAuthorisationException.class, () ->
        boardService.addUserToBoard(USER_ID_2, BOARD_ID_1));
    assertThat(exception.getMessage(), is("User not authorised"));

    verify(userService, times(1)).getCurrentAuthUser();
    verify(boardRepository, times(1)).findById(BOARD_ID_1);
  }

  @Test
  void givenBoardAndLoggedInUserButNotExistingFutureUser_whenAddUserToBoard_thenThrowResourceNotFoundException()
      throws UserAuthenticationException {
    final Board board = givenBoard(BOARD_ID_1, EBoardAccessLevel.PRIVATE);
    when(boardRepository.findById(BOARD_ID_1)).thenReturn(Optional.of(board));

    when(userService.getUser(UNKNOWN_USER_ID)).thenReturn(Optional.empty());

    final User user = givenUser(USER_ID);
    when(userService.getCurrentAuthUser()).thenReturn(user);

    final Exception exception = assertThrows(ResourceNotFoundException.class, () ->
        boardService.addUserToBoard(UNKNOWN_USER_ID, BOARD_ID_1));
    assertThat(exception.getMessage(),
        is("Resource User with id " + UNKNOWN_USER_ID + " is not found."));

    verify(userService, times(1)).getCurrentAuthUser();
    verify(boardRepository, times(1)).findById(BOARD_ID_1);
  }


  private static Board givenBoard(String boardId, final EBoardAccessLevel accessLevel) {
    final Board board = new Board(BOARD_NAME, Collections.emptyList(),
        Collections.singletonList(givenUser(USER_ID)), accessLevel);
    board.setId(boardId);
    return board;
  }

  private static Message givenMessage() {
    return new Message("text", "image.gif", Instant.now(), null);
  }

  private static User givenUser(final String userId) {
    final User user = new User(USERNAME, EMAIL, PASSWORD);
    user.setId(userId);
    return user;
  }

}
