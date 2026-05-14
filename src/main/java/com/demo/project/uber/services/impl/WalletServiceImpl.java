package com.demo.project.uber.services.impl;

import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.Wallet;
import com.demo.project.uber.entities.WalletTransaction;
import com.demo.project.uber.entities.enums.TransactionMethod;
import com.demo.project.uber.entities.enums.TransactionType;
import com.demo.project.uber.exceptions.InsufficientBalanceException;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.WalletRepository;
import com.demo.project.uber.services.WalletService;
import com.demo.project.uber.services.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionService walletTransactionService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Wallet addMoneyToWallet(User user, Double amount, String transactionId, Ride ride, TransactionMethod transactionMethod) {
        Wallet wallet = findByUser(user);
        wallet.setBalance(wallet.getBalance() + amount);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .transactionId(transactionId)
                .ride(ride)
                .wallet(wallet)
                .transactionType(TransactionType.CREDIT)
                .transactionMethod(transactionMethod)
                .amount(amount)
                .build();

        walletTransactionService.createNewWalletTransaction(walletTransaction);

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet deductMoneyFromWallet(User user, Double amount, String transactionId, Ride ride,
                                        TransactionMethod transactionMethod) {

        Wallet wallet = findByUser(user);

        if (wallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient wallet balance. Available: "
                    + wallet.getBalance() + ", Required: " + amount);
        }

        wallet.setBalance(wallet.getBalance() - amount);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .transactionId(transactionId)
                .ride(ride)
                .wallet(wallet)
                .transactionType(TransactionType.DEBIT)
                .transactionMethod(transactionMethod)
                .amount(amount)
                .build();

        walletTransactionService.createNewWalletTransaction(walletTransaction);

        //wallet.getTransactions().add(walletTransaction);


        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet withdrawAllMyMoneyFromWallet(User user) {

        Wallet wallet = findByUser(user);

        if (wallet.getBalance() <= 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance.");
        }

        double amountToWithdraw = wallet.getBalance();

        log.info("Driver with userId: {} initiating withdrawal of amount: {}",
                user.getId(), amountToWithdraw);


        WalletTransaction walletTransaction = WalletTransaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .ride(null)
                .wallet(wallet)
                .transactionType(TransactionType.DEBIT)
                .transactionMethod(TransactionMethod.BANKING)
                .amount(amountToWithdraw)
                .build();

        walletTransactionService.createNewWalletTransaction(walletTransaction);

        wallet.setBalance(0.0);

        // TODO: integrate with payment gateway (Razorpay/Stripe) to trigger actual bank transfer

        log.info("Withdrawal successful for userId: {}, amount: {}", user.getId(), amountToWithdraw);

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
    }

    @Override
    public Wallet createNewWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("wallet not found for user with id : " + user.getId()));
    }
}
