package io.github.lucyfred.bflow.repository;

import io.github.lucyfred.bflow.entity.Transaction;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransanctionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> getAllByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);
    List<Transaction> getAllByUserIdAndCategoryId(Long userId, Long categoryId);
    List<Transaction> getAllByUserIdAndCategoryType(Long userId, CategoryTypes categoryType);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC LIMIT 5")
    List<Transaction> findTop5ByUserIdOrderByTransactionDateAsc(Long userId);
    @EntityGraph(attributePaths = {"category"})
    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId,
            LocalDate start,
            LocalDate end
    );
}
