package com.krterziev.kudosboards.controllers;

import static com.krterziev.kudosboards.matchers.ResponseBodyMatchers.responseBody;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.EBoardAccessLevel;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.IdRequest;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.services.BoardService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@WebMvcTest(value = BoardController.class)
class BoardControllerTest extends ControllerTest {
  private static final String PATH = "/api/boards/";
  final static String BOARD_ID_1 = "asdfg-12345-hjkl";
  final static String BOARD_ID_2 = "qwert-67890-uiop";
  static final String BOARD_NAME = "Board Name";
  static final String USER_ID = "zxcv-3456-bnmz";

  @MockBean
  private BoardService service;


  @Test
  void givenExistingBoard_whenGetBoardById_thenReturnBoardResponse()
      throws Exception {
    final Board board = givenBoard();

    when(service.getBoard(BOARD_ID_1)).thenReturn(board);

    mvc.perform(get(PATH + BOARD_ID_1))
        .andExpect(status().isOk())
        .andExpect(responseBody().containsObjectAsJson(board, BoardResponse.class));

    verify(service, times(1)).getBoard(BOARD_ID_1);
  }

  @Test
  void givenNonExistingBoard_whenGetBoardById_thenReturn404()
      throws Exception {
    when(service.getBoard(BOARD_ID_1)).thenThrow(new ResourceNotFoundException("Board", BOARD_ID_1));

    mvc.perform(get(PATH + BOARD_ID_1))
        .andExpect(status().isNotFound());

    verify(service, times(1)).getBoard(BOARD_ID_1);
  }

  @Test
  void givenPrivateBoardAndUserNotLoggedIn_whenGetBoardById_thenReturnUnauthorized()
      throws Exception {
    when(service.getBoard(BOARD_ID_1)).thenThrow(new UserAuthenticationException());

    mvc.perform(get(PATH + BOARD_ID_1))
        .andExpect(status().isUnauthorized());

    verify(service, times(1)).getBoard(BOARD_ID_1);
  }

  @Test
  void givenPrivateBoardAndUserNotPartOfBoard_whenGetBoardById_thenReturnUnauthorized()
      throws Exception {
    when(service.getBoard(BOARD_ID_1)).thenThrow(new UserAuthorisationException());

    mvc.perform(get(PATH + BOARD_ID_1))
        .andExpect(status().isUnauthorized());

    verify(service, times(1)).getBoard(BOARD_ID_1);
  }

  @Test
  void givenOnePublicBoard_whenGetAllBoards_thenReturnBoardResponse() throws Exception {
    final List<Board> boards = List.of(givenBoard());

    when(service.getAllBoards()).thenReturn(boards);

    mvc.perform(get("/api/boards"))
        .andExpect(status().isOk())
        .andExpect(responseBody().containsObjectsAsJson(expected(boards), BoardResponse.class));

    verify(service, times(1)).getAllBoards();
  }

  @Test
  void givenPublicBoardAndPrivateBoardOwnedByCaller_whenGetAllBoards_thenReturnTwoBoardResponse()
      throws Exception {
    final List<Board> boards = givenBoards();

    when(service.getAllBoards()).thenReturn(boards);

    mvc.perform(get("/api/boards"))
        .andExpect(status().isOk())
        .andExpect(
            responseBody().containsObjectsAsJson(expected(boards), BoardResponse.class));

    verify(service, times(1)).getAllBoards();
  }

  @Test
  void givenZeroPublicBoard_whenGetAllBoards_thenReturnEmptyList() throws Exception {

    when(service.getAllBoards()).thenReturn(Collections.emptyList());

    mvc.perform(get("/api/boards"))
        .andExpect(status().isOk())
        .andExpect(
            responseBody().containsObjectsAsJson(Collections.emptyList(), BoardResponse.class));

    verify(service, times(1)).getAllBoards();
  }

  @Test
  void givenBoardRequest_whenPostBoard_thenReturnNewBoardId() throws Exception {
    objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

    final CreateBoardRequest boardRequest = givenBoardRequest();
    final Board createdBoard = givenBoard();

    when(service.createBoard(boardRequest)).thenReturn(createdBoard);

    mvc.perform(post("/api/boards")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(boardRequest)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", PATH + BOARD_ID_1));

    verify(service, times(1)).createBoard(boardRequest);

  }

  @Test
  void givenBoardRequestAndUserNotLoggedIn_whenPostBoard_thenReturnUnauthorized() throws Exception {
    final CreateBoardRequest boardRequest = givenBoardRequest();

    when(service.createBoard(boardRequest)).thenThrow(new UserAuthenticationException());

    mvc.perform(post("/api/boards")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(boardRequest)))
        .andExpect(status().isUnauthorized());

    verify(service, times(1)).createBoard(boardRequest);
  }

  @Test
  void givenBoardIdAndUserId_whenAddUserToBoard_thenReturnOk() throws Exception {
    final IdRequest userIdRequest = new IdRequest(USER_ID);

    mvc.perform(put(PATH + BOARD_ID_1 + "/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(userIdRequest)))
        .andExpect(status().isOk());

    verify(service, times(1)).addUserToBoard(USER_ID, BOARD_ID_1);
  }

  @Test
  void givenNonExistingBoardIdAndUserId_whenAddUserToBoard_thenReturnNotFound() throws Exception {
    final IdRequest userIdRequest = new IdRequest(USER_ID);
    doThrow(new ResourceNotFoundException("Board", BOARD_ID_1))
        .when(service).addUserToBoard(USER_ID, BOARD_ID_1);

    mvc.perform(put(PATH + BOARD_ID_1 + "/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(userIdRequest)))
        .andExpect(status().isNotFound());

    verify(service, times(1)).addUserToBoard(USER_ID, BOARD_ID_1);
  }

  @Test
  void givenBoardIdAndNonExistingUserId_whenAddUserToBoard_thenReturnNotFound() throws Exception {
    final IdRequest userIdRequest = new IdRequest(USER_ID);
    doThrow(new ResourceNotFoundException("User", USER_ID))
        .when(service).addUserToBoard(USER_ID, BOARD_ID_1);

    mvc.perform(put(PATH + BOARD_ID_1 + "/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(userIdRequest)))
        .andExpect(status().isNotFound());

    verify(service, times(1)).addUserToBoard(USER_ID, BOARD_ID_1);
  }

  @Test
  void givenBoardIdAndUserIdButNotLoggedIn_whenAddUserToBoard_thenReturnUnauthorized() throws Exception {
    final IdRequest userIdRequest = new IdRequest(USER_ID);
    doThrow(new UserAuthenticationException())
        .when(service).addUserToBoard(USER_ID, BOARD_ID_1);

    mvc.perform(put(PATH + BOARD_ID_1 + "/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(userIdRequest)))
        .andExpect(status().isUnauthorized());

    verify(service, times(1)).addUserToBoard(USER_ID, BOARD_ID_1);
  }

  @Test
  void givenBoardIdAndUserNotPartOfBoard_whenAddUserToBoard_thenReturnUnauthorized() throws Exception {
    final IdRequest userIdRequest = new IdRequest(USER_ID);
    doThrow(new UserAuthorisationException())
        .when(service).addUserToBoard(USER_ID, BOARD_ID_1);

    mvc.perform(put(PATH + BOARD_ID_1 + "/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(userIdRequest)))
        .andExpect(status().isUnauthorized());

    verify(service, times(1)).addUserToBoard(USER_ID, BOARD_ID_1);
  }

  private static Board givenBoard() {
    final Board board = new Board(BOARD_NAME, Collections.emptyList(), Collections.emptyList(),
        EBoardAccessLevel.PUBLIC);
    board.setId(BOARD_ID_1);
    return board;
  }

  private static List<Board> givenBoards() {
    final Board board1 = new Board(BOARD_NAME, Collections.emptyList(), Collections.emptyList(),
        EBoardAccessLevel.PUBLIC);
    board1.setId(BOARD_ID_1);
    final Board board2 = new Board(BOARD_NAME, Collections.emptyList(), Collections.emptyList(),
        EBoardAccessLevel.PRIVATE);
    board2.setId(BOARD_ID_2);
    return List.of(board1, board2);
  }

  private static CreateBoardRequest givenBoardRequest() {
    return new CreateBoardRequest(BOARD_NAME, EBoardAccessLevel.PUBLIC);
  }

  private static List<Object> expected(final List<Board> boards) {
    return boards.stream().map(b -> (Object) b).toList();
  }

}
