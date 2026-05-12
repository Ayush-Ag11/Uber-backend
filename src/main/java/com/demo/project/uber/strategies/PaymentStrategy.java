package com.demo.project.uber.strategies;

import com.demo.project.uber.entities.Payment;

public interface PaymentStrategy {

    static final double PLATFORM_COMMISSION = 0.3;

    void processPayment(Payment payment);
}
