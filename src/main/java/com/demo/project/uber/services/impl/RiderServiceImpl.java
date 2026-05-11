package com.demo.project.uber.services.impl;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.RideDto;
import com.demo.project.uber.dto.RideRequestDto;
import com.demo.project.uber.dto.RiderDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.entities.Rider;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.enums.RideRequestStatus;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.RideRequestRepository;
import com.demo.project.uber.repositories.RiderRepository;
import com.demo.project.uber.services.RiderService;
import com.demo.project.uber.strategies.RideStrategyManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderServiceImpl implements RiderService {

    private final ModelMapper modelMapper;
    private final RideStrategyManager rideStrategyManager;
    private final RideRequestRepository rideRequestRepository;
    private final RiderRepository riderRepository;

    @Override
    @Transactional
    public RideRequestDto requestRide(RideRequestDto rideRequestDto) {
        Rider rider = getCurrentRider();
        RideRequest rideRequest = modelMapper.map(rideRequestDto, RideRequest.class);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setRider(rider);

        Double fare = rideStrategyManager.rideFareStrategy().calculateFare(rideRequest);
        rideRequest.setFare(fare);

        RideRequest SavedRideRequest = rideRequestRepository.save(rideRequest);

        List<Driver> drivers = rideStrategyManager.driverMatchingStrategy(rider.getRating())
                .findMatchingDrivers(rideRequest);

        return modelMapper.map(SavedRideRequest, RideRequestDto.class);
    }

    @Override
    public RideDto cancelRide(Long rideId) {
        return null;
    }

    @Override
    public DriverDto rateDriver(Long rideId, Integer rating) {
        return null;
    }

    @Override
    public RiderDto getMyProfile() {
        return null;
    }

    @Override
    public List<RideDto> getAllMyRides() {
        return List.of();
    }

    @Override
    public Rider createNewRider(User user) {
        Rider rider = Rider.builder()
                .user(user)
                .rating(0.0)
                .build();
        return riderRepository.save(rider);
    }

    @Override
    public Rider getCurrentRider() {

        return riderRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
    }
}
