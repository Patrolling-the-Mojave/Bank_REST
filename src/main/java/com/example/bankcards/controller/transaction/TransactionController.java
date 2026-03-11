package com.example.bankcards.controller.transaction;

import com.example.bankcards.dto.transaction.TransactionDto;
import com.example.bankcards.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/admin/transactions")
@RestController
@Tag(name = "Admin - Transactions", description = "API для управления переводами (только ADMIN)")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{transactionId}")
    public TransactionDto findById(@PathVariable UUID transactionId) {
        return transactionService.findById(transactionId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<TransactionDto> findAll(@Parameter(description = "Номер страницы (от 0)", example = "0")
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                        @Parameter(description = "Размер страницы", example = "10")
                                        @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        return transactionService.findAll(pageable);
    }
}
