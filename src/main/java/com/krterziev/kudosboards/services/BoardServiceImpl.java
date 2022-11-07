package com.krterziev.kudosboards.services;

import static com.krterziev.kudosboards.helpers.UserHelper.getUsername;
import static com.krterziev.kudosboards.models.EBoardAccessLevel.PUBLIC;

import com.krterziev.kudosboards.exceptions.UserAuthenticationException;
import com.krterziev.kudosboards.models.Board;
import com.krterziev.kudosboards.models.Message;
import com.krterziev.kudosboards.models.User;
import com.krterziev.kudosboards.payload.request.CreateBoardRequest;
import com.krterziev.kudosboards.payload.request.CreateMessageRequest;
import com.krterziev.kudosboards.repository.BoardRepository;
import com.krterziev.kudosboards.repository.MessageRepository;
import com.krterziev.kudosboards.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl implements BoardService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BoardServiceImpl.class);

  final BoardRepository boardRepository;
  final UserRepository userRepository;
  final MessageRepository messageRepository;

  final MongoTemplate mongoTemplate;

  @Autowired
  public BoardServiceImpl(final BoardRepository boardRepository,
      final UserRepository userRepository, MessageRepository messageRepository,
      MongoTemplate mongoTemplate) {
    this.boardRepository = boardRepository;
    this.userRepository = userRepository;
    this.messageRepository = messageRepository;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Optional<Board> getBoard(String id) {
    final Query query = new Query();
    query.addCriteria(Criteria.where("id").is(id));
    final Optional<Board> board = boardRepository.findById(id);
      if (board.isEmpty()) {
          return board;
      }

    return switch (board.get().getAccessLevel()) {
      case PUBLIC, LINK -> board;
      case PRIVATE -> checkIfUserIsPartOfBoardUsers(board.get()) ? board : Optional.empty();
    };
  }

  @Override
  public List<Board> getAllBoards() {
    Optional<User> user = getUser();

    final Query query = new Query();
    if (user.isPresent()) {
      query.addCriteria(Criteria.where("users.id").is(user.orElseThrow().getId()));
    } else {
      query.addCriteria(Criteria.where("accessLevel").is(PUBLIC.toString()));
    }
    return mongoTemplate.find(query, Board.class);
  }

  @Override
  public Board createBoard(CreateBoardRequest boardRequest) {
    final User user = getAuthUser();
    final Board board = new Board(boardRequest.name(),
        Collections.emptyList(),
        Collections.singletonList(user),
        boardRequest.accessLevel());
    return boardRepository.save(board);
  }

  @Override
  public Message addMessageToBoard(final Board board, CreateMessageRequest messageRequest) {
    final Optional<User> user = getUser();
    final Message message = new Message(messageRequest.text(), messageRequest.image(),
        Instant.now(), user.orElse(null));

    messageRepository.save(message);
    final Query query = new Query();
    query.addCriteria(Criteria.where("id").is(board.getId()));
    final Update update = new Update();
    update.addToSet("messages", message);
    mongoTemplate.updateFirst(query, update, Board.class);
    return message;
  }

  private boolean checkIfUserIsPartOfBoardUsers(final Board board) {
    final Optional<User> user = getUser();
    if (user.isPresent()) {
      return board.getUsers().stream()
          .anyMatch(boardUser -> boardUser.getId().equals(user.orElseThrow().getId()));
    }
    return false;
  }

  private Optional<User> getUser() {
    final String username;
    try {
      username = getUsername();
    } catch (final UserAuthenticationException ex) {
      return Optional.empty();
    }
    return userRepository.findByUsername(username);
  }

  private User getAuthUser() {
    final String username;
    try {
      username = getUsername();
    } catch (final UserAuthenticationException ex) {
      LOGGER.error("User is not authenticated", ex);
      throw new RuntimeException(ex);
    }
    return userRepository.findByUsername(username).orElseThrow();
  }


}
