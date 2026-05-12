package com.demo.project.uber.services.impl;

import com.demo.project.uber.entities.WalletTransaction;
import com.demo.project.uber.repositories.WalletTransactionRepository;
import com.demo.project.uber.services.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public void createNewWalletTransaction(WalletTransaction walletTransaction) {

        walletTransactionRepository.save(walletTransaction);
    }
}
