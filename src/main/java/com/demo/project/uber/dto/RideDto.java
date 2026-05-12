package com.demo.project.uber.dto;

import com.demo.project.uber.entities.enums.PaymentMethod;
import com.demo.project.uber.entities.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideDto {

    private PointDto pickupLocation;

    private PointDto destinationLocation;

    private LocalDateTime createdTime;

    private RiderDto riderDto;

    private DriverDto driver;

    private PaymentMethod paymentMethod;

    private RideStatus rideStatus;

    private String otp;

    private double fare;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
