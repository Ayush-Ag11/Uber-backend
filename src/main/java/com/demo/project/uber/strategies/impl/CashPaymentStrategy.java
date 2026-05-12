package com.demo.project.uber.strategies.impl;

import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Payment;
import com.demo.project.uber.entities.enums.PaymentStatus;
import com.demo.project.uber.entities.enums.TransactionMethod;
import com.demo.project.uber.repositories.PaymentRepository;
import com.demo.project.uber.services.WalletService;
import com.demo.project.uber.strategies.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CashPaymentStrategy implements PaymentStrategy {

    private final WalletService  walletService;
    private final PaymentRepository paymentRepository;
    @Override
    public void processPayment(Payment payment) {
        Driver driver = payment.getRide().getDriver();

        double platformCommission = payment.getAmount() * PLATFORM_COMMISSION;
        walletService.deductMoneyFromWallet(driver.getUser(), platformCommission, null,
                payment.getRide(), TransactionMethod.RIDE);

        payment.setPaymentStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);
    }
}
