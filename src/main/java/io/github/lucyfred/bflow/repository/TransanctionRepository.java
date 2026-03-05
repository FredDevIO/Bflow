package io.github.lucyfred.bflow.repository;

import io.github.lucyfred.bflow.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransanctionRepository extends JpaRepository<Transaction, Long> {
}
