package com.example.bankcards.service.transaction;

import com.example.bankcards.dto.transaction.TransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {
    TransactionDto findById(UUID id);

    Page<TransactionDto> findAll(Pageable pageable);
}
