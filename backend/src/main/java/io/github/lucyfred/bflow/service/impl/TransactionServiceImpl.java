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
import io.github.lucyfred.bflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransanctionRepository transanctionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponseDto getTransaction(Long transactionId, Long userId) {
        Transaction transaction = transanctionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return transactionMapper.toTransactionResponseDto(transaction);
    }

    @Override
    public Page<TransactionResponseDto> getAllTransactions(Long userId, Pageable pageable) {
        Page<Transaction> transactions = transanctionRepository.getAllByUserIdOrderByTransactionDateDesc(userId, pageable);

        return transactions.map(transactionMapper::toTransactionResponseDto);
    }

    @Override
    public List<TransactionResponseDto> getAllTransactionsType(Long userId, CategoryTypes categoryType) {
        List<Transaction> transactions = transanctionRepository.getAllByUserIdAndCategoryType(userId, categoryType);

        return transactionMapper.toListTransactionResponseDto(transactions);
    }

    @Override
    public List<TransactionResponseDto> getAllTransactionsFromCategory(Long categoryId, Long userId) {
        List<Transaction> transactions = transanctionRepository.getAllByUserIdAndCategoryId(userId, categoryId);

        return transactionMapper.toListTransactionResponseDto(transactions);
    }

    @Override
    public List<TransactionResponseDto> getTop4Transactions(Long userId) {
        List<Transaction> transactions = transanctionRepository.findTop5ByUserIdOrderByTransactionDateAsc(userId);

        return transactionMapper.toListTransactionResponseDto(transactions);
    }

    @Override
    public List<TransactionResponseDto> getAllTransactionsBetweenDates(Long userId, LocalDate start, LocalDate end) {
        List<Transaction> transactions = transanctionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end);

        return  transactionMapper.toListTransactionResponseDto(transactions);
    }

    @Override
    public TransactionResponseDto createTransaction(TransactionRequestDto transactionRequestDto, Long userId) {
        Transaction transaction = transactionMapper.toTransactionFromRequest(transactionRequestDto);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = categoryRepository.findByIdAndUserId(transactionRequestDto.categoryId(), userId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction savedTransaction = transanctionRepository.save(transaction);

        return transactionMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    public TransactionResponseDto updateTransaction(Long transactionId, TransactionRequestDto transactionRequestDto, Long userId) {
        Transaction transaction = transanctionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Category category = categoryRepository.findByIdAndUserId(transactionRequestDto.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        transaction.setAmount(transactionRequestDto.amount());
        transaction.setDescription(transactionRequestDto.description());
        transaction.setTransactionDate(transactionRequestDto.transactionDate());
        transaction.setCategory(category);

        return transactionMapper.toTransactionResponseDto(transanctionRepository.save(transaction));
    }

    @Override
    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = transanctionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transanctionRepository.delete(transaction);
    }
}
