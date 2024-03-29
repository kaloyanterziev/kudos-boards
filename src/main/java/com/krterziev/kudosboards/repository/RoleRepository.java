package com.krterziev.kudosboards.repository;

import com.krterziev.kudosboards.models.ERole;
import com.krterziev.kudosboards.models.Role;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

  Optional<Role> findByName(ERole name);
}
