package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    TransactionResponseDto getTransaction(Long transactionId, Long userId);
    Page<TransactionResponseDto> getAllTransactions(Long userId, Pageable pageable);
    List<TransactionResponseDto> getAllTransactionsType(Long userId, CategoryTypes categoryType);
    List<TransactionResponseDto> getAllTransactionsFromCategory(Long categoryId, Long userId);
    List<TransactionResponseDto> getTop4Transactions(Long userId);
    List<TransactionResponseDto> getAllTransactionsBetweenDates(Long userId, LocalDate startDate, LocalDate endDate);
    TransactionResponseDto createTransaction(TransactionRequestDto transactionRequestDto, Long userId);
    TransactionResponseDto updateTransaction(Long transactionId, TransactionRequestDto transactionRequestDto, Long userId);
    void deleteTransaction(Long transactionId, Long userId);
}
