package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.CategoryRequestDto;
import io.github.lucyfred.bflow.dto.CategoryResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import io.github.lucyfred.bflow.exception.DuplicateResourceException;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.repository.CategoryRepository;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.security.JwtService;
import io.github.lucyfred.bflow.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryServiceImpl categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
    }

    @Test
    @DisplayName("GET /api/categories - Should return all categories for a given user")
    void getAllUserCategories_success() throws Exception {
        CategoryResponseDto category = new CategoryResponseDto(1L, "Expenses", CategoryTypes.EXPENSE, "#FF0000");

        when(categoryService.getAllUserCategories(anyLong())).thenReturn(List.of(category));

        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Expenses"))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$[0].color").value("#FF0000"));
    }

    @Test
    @DisplayName("GET /api/categories - Should return empty list when no categories exist")
    void getAllUserCategories_emptyList() throws Exception {
        when(categoryService.getAllUserCategories(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/categories - Should create a new category")
    void createCategory_success() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Food", CategoryTypes.EXPENSE, "#FF0000");
        CategoryResponseDto response = new CategoryResponseDto(1L, "Food", CategoryTypes.EXPENSE, "#FF0000");

        when(categoryService.createCategory(any(CategoryRequestDto.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.color").value("#FF0000"));
    }

    @Test
    @DisplayName("POST /api/categories - Should throw DuplicateResourceException when category already exists")
    void createCategory_duplicateResource() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Food", CategoryTypes.EXPENSE, "#FF0000");

        when(categoryService.createCategory(any(CategoryRequestDto.class), anyLong())).thenThrow(new DuplicateResourceException("Category already exists"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/categories/{category_id} - Should update an existing category")
    void updateCategory_success() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("UpdatedFood", CategoryTypes.INCOME, "#00FF00");
        CategoryResponseDto response = new CategoryResponseDto(1L, "UpdatedFood", CategoryTypes.INCOME, "#00FF00");

        when(categoryService.updateCategory(anyLong(), any(CategoryRequestDto.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UpdatedFood"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.color").value("#00FF00"));
    }

    @Test
    @DisplayName("PUT /api/categories/{category_id} - Should throw ResourceNotFoundException when category not found")
    void updateCategory_notFound() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("UpdatedFood", CategoryTypes.INCOME, "#00FF00");

        when(categoryService.updateCategory(anyLong(), any(CategoryRequestDto.class), anyLong())).thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/categories/{category_id} - Should delete an existing category")
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteCategory(anyLong(), anyLong());

        mockMvc.perform(delete("/api/categories/1")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/categories/{category_id} - Should throw ResourceNotFoundException when category not found")
    void deleteCategory_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found")).when(categoryService).deleteCategory(anyLong(), anyLong());

        mockMvc.perform(delete("/api/categories/1")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }
}
