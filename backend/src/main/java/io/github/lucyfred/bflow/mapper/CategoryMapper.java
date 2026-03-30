package io.github.lucyfred.bflow.mapper;

import io.github.lucyfred.bflow.dto.CategoryRequestDto;
import io.github.lucyfred.bflow.dto.CategoryResponseDto;
import io.github.lucyfred.bflow.entity.Category;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto toCategoryResponseDto(Category category);
    List<CategoryResponseDto> toListCategoryResponseDto(List<Category> categories);
    Category toCategoryFromResponse(CategoryResponseDto categoryResponseDto);
    CategoryResponseDto toCategoryDtoFromRequest(CategoryRequestDto categoryRequestDto);
    Category toCategoryFromRequest(CategoryRequestDto categoryRequestDto);
}
