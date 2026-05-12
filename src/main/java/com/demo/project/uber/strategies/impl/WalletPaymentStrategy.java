package com.demo.project.uber.strategies.impl;

import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Payment;
import com.demo.project.uber.entities.Rider;
import com.demo.project.uber.entities.enums.PaymentStatus;
import com.demo.project.uber.entities.enums.TransactionMethod;
import com.demo.project.uber.repositories.PaymentRepository;
import com.demo.project.uber.services.WalletService;
import com.demo.project.uber.strategies.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletPaymentStrategy implements PaymentStrategy {

    private final WalletService walletService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void processPayment(Payment payment) {

        Driver driver = payment.getRide().getDriver();
        Rider rider = payment.getRide().getRider();

        walletService.deductMoneyFromWallet(rider.getUser(), payment.getAmount(),
                null, payment.getRide(), TransactionMethod.RIDE);

        double driverCut = payment.getAmount() * (1 - PLATFORM_COMMISSION);

        walletService.addMoneyToWallet(driver.getUser(), driverCut, null,
                payment.getRide(), TransactionMethod.RIDE);

        payment.setPaymentStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);

    }
}
