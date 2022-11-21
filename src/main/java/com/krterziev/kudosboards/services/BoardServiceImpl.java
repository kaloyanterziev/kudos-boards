package com.krterziev.kudosboards.services;

import static com.krterziev.kudosboards.models.EBoardAccessLevel.PUBLIC;

import com.krterziev.kudosboards.exceptions.ResourceNotFoundException;
import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.exceptions.UserAuthorisationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.models.User;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.repository.BoardRepository;
import com.krterziev.kudosboards.security.services.UserService;
import com.mongodb.DBRef;
import com.mongodb.client.result.UpdateResult;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl implements BoardService {

  final BoardRepository boardRepository;
  final UserService userService;
  final MongoTemplate mongoTemplate;

  @Autowired
  public BoardServiceImpl(final BoardRepository boardRepository,
      final UserService userService,
      final MongoTemplate mongoTemplate) {
    this.boardRepository = boardRepository;
    this.userService = userService;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Board getBoard(final String id)
      throws ResourceNotFoundException, UserAuthorisationException, UserAuthenticationException {
    final Query query = new Query();
    query.addCriteria(Criteria.where("id").is(id));
    final Board board = boardRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Board", id));

    return switch (board.getAccessLevel()) {
      case PUBLIC, LINK -> board;
      case PRIVATE -> checkIfUserIsPartOfBoardAndReturnBoard(board);
    };
  }

  @Override
  public List<Board> getAllBoards() {
    Optional<User> user = userService.getCurrentUser();

    final Query query = new Query();
    if (user.isPresent()) {
      query.addCriteria(Criteria.where("users.id").is(user.get().getId()));
    } else {
      query.addCriteria(Criteria.where("accessLevel").is(PUBLIC.toString()));
    }
    return mongoTemplate.find(query, Board.class);
  }

  @Override
  public Board createBoard(CreateBoardRequest boardRequest) throws UserAuthenticationException {
    final User user = userService.getCurrentAuthUser();
    final Board board = new Board(boardRequest.name(),
        Collections.emptyList(),
        Collections.singletonList(user),
        boardRequest.accessLevel());
    return boardRepository.save(board);
  }

  @Override
  public void addMessageToBoard(final String boardId, final Message message)
      throws ResourceNotFoundException {
    final Query boardQuery = Query.query(Criteria.where("id").is(boardId));
    final Update update = new Update().addToSet("messages", message);
    final UpdateResult result = mongoTemplate.updateFirst(boardQuery, update, Board.class);
    if (result.getMatchedCount() != 1L) {
      throw new ResourceNotFoundException("Board", boardId);
    }
  }

  @Override
  public void deleteMessageFromBoard(final String boardId, final String messageId)
      throws ResourceNotFoundException {
    final Query boardQuery = Query.query(Criteria.where("id").is(boardId));
    final Query messageQuery = Query.query(Criteria.where("$id").is(new ObjectId(messageId)));
    final Update messageUpdate = new Update().pull("messages", messageQuery);
    final UpdateResult result = mongoTemplate.updateFirst(boardQuery, messageUpdate, Board.class);
    if (result.getMatchedCount() != 1L) {
      throw new ResourceNotFoundException("Board", boardId);
    }
    if (result.getModifiedCount() != 1L) {
      throw new ResourceNotFoundException("Message", messageId);
    }
  }

  @Override
  public void addUserToBoard(final String userId, final String boardId)
      throws UserAuthenticationException, ResourceNotFoundException, UserAuthorisationException {

    final Optional<Board> board = boardRepository.findById(boardId);
    final Optional<User> futureBoardUser = userService.getUser(userId);
    if (board.isEmpty()) {
      throw new ResourceNotFoundException("Board", boardId);
    } else if (futureBoardUser.isEmpty()) {
      throw new ResourceNotFoundException("User", userId);
    }

    final User user = userService.getCurrentAuthUser();
    if (!checkIfUserIsPartOfBoard(user, board.get())) {
      throw new UserAuthorisationException();
    }

    final Query boardQuery = Query.query(Criteria.where("id").is(new ObjectId(boardId)));
    final Update update = new Update().push("users", new DBRef("users", new ObjectId(userId)));
    mongoTemplate.updateFirst(boardQuery, update, Board.class);
  }

  private boolean checkIfUserIsPartOfBoard(final User user, final Board board) {
    return board.getUsers().stream()
        .anyMatch(boardUser -> boardUser.getId().equals(user.getId()));
  }

  private Board checkIfUserIsPartOfBoardAndReturnBoard(final Board board)
      throws UserAuthenticationException, UserAuthorisationException {
    final User user = userService.getCurrentAuthUser();
    if(checkIfUserIsPartOfBoard(user, board)) {
      return board;
    } else {
      throw new UserAuthorisationException();
    }
  }


}
