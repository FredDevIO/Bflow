package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.CategoryRequestDto;
import io.github.lucyfred.bflow.dto.CategoryResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.service.impl.CategoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category operations endpoint")
public class CategoryController {
    private final CategoryServiceImpl categoryService;

    @GetMapping
    @Operation(summary = "Retrieve all categories", description = "Retrieve all user categories")
    public List<CategoryResponseDto> getAllUserCategories(@AuthenticationPrincipal User user) {
        return categoryService.getAllUserCategories(user.getId());
    }

    @PostMapping
    @Operation(summary = "Create a category", description = "Create a user category")
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryRequestDto, user.getId()));
    }

    @PutMapping("/{category_id}")
    @Operation(summary = "Update a category", description = "Update a user's category")
    public CategoryResponseDto updateCategory(@PathVariable("category_id") Long categoryId, @Valid @RequestBody CategoryRequestDto categoryRequestDto, @AuthenticationPrincipal User user) {
        return categoryService.updateCategory(categoryId, categoryRequestDto, user.getId());
    }

    @DeleteMapping("/{category_id}")
    public void deleteCategory(@PathVariable("category_id") Long categoryId, @AuthenticationPrincipal User user) {
        categoryService.deleteCategory(categoryId, user.getId());
    }

}
