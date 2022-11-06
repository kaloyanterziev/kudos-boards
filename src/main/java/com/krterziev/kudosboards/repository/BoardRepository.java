package com.krterziev.kudosboards.repository;

import com.krterziev.kudosboards.models.Board;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BoardRepository extends MongoRepository<Board, String>{
}
