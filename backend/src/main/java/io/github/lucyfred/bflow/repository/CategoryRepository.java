package io.github.lucyfred.bflow.repository;

import io.github.lucyfred.bflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> getAllByUserId(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
}
