package io.github.lucyfred.bflow.repository;

import io.github.lucyfred.bflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
