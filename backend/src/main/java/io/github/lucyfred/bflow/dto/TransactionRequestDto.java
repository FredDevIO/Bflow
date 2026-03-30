package io.github.lucyfred.bflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDto(
        @NotNull(message = "Amount cannot be empty")
        BigDecimal amount,

        @Size(min = 1, max = 150, message = "Description cannot exceed 150 characters")
        @NotBlank(message = "Description cannot be empty")
        String description,

        @NotNull
        LocalDate transactionDate,

        @NotNull
        Long categoryId
) {
}
