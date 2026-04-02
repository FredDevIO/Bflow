package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.CategoryTypes;
import io.github.lucyfred.bflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction operations endpoint")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get user transactions", description = "Retrieve a user's transactions")
    public Page<TransactionResponseDto> getAllTransactions(@PageableDefault(size = 10, page = 0) Pageable pageable, @AuthenticationPrincipal User user) {
        return transactionService.getAllTransactions(user.getId(), pageable);
    }

    @GetMapping("/{transaction_id}")
    @Operation(summary = "Get user transaction by id", description = "Retrieve a user's transaction by id")
    public TransactionResponseDto getTransaction(@PathVariable("transaction_id") Long transactionId, @AuthenticationPrincipal User user) {
        return transactionService.getTransaction(transactionId, user.getId());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get user transactions type", description = "Retrieve a user's transactions from a type")
    public List<TransactionResponseDto> getTransactionsByType(@PathVariable("type") String type, @AuthenticationPrincipal User user) {
        return transactionService.getAllTransactionsType(user.getId(), CategoryTypes.valueOf(type.toUpperCase()));
    }

    @GetMapping("/top5")
    @Operation(summary = "Get user top 5 transactions", description = "Retrieve a user's top transactions")
    public  List<TransactionResponseDto> getTop4Transactions(@AuthenticationPrincipal User user) {
        return  transactionService.getTop4Transactions(user.getId());
    }

    @GetMapping("/between/{start_date}/{end_date}")
    @Operation(summary = "Get user transactions between dates", description = "Retrieve a user's transactions between dates")
    public List<TransactionResponseDto> getTransactionsBetweenDates(@PathVariable("start_date") String startDate, @PathVariable("end_date") String endDate, @AuthenticationPrincipal User user) {
        return transactionService.getAllTransactionsBetweenDates(user.getId(), LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @GetMapping("/category/{category_id}")
    @Operation(summary = "Get user transactions category", description = "Retrieve a user's transactions from a category")
    public List<TransactionResponseDto> getTransactionsFromCategory(@PathVariable("category_id") Long idCategory, @AuthenticationPrincipal User user) {
        return transactionService.getAllTransactionsFromCategory(idCategory, user.getId());
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Create a transaction for the user")
    public ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody TransactionRequestDto transactionRequestDto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(transactionRequestDto, user.getId()));
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
