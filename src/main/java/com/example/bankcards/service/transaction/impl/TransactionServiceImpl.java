package com.example.bankcards.service.transaction.impl;

import com.example.bankcards.dto.transaction.TransactionDto;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public Page<TransactionDto> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    public TransactionDto findById(UUID id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() ->
                new NotFoundException("транзакция с id" + id + " не найдена"));
        return mapToDto(transaction);
    }

    private TransactionDto mapToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .fromCard(transaction.getFromCard().getId())
                .toCard(transaction.getToCard().getId())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
