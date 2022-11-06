package com.krterziev.kudosboards.controllers;

import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.CreateMessageRequest;
import com.krterziev.kudosboards.payload.response.BoardResponse;
import com.krterziev.kudosboards.payload.response.IdResponse;
import com.krterziev.kudosboards.services.BoardService;
import com.krterziev.kudosboards.transformers.ResponseTransformer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.krterziev.kudosboards.transformers.ResponseTransformer.toBoardResponse;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/boards")
public class BoardController {
    final BoardService boardService;

    public BoardController(BoardService boardService) {
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Board %s not found", id));
        }
        return ResponseEntity.ok(toBoardResponse(board.get()));
    }

    @PostMapping()
    public ResponseEntity<IdResponse> createBoard(@RequestBody final CreateBoardRequest boardRequest) {
        final Board board = boardService.createBoard(boardRequest);
        return ResponseEntity.ok(new IdResponse(board.getId()));
    }

    @PostMapping("/{boardId}/messages")
    public ResponseEntity<IdResponse> addMessageToBoard(@PathVariable final String boardId,
                                                        @RequestBody final CreateMessageRequest messageRequest) {
        final Optional<Board> board = boardService.getBoard(boardId);
        if(board.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Board %s not found", boardId));
        }

        final Message message = boardService.addMessageToBoard(board.get(), messageRequest);
        return ResponseEntity.ok(new IdResponse(message.getId()));
    }


}
