package io.github.lucyfred.bflow.repository;

import io.github.lucyfred.bflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
