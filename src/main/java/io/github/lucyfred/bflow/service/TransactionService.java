package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import java.util.List;

public interface TransactionService {
    TransactionResponseDto getTransaction(Long transactionId, Long userId);
    List<TransactionResponseDto> getAllTransactionsFromCategory(Long categoryId, Long userId);
    TransactionResponseDto createTransaction(TransactionRequestDto transactionRequestDto, Long userId);
    TransactionResponseDto updateTransaction(Long transactionId, TransactionRequestDto transactionRequestDto, Long userId);
    void deleteTransaction(Long transactionId, Long userId);
}
