package com.demo.project.uber.dto;

import com.demo.project.uber.entities.enums.PaymentMethod;
import com.demo.project.uber.entities.enums.RideRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDto {

    private Long id;

    @NotNull(message = "Pickup location is required")
    private PointDto pickupLocation;

    @NotNull(message = "Destination location is required")
    private PointDto destinationLocation;

    private LocalDateTime requestedTime;

    private RiderDto rider;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private RideRequestStatus rideRequestStatus;

    private Double fare;
}
