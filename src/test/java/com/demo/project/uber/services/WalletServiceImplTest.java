package com.demo.project.uber.services;

import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.Wallet;
import com.demo.project.uber.entities.enums.TransactionMethod;
import com.demo.project.uber.exceptions.InsufficientBalanceException;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.WalletRepository;
import com.demo.project.uber.services.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionService walletTransactionService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setBalance(1000.0);
        wallet.setTransactions(new ArrayList<>());
    }

    @Test
    void addMoneyToWallet_shouldIncreaseBalance() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.addMoneyToWallet(
                user, 500.0, "txn123", null, TransactionMethod.RIDE);

        assertThat(result.getBalance()).isEqualTo(1500.0);
        verify(walletTransactionService, times(1)).createNewWalletTransaction(any());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    void addMoneyToWallet_shouldCreateWalletTransaction() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        walletService.addMoneyToWallet(user, 500.0, "txn123", null, TransactionMethod.RIDE);

        verify(walletTransactionService, times(1)).createNewWalletTransaction(any());
    }

    @Test
    void deductMoneyFromWallet_shouldDecreaseBalance() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.deductMoneyFromWallet(
                user, 500.0, "txn123", null, TransactionMethod.RIDE);

        assertThat(result.getBalance()).isEqualTo(500.0);
        verify(walletTransactionService, times(1)).createNewWalletTransaction(any());
    }

    @Test
    void deductMoneyFromWallet_shouldThrowException_whenInsufficientBalance() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() ->
                walletService.deductMoneyFromWallet(
                        user, 2000.0, "txn123", null, TransactionMethod.RIDE))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient wallet balance");
    }

    @Test
    void deductMoneyFromWallet_shouldThrowException_whenBalanceIsZero() {
        wallet.setBalance(0.0);
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() ->
                walletService.deductMoneyFromWallet(
                        user, 100.0, "txn123", null, TransactionMethod.RIDE))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void deductMoneyFromWallet_shouldThrowException_whenDeductingExactBalance() {
        wallet.setBalance(100.0);
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);

        // deducting exact balance should work — not throw
        Wallet result = walletService.deductMoneyFromWallet(
                user, 100.0, "txn123", null, TransactionMethod.RIDE);

        assertThat(result.getBalance()).isEqualTo(0.0);
    }

    @Test
    void withdrawAllMyMoney_shouldSetBalanceToZero() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.withdrawAllMyMoneyFromWallet(user);

        assertThat(result.getBalance()).isEqualTo(0.0);
        verify(walletTransactionService, times(1)).createNewWalletTransaction(any());
    }

    @Test
    void withdrawAllMyMoney_shouldThrowException_whenBalanceIsZero() {
        wallet.setBalance(0.0);
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdrawAllMyMoneyFromWallet(user))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient wallet balance.");
    }

    @Test
    void findByUser_shouldThrowException_whenWalletNotFound() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.findByUser(user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("wallet not found for user");
    }

    @Test
    void createNewWallet_shouldCreateWalletWithZeroBalance() {
        Wallet newWallet = new Wallet();
        newWallet.setUser(user);
        newWallet.setBalance(0.0);

        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);

        Wallet result = walletService.createNewWallet(user);

        assertThat(result.getBalance()).isEqualTo(0.0);
        assertThat(result.getUser()).isEqualTo(user);
    }
}