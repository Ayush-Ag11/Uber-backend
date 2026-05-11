package com.demo.project.uber.services;

import com.demo.project.uber.dto.RideRequestDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.entities.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface RideService {

    Ride getRideById(long rideId);

    void matchWithDrivers(RideRequestDto rideRequestDto);

    Ride createNewRide(RideRequest rideRequest, Driver  driver);

    Ride updateRideStatus(Ride ride, RideStatus  rideStatus);

    Page<Ride> getAllRidesOfRider(Long riderId, PageRequest pageRequest);

    Page<Ride> getAllRidesOfDriver(Long DriverId, PageRequest pageRequest);
}
