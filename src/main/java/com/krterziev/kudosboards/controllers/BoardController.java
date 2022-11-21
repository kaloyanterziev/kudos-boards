package com.krterziev.kudosboards.controllers;

import static com.krterziev.kudosboards.transformers.ResponseTransformer.toBoardResponse;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.IdRequest;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.services.BoardService;
import com.krterziev.kudosboards.transformers.ResponseTransformer;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  public ResponseEntity<BoardResponse> getBoard(@PathVariable final String id)
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    final Board board = boardService.getBoard(id);
    return ResponseEntity.ok(toBoardResponse(board));
  }

  @PostMapping()
  public ResponseEntity<Void> createBoard(
      @RequestBody final CreateBoardRequest boardRequest) throws UserAuthenticationException {
    final Board board = boardService.createBoard(boardRequest);
    return ResponseEntity.created(URI.create("/api/boards/" + board.getId())).build();
  }

  @PutMapping("/{boardId}/users")
  public ResponseEntity<Void> addUserToBoard(@PathVariable final String boardId,
      @RequestBody final IdRequest userId)
      throws UserAuthenticationException, UserAuthorisationException, ResourceNotFoundException {
    boardService.addUserToBoard(userId.id(), boardId);
    return ResponseEntity.ok().build();
  }


}
