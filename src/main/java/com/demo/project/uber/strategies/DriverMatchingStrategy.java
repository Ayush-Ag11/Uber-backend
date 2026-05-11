package com.demo.project.uber.strategies;

import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.RideRequest;

import java.util.List;

public interface DriverMatchingStrategy {

    List<Driver> findMatchingDrivers(RideRequest rideRequest);
}
