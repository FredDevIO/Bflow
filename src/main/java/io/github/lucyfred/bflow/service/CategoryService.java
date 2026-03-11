package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.CategoryRequestDto;
import io.github.lucyfred.bflow.dto.CategoryResponseDto;
import io.github.lucyfred.bflow.entity.User;

import java.util.List;

public interface CategoryService {
    void createDefaultCategories(User user);
    List<CategoryResponseDto> getAllUserCategories(Long userId);
    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto, Long userId);
    CategoryResponseDto updateCategory(Long idCategory, CategoryRequestDto categoryRequestDto, Long userId);
    void deleteCategory(Long idCategory, Long userId);
}
