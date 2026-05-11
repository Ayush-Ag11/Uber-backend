package com.demo.project.uber.services;

import com.demo.project.uber.entities.RideRequest;

public interface RideRequestService {

    RideRequest getRideRequestById(Long rideRequestId);

    void update(RideRequest rideRequest);
}
