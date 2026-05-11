package com.demo.project.uber.strategies.impl;

import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.services.DistanceService;
import com.demo.project.uber.strategies.RideFareCalculationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideFareDefaultFareCalculationStrategy implements RideFareCalculationStrategy {

    private final DistanceService distanceService;

    @Override
    public double calculateFare(RideRequest rideRequest) {

        double distance = distanceService
                .calculateDistance(
                        rideRequest.getPickupLocation(),
                        rideRequest.getDestinationLocation()
                );

        double calculatedFare =
                BASE_FARE + (distance * RIDE_FARE_MULTIPLIER);

        return Math.max(calculatedFare, MINIMUM_FARE);
    }
}
