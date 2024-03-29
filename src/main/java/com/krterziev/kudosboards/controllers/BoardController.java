package com.krterziev.kudosboards.controllers;

import static com.krterziev.kudosboards.transformers.ResponseTransformer.toBoardResponse;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.IdRequest;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.payload.response.IdResponse;
import com.krterziev.kudosboards.services.BoardService;
import com.krterziev.kudosboards.transformers.ResponseTransformer;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/boards")
public class BoardController {

  final BoardService boardService;

  @Autowired
  public BoardController(final BoardService boardService) {
    this.boardService = boardService;
  }

  @GetMapping
  public ResponseEntity<List<BoardResponse>> getBoards() {
    final List<Board> boards = boardService.getAllBoards();
    return ResponseEntity.ok(boards.stream().map(ResponseTransformer::toBoardResponse).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<BoardResponse> getBoard(@PathVariable final String id) {
    final Optional<Board> board = boardService.getBoard(id);
    if (board.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format("Board %s not found", id));
    }
    return ResponseEntity.ok(toBoardResponse(board.get()));
  }

  @PostMapping()
  public ResponseEntity<IdResponse> createBoard(
      @RequestBody final CreateBoardRequest boardRequest) {
    final Board board;
    try {
      board = boardService.createBoard(boardRequest);
    } catch (UserAuthenticationException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
    return ResponseEntity.created(URI.create("/api/boards/" + board.getId())).build();
  }

  @PutMapping("/{boardId}/users")
  public ResponseEntity<Void> addUserToBoard(@PathVariable final String boardId,
      @RequestBody final IdRequest userId) {
    try {
      boardService.addUserToBoard(userId.id(), boardId);
    } catch (ResourceNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    } catch (UserAuthenticationException | UserAuthorisationException  ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
    return ResponseEntity.ok().build();
  }


}
