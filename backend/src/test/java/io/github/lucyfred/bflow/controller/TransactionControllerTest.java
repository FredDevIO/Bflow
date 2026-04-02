package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.security.JwtService;
import io.github.lucyfred.bflow.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionServiceImpl transactionService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
    }

    @Test
    @DisplayName("GET /api/transaction - Should return a page of transactions for a given user")
    void getAllTransactions_success() throws Exception {
        TransactionResponseDto transaction = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));
        Page<TransactionResponseDto> page = new PageImpl<>(List.of(transaction));

        when(transactionService.getAllTransactions(anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/transaction")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(100.00))
                .andExpect(jsonPath("$.content[0].description").value("Groceries"))
                .andExpect(jsonPath("$.content[0].categoryType").value("EXPENSE"));
    }

    @Test
    @DisplayName("GET /api/transaction - Should return empty page when no transactions exist")
    void getAllTransactions_emptyPage() throws Exception {
        Page<TransactionResponseDto> emptyPage = new PageImpl<>(List.of());

        when(transactionService.getAllTransactions(anyLong(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/transaction")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/transaction/{transaction_id} - Should return a transaction by id")
    void getTransaction_success() throws Exception {
        TransactionResponseDto transaction = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionService.getTransaction(1L, 1L)).thenReturn(transaction);

        mockMvc.perform(get("/api/transaction/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Groceries"))
                .andExpect(jsonPath("$.categoryType").value("EXPENSE"))
                .andExpect(jsonPath("$.categoryName").value("Food"));
    }

    @Test
    @DisplayName("GET /api/transaction/{transaction_id} - Should return not found when transaction does not exist")
    void getTransaction_notFound() throws Exception {
        when(transactionService.getTransaction(1L, 1L)).thenThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(get("/api/transaction/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/transaction/type/{type} - Should return a list of transactions by type")
    void getTransactionsByType_success() throws Exception {
        TransactionResponseDto transaction = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionService.getAllTransactionsType(anyLong(), any(CategoryTypes.class))).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transaction/type/EXPENSE")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].categoryType").value("EXPENSE"));
    }

    @Test
    @DisplayName("GET /api/transaction/type/{type} - Should return empty list when no transactions of type exist")
    void getTransactionsByType_emptyList() throws Exception {
        when(transactionService.getAllTransactionsType(anyLong(), any(CategoryTypes.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/transaction/type/INCOME")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/transaction/top5 - Should return a list of top 5 transactions")
    void getTop4Transactions_success() throws Exception {
        TransactionResponseDto transaction = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionService.getTop4Transactions(1L)).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transaction/top5")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    @DisplayName("GET /api/transaction/top5 - Should return empty list when no transactions exist")
    void getTop4Transactions_emptyList() throws Exception {
        when(transactionService.getTop4Transactions(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/transaction/top5")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/transaction/between/{start_date}/{end_date} - Should return a list of transactions between dates")
    void getTransactionsBetweenDates_success() throws Exception {
        TransactionResponseDto transaction = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionService.getAllTransactionsBetweenDates(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transaction/between/2024-01-01/2024-01-31")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].transactionDate").value("2024-01-15"));
    }

    @Test
    @DisplayName("GET /api/transaction/between/{start_date}/{end_date} - Should return empty list when no transactions in date range")
    void getTransactionsBetweenDates_emptyList() throws Exception {
        when(transactionService.getAllTransactionsBetweenDates(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/transaction/between/2024-01-01/2024-01-31")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/transaction/category/{category_id} - Should return a list of transactions from a category")
    void getTransactionsFromCategory_success() throws Exception {
        TransactionResponseDto transaction = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionService.getAllTransactionsFromCategory(1L, 1L)).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/transaction/category/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Food"));
    }

    @Test
    @DisplayName("GET /api/transaction/category/{category_id} - Should return empty list when category has no transactions")
    void getTransactionsFromCategory_emptyList() throws Exception {
        when(transactionService.getAllTransactionsFromCategory(1L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/transaction/category/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/transaction - Should create a new transaction")
    void createTransaction_success() throws Exception {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("100.00"), "Groceries", LocalDate.of(2024, 1, 15), 1L);
        TransactionResponseDto response = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionService.createTransaction(any(TransactionRequestDto.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/api/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Groceries"))
                .andExpect(jsonPath("$.categoryType").value("EXPENSE"));
    }

    @Test
    @DisplayName("POST /api/transaction - Should return not found when category does not exist")
    void createTransaction_categoryNotFound() throws Exception {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("100.00"), "Groceries", LocalDate.of(2024, 1, 15), 999L);

        when(transactionService.createTransaction(any(TransactionRequestDto.class), anyLong())).thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(post("/api/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/transaction/{transaction_id} - Should update a transaction")
    void updateTransaction_success() throws Exception {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("150.00"), "Updated Groceries", LocalDate.of(2024, 1, 20), 1L);
        TransactionResponseDto response = new TransactionResponseDto(1L, new BigDecimal("150.00"), "Updated Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 20));

        when(transactionService.updateTransaction(anyLong(), any(TransactionRequestDto.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/transaction/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.description").value("Updated Groceries"));
    }

    @Test
    @DisplayName("PUT /api/transaction/{transaction_id} - Should return not found when transaction does not exist")
    void updateTransaction_notFound() throws Exception {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("150.00"), "Updated Groceries", LocalDate.of(2024, 1, 20), 1L);

        when(transactionService.updateTransaction(anyLong(), any(TransactionRequestDto.class), anyLong())).thenThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(put("/api/transaction/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/transaction/{transaction_id} - Should delete a transaction")
    void deleteTransaction_success() throws Exception {
        doNothing().when(transactionService).deleteTransaction(anyLong(), anyLong());

        mockMvc.perform(delete("/api/transaction/1")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/transaction/{transaction_id} - Should return not found when transaction does not exist")
    void deleteTransaction_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Transaction not found")).when(transactionService).deleteTransaction(anyLong(), anyLong());

        mockMvc.perform(delete("/api/transaction/1")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }
}
