package io.github.lucyfred.bflow.repository;

import io.github.lucyfred.bflow.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransanctionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> getAllByUserIdAndCategoryId(Long userId, Long categoryId);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
