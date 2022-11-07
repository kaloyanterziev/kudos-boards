package com.krterziev.kudosboards.repository;

import com.krterziev.kudosboards.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

}
