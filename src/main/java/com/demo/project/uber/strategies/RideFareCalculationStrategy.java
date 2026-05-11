package com.demo.project.uber.strategies;

import com.demo.project.uber.entities.RideRequest;

public interface RideFareCalculationStrategy {

    double RIDE_FARE_MULTIPLIER = 10;
    double BASE_FARE = 50;
    double MINIMUM_FARE = 80;

    double calculateFare(RideRequest rideRequest);
}
