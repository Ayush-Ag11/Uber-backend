package com.demo.project.uber.services;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.RiderDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Rating;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.Rider;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.DriverRepository;
import com.demo.project.uber.repositories.RatingRepository;
import com.demo.project.uber.repositories.RiderRepository;
import com.demo.project.uber.services.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Ride ride;
    private Driver driver;
    private Rider rider;
    private Rating rating;

    @BeforeEach
    void setUp() {
        User driverUser = new User();
        driverUser.setId(1L);
        driverUser.setEmail("driver@test.com");

        User riderUser = new User();
        riderUser.setId(2L);
        riderUser.setEmail("rider@test.com");

        driver = new Driver();
        driver.setId(1L);
        driver.setUser(driverUser);
        driver.setRating(4.0);

        rider = new Rider();
        rider.setId(1L);
        rider.setUser(riderUser);
        rider.setRating(4.0);

        ride = new Ride();
        ride.setId(1L);
        ride.setDriver(driver);
        ride.setRider(rider);

        rating = Rating.builder()
                .id(1L)
                .ride(ride)
                .driver(driver)
                .rider(rider)
                .build();
    }

    @Test
    void rateDriver_shouldSetDriverRatingSuccessfully() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(ratingRepository.getAverageDriverRating(driver)).thenReturn(4.5);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(any(Driver.class), eq(DriverDto.class))).thenReturn(new DriverDto());

        DriverDto result = ratingService.rateDriver(ride, 5);

        assertThat(rating.getDriverRating()).isEqualTo(5);
        verify(ratingRepository, times(1)).save(rating);
        verify(driverRepository, times(1)).save(driver);
    }

    @Test
    void rateDriver_shouldUpdateDriverAverageRating() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(ratingRepository.getAverageDriverRating(driver)).thenReturn(4.5);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(any(Driver.class), eq(DriverDto.class))).thenReturn(new DriverDto());

        ratingService.rateDriver(ride, 5);

        assertThat(driver.getRating()).isEqualTo(4.5);
    }

    @Test
    void rateDriver_shouldThrowException_whenRatingNotFound() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.rateDriver(ride, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rating not found for ride");
    }

    @Test
    void rateDriver_shouldThrowException_whenDriverAlreadyRated() {
        rating.setDriverRating(4); // already rated
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));

        assertThatThrownBy(() -> ratingService.rateDriver(ride, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver already rated");
    }

    @Test
    void rateDriver_shouldThrowException_whenRatingIsNull() {
        assertThatThrownBy(() -> ratingService.rateDriver(ride, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void rateDriver_shouldThrowException_whenRatingBelowOne() {
        assertThatThrownBy(() -> ratingService.rateDriver(ride, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void rateDriver_shouldThrowException_whenRatingAboveFive() {
        assertThatThrownBy(() -> ratingService.rateDriver(ride, 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void rateRider_shouldSetRiderRatingSuccessfully() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(ratingRepository.getAverageRiderRating(rider)).thenReturn(4.2);
        when(riderRepository.save(any(Rider.class))).thenReturn(rider);
        when(modelMapper.map(any(Rider.class), eq(RiderDto.class))).thenReturn(new RiderDto());

        RiderDto result = ratingService.rateRider(ride, 4);

        assertThat(rating.getRiderRating()).isEqualTo(4);
        verify(ratingRepository, times(1)).save(rating);
        verify(riderRepository, times(1)).save(rider);
    }

    @Test
    void rateRider_shouldUpdateRiderAverageRating() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));
        when(ratingRepository.getAverageRiderRating(rider)).thenReturn(4.2);
        when(riderRepository.save(any(Rider.class))).thenReturn(rider);
        when(modelMapper.map(any(Rider.class), eq(RiderDto.class))).thenReturn(new RiderDto());

        ratingService.rateRider(ride, 4);

        assertThat(rider.getRating()).isEqualTo(4.2);
    }

    @Test
    void rateRider_shouldThrowException_whenRiderAlreadyRated() {
        rating.setRiderRating(3); // already rated
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating));

        assertThatThrownBy(() -> ratingService.rateRider(ride, 4))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rider already rated");
    }

    @Test
    void rateRider_shouldThrowException_whenRatingNotFound() {
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.rateRider(ride, 4))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rating not found for ride");
    }

    @Test
    void createNewRating_shouldCreateRatingWithNullRatings() {
        ratingService.createNewRating(ride);

        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    void createNewRating_shouldAssociateCorrectRideDriverRider() {
        ratingService.createNewRating(ride);

        verify(ratingRepository).save(argThat(savedRating ->
                savedRating.getRide().equals(ride) &&
                        savedRating.getDriver().equals(driver) &&
                        savedRating.getRider().equals(rider)
        ));
    }
}