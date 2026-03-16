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
import io.github.lucyfred.bflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void createDefaultCategories(User user) {
        List<Category> categories = new ArrayList<>();
        Category expenseCategory = Category
                .builder()
                .name("Expenses")
                .type(CategoryTypes.EXPENSE)
                .color("#FF0000")
                .user(user)
                .build();
        categories.add(expenseCategory);
        Category incomeCategory = Category
                .builder()
                .name("Incomes")
                .type(CategoryTypes.INCOME)
                .color("#00FF00")
                .user(user)
                .build();
        categories.add(incomeCategory);

        categories.forEach(categoryRepository::save);
    }

    @Override
    public List<CategoryResponseDto> getAllUserCategories(Long userId) {
        return categoryMapper.toListCategoryResponseDto(categoryRepository.getAllByUserId(userId));
    }

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto, Long userId) {
        if(categoryRepository.existsByNameAndUserId(categoryRequestDto.name(), userId)) {
            throw new DuplicateResourceException("Category already exists");
        }

        Category category = categoryMapper.toCategoryFromRequest(categoryRequestDto);
        User user = userRepository.getReferenceById(userId);
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(Long idCategory, CategoryRequestDto categoryRequestDto, Long userId) {
        if(!categoryRepository.existsByIdAndUserId(idCategory, userId)){
            throw new ResourceNotFoundException("Category not found");
        }

        Optional<Category> category = categoryRepository.findById(idCategory);

        Category categoryToUpdate = category.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        User user = userRepository.getReferenceById(userId);

        categoryToUpdate.setName(categoryRequestDto.name());
        categoryToUpdate.setType(categoryRequestDto.type());
        categoryToUpdate.setColor(categoryRequestDto.color());
        categoryToUpdate.setUser(user);

        return categoryMapper.toCategoryResponseDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    public void deleteCategory(Long idCategory, Long userId) {
        if(!categoryRepository.existsByIdAndUserId(idCategory, userId)){
            throw new ResourceNotFoundException("Category not found");
        }else {
            categoryRepository.deleteById(idCategory);
        }
    }
}
