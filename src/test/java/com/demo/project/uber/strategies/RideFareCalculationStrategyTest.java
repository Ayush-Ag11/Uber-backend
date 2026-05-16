package com.demo.project.uber.strategies;

import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.services.DistanceService;
import com.demo.project.uber.strategies.impl.RideFareDefaultFareCalculationStrategy;
import com.demo.project.uber.strategies.impl.RideFareSurgePricingFareStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideFareCalculationStrategyTest {

    @Mock
    private DistanceService distanceService;

    @InjectMocks
    private RideFareDefaultFareCalculationStrategy defaultFareStrategy;

    @InjectMocks
    private RideFareSurgePricingFareStrategy surgeFareStrategy;

    private RideRequest rideRequest;
    private Point pickup;
    private Point destination;

    @BeforeEach
    void setUp() {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        pickup = geometryFactory.createPoint(new Coordinate(77.0, 28.0));
        destination = geometryFactory.createPoint(new Coordinate(77.1, 28.1));

        rideRequest = new RideRequest();
        rideRequest.setPickupLocation(pickup);
        rideRequest.setDestinationLocation(destination);
    }

    @Test
    void calculateFare_shouldReturnBaseFarePlusDistanceMultiplier() {
        // distance = 10km
        // expected = BASE_FARE(50) + (10 * MULTIPLIER(10)) = 150
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(10.0);

        double fare = defaultFareStrategy.calculateFare(rideRequest);

        assertThat(fare).isEqualTo(150.0);
    }

    @Test
    void calculateFare_shouldReturnMinimumFare_whenCalculatedFareIsBelowMinimum() {
        // distance = 1km
        // calculated = 50 + (1 * 10) = 60 — below MINIMUM_FARE(80)
        // expected = 80 (minimum fare)
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(1.0);

        double fare = defaultFareStrategy.calculateFare(rideRequest);

        assertThat(fare).isEqualTo(80.0);
    }

    @Test
    void calculateFare_shouldReturnMinimumFare_whenDistanceIsZero() {
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(0.0);

        double fare = defaultFareStrategy.calculateFare(rideRequest);

        assertThat(fare).isEqualTo(80.0); // minimum fare applies
    }

    @Test
    void calculateFare_shouldHandleLongDistance() {
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(100.0);

        double fare = defaultFareStrategy.calculateFare(rideRequest);

        assertThat(fare).isEqualTo(1050.0);
    }

    @Test
    void surgeFare_shouldApplySurgeMultiplier() {
        // distance = 10km
        // expected = 50 + (10 * 10 * SURGE_FACTOR(2)) = 250
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(10.0);

        double fare = surgeFareStrategy.calculateFare(rideRequest);

        assertThat(fare).isEqualTo(250.0);
    }

    @Test
    void surgeFare_shouldReturnMinimumFare_whenCalculatedFareBelowMinimum() {
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(0.0);

        double fare = surgeFareStrategy.calculateFare(rideRequest);

        assertThat(fare).isEqualTo(80.0);
    }

    @Test
    void surgeFare_shouldAlwaysBeHigherThanDefaultFare_forSameDistance() {
        when(distanceService.calculateDistance(pickup, destination)).thenReturn(10.0);

        double defaultFare = defaultFareStrategy.calculateFare(rideRequest);
        double surgeFare = surgeFareStrategy.calculateFare(rideRequest);

        assertThat(surgeFare).isGreaterThan(defaultFare);
    }
}