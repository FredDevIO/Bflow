package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.CategoryRequestDto;
import io.github.lucyfred.bflow.dto.CategoryResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.service.impl.CategoryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryServiceImpl categoryService;

    @GetMapping
    public List<CategoryResponseDto> getAllUserCategories(@AuthenticationPrincipal User user) {
        return categoryService.getAllUserCategories(user.getId());
    }

    @PostMapping
    public CategoryResponseDto createCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto, @AuthenticationPrincipal User user) {
        return categoryService.createCategory(categoryRequestDto, user.getId());
    }

    @PutMapping("/{category_id}")
    public CategoryResponseDto updateCategory(@PathVariable("category_id") Long categoryId, @Valid @RequestBody CategoryRequestDto categoryRequestDto, @AuthenticationPrincipal User user) {
        return categoryService.updateCategory(categoryId, categoryRequestDto, user.getId());
    }

    @DeleteMapping("/{category_id}")
    public void deleteCategory(@PathVariable("category_id") Long categoryId, @AuthenticationPrincipal User user) {
        categoryService.deleteCategory(categoryId, user.getId());
    }

}
