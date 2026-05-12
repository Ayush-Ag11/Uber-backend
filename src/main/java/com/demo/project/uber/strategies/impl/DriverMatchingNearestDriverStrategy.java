package com.demo.project.uber.strategies.impl;

import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.repositories.DriverRepository;
import com.demo.project.uber.strategies.DriverMatchingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverMatchingNearestDriverStrategy implements DriverMatchingStrategy {

    private final DriverRepository driverRepository;

    @Override
    public List<Driver> findMatchingDrivers(RideRequest rideRequest) {
        return driverRepository.find10NearestDrivers(rideRequest.getPickupLocation());
    }
}
