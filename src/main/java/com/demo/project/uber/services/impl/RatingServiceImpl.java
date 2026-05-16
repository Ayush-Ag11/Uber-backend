package com.demo.project.uber.services.impl;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.RiderDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Rating;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.Rider;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.DriverRepository;
import com.demo.project.uber.repositories.RatingRepository;
import com.demo.project.uber.repositories.RiderRepository;
import com.demo.project.uber.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DriverDto rateDriver(Ride ride, Integer rating) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Driver driver = ride.getDriver();
        Rating ratingObj = ratingRepository.findByRide(ride)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for ride with id " + ride.getId()));

        if (ratingObj.getDriverRating() != null) {
            throw new RuntimeException("Driver already rated");
        }

        ratingObj.setDriverRating(rating);

        ratingRepository.save(ratingObj);

        Double newRating = ratingRepository.getAverageDriverRating(driver);

        driver.setRating(newRating);
        Driver savedDriver = driverRepository.save(driver);
        return modelMapper.map(savedDriver, DriverDto.class);
    }

    @Override
    @Transactional
    public RiderDto rateRider(Ride ride, Integer rating) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Rider rider = ride.getRider();
        Rating ratingObj = ratingRepository.findByRide(ride)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for ride with id " + ride.getId()));

        if (ratingObj.getRiderRating() != null) {
            throw new RuntimeException("Rider already rated");
        }

        ratingObj.setRiderRating(rating);

        ratingRepository.save(ratingObj);

        Double newRating = ratingRepository.getAverageRiderRating(rider);

        rider.setRating(newRating);
        Rider savedRider = riderRepository.save(rider);
        return modelMapper.map(savedRider, RiderDto.class);
    }

    @Override
    public void createNewRating(Ride ride) {
        Rating rating = Rating.builder()
                .rider(ride.getRider())
                .driver(ride.getDriver())
                .ride(ride)
                .build();

        ratingRepository.save(rating);
    }
}
