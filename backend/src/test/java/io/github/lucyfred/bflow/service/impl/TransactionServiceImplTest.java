package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.Category;
import io.github.lucyfred.bflow.entity.Transaction;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.mapper.TransactionMapper;
import io.github.lucyfred.bflow.repository.CategoryRepository;
import io.github.lucyfred.bflow.repository.TransanctionRepository;
import io.github.lucyfred.bflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private TransanctionRepository transanctionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User mockUser;
    private Category mockCategory;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockCategory = Category.builder()
                .id(1L)
                .name("Food")
                .type(CategoryTypes.EXPENSE)
                .color("#FF0000")
                .user(mockUser)
                .build();

        mockTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Groceries")
                .transactionDate(LocalDate.of(2024, 1, 15))
                .category(mockCategory)
                .user(mockUser)
                .build();
    }

    @Test
    @DisplayName("Should get transaction by id successfully")
    void getTransaction_success() {
        TransactionResponseDto expected = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockTransaction));
        when(transactionMapper.toTransactionResponseDto(mockTransaction)).thenReturn(expected);

        TransactionResponseDto result = transactionService.getTransaction(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Groceries", result.description());
        verify(transanctionRepository, times(1)).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction not found")
    void getTransaction_notFound() {
        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransaction(1L, 1L);
        });

        verify(transanctionRepository, times(1)).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("Should get all user transactions successfully")
    void getAllTransactions_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(mockTransaction));
        TransactionResponseDto responseDto = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transanctionRepository.getAllByUserIdOrderByTransactionDateDesc(1L, pageable)).thenReturn(transactionPage);
        when(transactionMapper.toTransactionResponseDto(mockTransaction)).thenReturn(responseDto);

        Page<TransactionResponseDto> result = transactionService.getAllTransactions(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto, result.getContent().get(0));
        verify(transanctionRepository, times(1)).getAllByUserIdOrderByTransactionDateDesc(1L, pageable);
        verify(transactionMapper, times(1)).toTransactionResponseDto(mockTransaction);
    }

    @Test
    @DisplayName("Should return empty page when no transactions exist")
    void getAllTransactions_emptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        when(transanctionRepository.getAllByUserIdOrderByTransactionDateDesc(1L, pageable)).thenReturn(emptyPage);

        Page<TransactionResponseDto> result = transactionService.getAllTransactions(1L, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("Should get all transactions by type successfully")
    void getAllTransactionsType_success() {
        List<Transaction> transactions = List.of(mockTransaction);
        TransactionResponseDto responseDto = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));
        List<TransactionResponseDto> expected = List.of(responseDto);

        when(transanctionRepository.getAllByUserIdAndCategoryType(1L, CategoryTypes.EXPENSE)).thenReturn(transactions);
        when(transactionMapper.toListTransactionResponseDto(transactions)).thenReturn(expected);

        List<TransactionResponseDto> result = transactionService.getAllTransactionsType(1L, CategoryTypes.EXPENSE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CategoryTypes.EXPENSE, result.get(0).categoryType());
        verify(transanctionRepository, times(1)).getAllByUserIdAndCategoryType(1L, CategoryTypes.EXPENSE);
    }

    @Test
    @DisplayName("Should return empty list when no transactions of type exist")
    void getAllTransactionsType_emptyList() {
        when(transanctionRepository.getAllByUserIdAndCategoryType(1L, CategoryTypes.INCOME)).thenReturn(List.of());
        when(transactionMapper.toListTransactionResponseDto(List.of())).thenReturn(List.of());

        List<TransactionResponseDto> result = transactionService.getAllTransactionsType(1L, CategoryTypes.INCOME);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get all transactions from category successfully")
    void getAllTransactionsFromCategory_success() {
        List<Transaction> transactions = List.of(mockTransaction);
        TransactionResponseDto responseDto = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));
        List<TransactionResponseDto> expected = List.of(responseDto);

        when(transanctionRepository.getAllByUserIdAndCategoryId(1L, 1L)).thenReturn(transactions);
        when(transactionMapper.toListTransactionResponseDto(transactions)).thenReturn(expected);

        List<TransactionResponseDto> result = transactionService.getAllTransactionsFromCategory(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transanctionRepository, times(1)).getAllByUserIdAndCategoryId(1L, 1L);
    }

    @Test
    @DisplayName("Should return empty list when category has no transactions")
    void getAllTransactionsFromCategory_emptyList() {
        when(transanctionRepository.getAllByUserIdAndCategoryId(1L, 1L)).thenReturn(List.of());
        when(transactionMapper.toListTransactionResponseDto(List.of())).thenReturn(List.of());

        List<TransactionResponseDto> result = transactionService.getAllTransactionsFromCategory(1L, 1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get top 5 transactions successfully")
    void getTop4Transactions_success() {
        List<Transaction> transactions = List.of(mockTransaction);
        TransactionResponseDto responseDto = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));
        List<TransactionResponseDto> expected = List.of(responseDto);

        when(transanctionRepository.findTop5ByUserIdOrderByTransactionDateAsc(1L)).thenReturn(transactions);
        when(transactionMapper.toListTransactionResponseDto(transactions)).thenReturn(expected);

        List<TransactionResponseDto> result = transactionService.getTop4Transactions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transanctionRepository, times(1)).findTop5ByUserIdOrderByTransactionDateAsc(1L);
    }

    @Test
    @DisplayName("Should return empty list when no transactions exist for top 5")
    void getTop4Transactions_emptyList() {
        when(transanctionRepository.findTop5ByUserIdOrderByTransactionDateAsc(1L)).thenReturn(List.of());
        when(transactionMapper.toListTransactionResponseDto(List.of())).thenReturn(List.of());

        List<TransactionResponseDto> result = transactionService.getTop4Transactions(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get all transactions between dates successfully")
    void getAllTransactionsBetweenDates_success() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        List<Transaction> transactions = List.of(mockTransaction);
        TransactionResponseDto responseDto = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));
        List<TransactionResponseDto> expected = List.of(responseDto);

        when(transanctionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1L, start, end)).thenReturn(transactions);
        when(transactionMapper.toListTransactionResponseDto(transactions)).thenReturn(expected);

        List<TransactionResponseDto> result = transactionService.getAllTransactionsBetweenDates(1L, start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transanctionRepository, times(1)).findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1L, start, end);
    }

    @Test
    @DisplayName("Should return empty list when no transactions in date range")
    void getAllTransactionsBetweenDates_emptyList() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(transanctionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1L, start, end)).thenReturn(List.of());
        when(transactionMapper.toListTransactionResponseDto(List.of())).thenReturn(List.of());

        List<TransactionResponseDto> result = transactionService.getAllTransactionsBetweenDates(1L, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void createTransaction_success() {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("100.00"), "Groceries", LocalDate.of(2024, 1, 15), 1L);
        Transaction transactionFromRequest = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .description("Groceries")
                .transactionDate(LocalDate.of(2024, 1, 15))
                .build();
        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Groceries")
                .transactionDate(LocalDate.of(2024, 1, 15))
                .category(mockCategory)
                .user(mockUser)
                .build();
        TransactionResponseDto expected = new TransactionResponseDto(1L, new BigDecimal("100.00"), "Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 15));

        when(transactionMapper.toTransactionFromRequest(request)).thenReturn(transactionFromRequest);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockCategory));
        when(transanctionRepository.save(transactionFromRequest)).thenReturn(savedTransaction);
        when(transactionMapper.toTransactionResponseDto(savedTransaction)).thenReturn(expected);

        TransactionResponseDto result = transactionService.createTransaction(request, 1L);

        assertNotNull(result);
        assertEquals("Groceries", result.description());
        verify(userRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, times(1)).save(transactionFromRequest);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found during transaction creation")
    void createTransaction_userNotFound() {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("100.00"), "Groceries", LocalDate.of(2024, 1, 15), 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(request, 1L);
        });

        verify(userRepository, times(1)).findById(1L);
        verify(transanctionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found during transaction creation")
    void createTransaction_categoryNotFound() {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("100.00"), "Groceries", LocalDate.of(2024, 1, 15), 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(request, 1L);
        });

        verify(categoryRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void updateTransaction_success() {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("150.00"), "Updated Groceries", LocalDate.of(2024, 1, 20), 1L);
        Transaction existingTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Groceries")
                .transactionDate(LocalDate.of(2024, 1, 15))
                .category(mockCategory)
                .user(mockUser)
                .build();
        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("150.00"))
                .description("Updated Groceries")
                .transactionDate(LocalDate.of(2024, 1, 20))
                .category(mockCategory)
                .user(mockUser)
                .build();
        TransactionResponseDto expected = new TransactionResponseDto(1L, new BigDecimal("150.00"), "Updated Groceries", CategoryTypes.EXPENSE, "Food", LocalDate.of(2024, 1, 20));

        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockCategory));
        when(transanctionRepository.save(existingTransaction)).thenReturn(savedTransaction);
        when(transactionMapper.toTransactionResponseDto(savedTransaction)).thenReturn(expected);

        TransactionResponseDto result = transactionService.updateTransaction(1L, request, 1L);

        assertNotNull(result);
        assertEquals("Updated Groceries", result.description());
        assertEquals(new BigDecimal("150.00"), result.amount());
        verify(transanctionRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(categoryRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, times(1)).save(existingTransaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction not found during update")
    void updateTransaction_notFound() {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("150.00"), "Updated Groceries", LocalDate.of(2024, 1, 20), 1L);

        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.updateTransaction(1L, request, 1L);
        });

        verify(transanctionRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found during update")
    void updateTransaction_categoryNotFound() {
        TransactionRequestDto request = new TransactionRequestDto(new BigDecimal("150.00"), "Updated Groceries", LocalDate.of(2024, 1, 20), 1L);

        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockTransaction));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.updateTransaction(1L, request, 1L);
        });

        verify(categoryRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void deleteTransaction_success() {
        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockTransaction));

        transactionService.deleteTransaction(1L, 1L);

        verify(transanctionRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, times(1)).delete(mockTransaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction not found during delete")
    void deleteTransaction_notFound() {
        when(transanctionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.deleteTransaction(1L, 1L);
        });

        verify(transanctionRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(transanctionRepository, never()).delete(any(Transaction.class));
    }
}
