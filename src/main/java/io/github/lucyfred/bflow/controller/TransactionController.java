package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction operations endpoint")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/{transaction_id}")
    @Operation(summary = "Get user transaction", description = "Retrieve a user's transaction")
    public TransactionResponseDto getTransaction(@PathVariable("transaction_id") Long transactionId, @AuthenticationPrincipal User user) {
        return transactionService.getTransaction(transactionId, user.getId());
    }

    @GetMapping("/category/{category_id}")
    @Operation(summary = "Get user transactions category", description = "Retrieve a user's transactions from a category")
    public List<TransactionResponseDto> getTransactionsFromCategory(@PathVariable("category_id") Long idCategory, @AuthenticationPrincipal User user) {
        return transactionService.getAllTransactionsFromCategory(idCategory, user.getId());
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Create a transaction for the user")
    public TransactionResponseDto createTransaction(@Valid @RequestBody TransactionRequestDto transactionRequestDto, @AuthenticationPrincipal User user) {
        return transactionService.createTransaction(transactionRequestDto, user.getId());
    }

    @PutMapping("/{transaction_id}")
    @Operation(summary = "Update a transaction", description = "Updates a user's transaction")
    public TransactionResponseDto updateTransaction(@PathVariable("transaction_id") Long idTransaction, @Valid @RequestBody TransactionRequestDto transactionRequestDto, @AuthenticationPrincipal User user) {
        return transactionService.updateTransaction(idTransaction, transactionRequestDto, user.getId());
    }

    @DeleteMapping("/{transaction_id}")
    @Operation(summary = "Delete a transaction", description = "Delete a user's transaction")
    public void deleteTransaction(@PathVariable("transaction_id") Long transactionId, @AuthenticationPrincipal User user) {
        transactionService.deleteTransaction(transactionId, user.getId());
    }
}
