package com.demo.project.uber.services;

import com.demo.project.uber.entities.Payment;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.enums.PaymentStatus;

public interface PaymentService {

    void processPayment(Ride ride);

    Payment createNewPayment(Ride ride);

    void updatePaymentStatus(Payment payment, PaymentStatus paymentStatus);
}
