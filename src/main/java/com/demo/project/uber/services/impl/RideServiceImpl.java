package com.demo.project.uber.services.impl;

import com.demo.project.uber.dto.RideRequestDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.entities.enums.RideRequestStatus;
import com.demo.project.uber.entities.enums.RideStatus;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.RideRepository;
import com.demo.project.uber.services.RideRequestService;
import com.demo.project.uber.services.RideService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final RideRequestService rideRequestService;
    private final ModelMapper modelMapper;

    @Override
    public Ride getRideById(long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride with id: " + rideId + " not found!"));
    }

    @Override
    public void matchWithDrivers(RideRequestDto rideRequestDto) {
    }

    @Override
    public Ride createNewRide(RideRequest rideRequest, Driver driver) {
        rideRequest.setRideRequestStatus(RideRequestStatus.CONFIRMED);

        Ride ride = modelMapper.map(rideRequest, Ride.class);

        ride.setRideStatus(RideStatus.CONFIRMED);
        ride.setDriver(driver);

        ride.setOtp(generateRandomOtp());

        ride.setId(null);

        rideRequestService.update(rideRequest);
        return rideRepository.save(ride);

    }

    @Override
    public Ride updateRideStatus(Ride ride, RideStatus rideStatus) {
        ride.setRideStatus(rideStatus);
        return rideRepository.save(ride);
    }

    @Override
    public Page<Ride> getAllRidesOfRider(Long riderId, PageRequest pageRequest) {
        return null;
    }

    @Override
    public Page<Ride> getAllRidesOfDriver(Long DriverId, PageRequest pageRequest) {
        return null;
    }

    private String generateRandomOtp(){
        Random  random = new Random();
        int otp = random.nextInt(10000);
        return String.format("%04d", otp);
    }
}
