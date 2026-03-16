package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/{transaction_id}")
    public TransactionResponseDto getTransaction(@PathVariable("transaction_id") Long transactionId, @AuthenticationPrincipal User user) {
        return transactionService.getTransaction(transactionId, user.getId());
    }

    @GetMapping("/category/{category_id}")
    public List<TransactionResponseDto> getTransactionsFromCategory(@PathVariable("category_id") Long idCategory, @AuthenticationPrincipal User user) {
        return transactionService.getAllTransactionsFromCategory(idCategory, user.getId());
    }

    @PostMapping
    public TransactionResponseDto createTransaction(@Valid @RequestBody TransactionRequestDto transactionRequestDto, @AuthenticationPrincipal User user) {
        return transactionService.createTransaction(transactionRequestDto, user.getId());
    }

    @PutMapping("/{transaction_id}")
    public TransactionResponseDto updateTransaction(@PathVariable("transaction_id") Long idTransaction, @Valid @RequestBody TransactionRequestDto transactionRequestDto, @AuthenticationPrincipal User user) {
        return transactionService.updateTransaction(idTransaction, transactionRequestDto, user.getId());
    }

    @DeleteMapping("/{transaction_id}")
    public void deleteTransaction(@PathVariable("transaction_id") Long transactionId, @AuthenticationPrincipal User user) {
        transactionService.deleteTransaction(transactionId, user.getId());
    }
}
