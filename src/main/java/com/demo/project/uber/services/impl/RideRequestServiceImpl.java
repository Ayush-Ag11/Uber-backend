package com.demo.project.uber.services.impl;

import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.RideRequestRepository;
import com.demo.project.uber.services.RideRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideRequestServiceImpl implements RideRequestService {

    private final RideRequestRepository rideRequestRepository;

    @Override
    public RideRequest getRideRequestById(Long rideRequestId) {
        return rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("RideRequest not found with id: " + rideRequestId));
    }

    @Override
    public void update(RideRequest rideRequest) {
        rideRequestRepository.findById(rideRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RideRequest not found with id : " + rideRequest.getId()));
        rideRequestRepository.save(rideRequest);
    }
}
