package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.CategoryRequestDto;
import io.github.lucyfred.bflow.dto.CategoryResponseDto;
import io.github.lucyfred.bflow.entity.Category;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import io.github.lucyfred.bflow.exception.DuplicateResourceException;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.mapper.CategoryMapper;
import io.github.lucyfred.bflow.repository.CategoryRepository;
import io.github.lucyfred.bflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User mockUser;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockCategory = Category.builder()
                .id(1L)
                .name("Expenses")
                .type(CategoryTypes.EXPENSE)
                .color("#FF0000")
                .user(mockUser)
                .build();
    }

    @Test
    @DisplayName("Should create default categories successfully")
    void createDefaultCategories_success() {
        categoryService.createDefaultCategories(mockUser);

        verify(categoryRepository, times(2)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should get all user categories successfully")
    void getAllUserCategories_success() {
        List<Category> categories = List.of(mockCategory);
        CategoryResponseDto responseDto = new CategoryResponseDto(1L, "Expenses", CategoryTypes.EXPENSE, "#FF0000");
        List<CategoryResponseDto> expected = List.of(responseDto);

        when(categoryRepository.getAllByUserId(1L)).thenReturn(categories);
        when(categoryMapper.toListCategoryResponseDto(categories)).thenReturn(expected);

        List<CategoryResponseDto> result = categoryService.getAllUserCategories(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Expenses", result.get(0).name());
        verify(categoryRepository, times(1)).getAllByUserId(1L);
        verify(categoryMapper, times(1)).toListCategoryResponseDto(categories);
    }

    @Test
    @DisplayName("Should create a category successfully")
    void createCategory_success() {
        CategoryRequestDto request = new CategoryRequestDto("Food", CategoryTypes.EXPENSE, "#FF0000");
        Category categoryFromRequest = Category.builder()
                .name("Food")
                .type(CategoryTypes.EXPENSE)
                .color("#FF0000")
                .build();
        Category savedCategory = Category.builder()
                .id(1L)
                .name("Food")
                .type(CategoryTypes.EXPENSE)
                .color("#FF0000")
                .user(mockUser)
                .build();
        CategoryResponseDto expected = new CategoryResponseDto(1L, "Food", CategoryTypes.EXPENSE, "#FF0000");

        when(categoryRepository.existsByNameAndUserId("Food", 1L)).thenReturn(false);
        when(categoryMapper.toCategoryFromRequest(request)).thenReturn(categoryFromRequest);
        when(userRepository.getReferenceById(1L)).thenReturn(mockUser);
        when(categoryRepository.save(categoryFromRequest)).thenReturn(savedCategory);
        when(categoryMapper.toCategoryResponseDto(savedCategory)).thenReturn(expected);

        CategoryResponseDto result = categoryService.createCategory(request, 1L);

        assertNotNull(result);
        assertEquals("Food", result.name());
        assertEquals(CategoryTypes.EXPENSE, result.type());
        verify(categoryRepository, times(1)).existsByNameAndUserId("Food", 1L);
        verify(categoryMapper, times(1)).toCategoryFromRequest(request);
        verify(userRepository, times(1)).getReferenceById(1L);
        verify(categoryRepository, times(1)).save(categoryFromRequest);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when category already exists")
    void createCategory_duplicateResource() {
        CategoryRequestDto request = new CategoryRequestDto("Food", CategoryTypes.EXPENSE, "#FF0000");

        when(categoryRepository.existsByNameAndUserId("Food", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            categoryService.createCategory(request, 1L);
        });

        verify(categoryRepository, times(1)).existsByNameAndUserId("Food", 1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update a category successfully")
    void updateCategory_success() {
        CategoryRequestDto request = new CategoryRequestDto("UpdatedFood", CategoryTypes.INCOME, "#00FF00");
        Category existingCategory = Category.builder()
                .id(1L)
                .name("Food")
                .type(CategoryTypes.EXPENSE)
                .color("#FF0000")
                .user(mockUser)
                .build();
        Category savedCategory = Category.builder()
                .id(1L)
                .name("UpdatedFood")
                .type(CategoryTypes.INCOME)
                .color("#00FF00")
                .user(mockUser)
                .build();
        CategoryResponseDto expected = new CategoryResponseDto(1L, "UpdatedFood", CategoryTypes.INCOME, "#00FF00");

        when(categoryRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(userRepository.getReferenceById(1L)).thenReturn(mockUser);
        when(categoryRepository.save(existingCategory)).thenReturn(savedCategory);
        when(categoryMapper.toCategoryResponseDto(savedCategory)).thenReturn(expected);

        CategoryResponseDto result = categoryService.updateCategory(1L, request, 1L);

        assertNotNull(result);
        assertEquals("UpdatedFood", result.name());
        assertEquals(CategoryTypes.INCOME, result.type());
        assertEquals("#00FF00", result.color());
        verify(categoryRepository, times(1)).existsByIdAndUserId(1L, 1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).getReferenceById(1L);
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category to update does not exist")
    void updateCategory_notFound() {
        CategoryRequestDto request = new CategoryRequestDto("UpdatedFood", CategoryTypes.INCOME, "#00FF00");

        when(categoryRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.updateCategory(1L, request, 1L);
        });

        verify(categoryRepository, times(1)).existsByIdAndUserId(1L, 1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete a category successfully")
    void deleteCategory_success() {
        when(categoryRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        categoryService.deleteCategory(1L, 1L);

        verify(categoryRepository, times(1)).existsByIdAndUserId(1L, 1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category to delete does not exist")
    void deleteCategory_notFound() {
        when(categoryRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(1L, 1L);
        });

        verify(categoryRepository, times(1)).existsByIdAndUserId(1L, 1L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}
