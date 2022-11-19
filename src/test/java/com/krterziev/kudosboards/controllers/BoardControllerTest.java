package com.krterziev.kudosboards.controllers;

import static com.krterziev.kudosboards.matchers.ResponseBodyMatchers.responseBody;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.EBoardAccessLevel;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.IdRequest;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.payload.response.IdResponse;
import com.krterziev.kudosboards.services.BoardService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;


@WebMvcTest(value = BoardController.class)
public class BoardControllerTest extends ControllerTest {

  private final static String BOARD_ID_1 = "asdfg-12345-hjkl";
  private final static String BOARD_ID_2 = "qwert-67890-uiop";
  private static final String BOARD_NAME = "Board Name";

  private static final String USER_ID = "zxcv-3456-bnmz";

  @MockBean
  private BoardService service;


  @Test
  void givenExistingBoard_whenGetBoardById_thenReturnBoardResponse()
      throws Exception {
    final Board board = givenBoard();

    when(service.getBoard(BOARD_ID_1)).thenReturn(board);

    mvc.perform(get("/api/boards/" + BOARD_ID_1))
        .andExpect(status().isOk())
        .andExpect(responseBody().containsObjectAsJson(board, BoardResponse.class));
  }


  @Test
  void givenNonExistingBoard_whenGetBoardById_thenReturnBoardResponse()
      throws Exception {
    when(service.getBoard(BOARD_ID_1)).thenThrow(new ResourceNotFoundException("Board", BOARD_ID_1));

    mvc.perform(get("/api/boards/" + BOARD_ID_1))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void givenOnePublicBoard_whenGetAllBoards_thenReturnBoardResponse() throws Exception {
    final List<Board> boards = List.of(givenBoard());

    when(service.getAllBoards()).thenReturn(boards);

    mvc.perform(get("/api/boards"))
        .andExpect(status().isOk())
        .andExpect(responseBody().containsObjectsAsJson(expected(boards), BoardResponse.class));
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
  }

  @Test
  void givenZeroPublicBoard_whenGetAllBoards_thenReturnEmptyList() throws Exception {

    when(service.getAllBoards()).thenReturn(Collections.emptyList());

    mvc.perform(get("/api/boards"))
        .andExpect(status().isOk())
        .andExpect(
            responseBody().containsObjectsAsJson(Collections.emptyList(), BoardResponse.class));
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
        .andExpect(header().string("Location", "/api/boards/" + BOARD_ID_1));

  }

  @Test
  void givenBoardIdAndUserId_whenAddUserToBoard_thenReturnOk() throws Exception {
    final IdRequest userIdRequest = new IdRequest(USER_ID);

    mvc.perform(put("/api/boards/" + BOARD_ID_1 + "/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(userIdRequest)))
        .andExpect(status().isOk());
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
