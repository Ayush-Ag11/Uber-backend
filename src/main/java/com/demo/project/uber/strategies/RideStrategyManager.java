package com.demo.project.uber.strategies;

import com.demo.project.uber.strategies.impl.DriverMatchingHighestRatedDriverStrategy;
import com.demo.project.uber.strategies.impl.DriverMatchingNearestDriverStrategy;
import com.demo.project.uber.strategies.impl.RideFareDefaultFareCalculationStrategy;
import com.demo.project.uber.strategies.impl.RideFareSurgePricingFareStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class RideStrategyManager {

    private final DriverMatchingNearestDriverStrategy driverMatchingNearestDriverStrategy;
    private final DriverMatchingHighestRatedDriverStrategy driverMatchingHighestRatedDriverStrategy;
    private final RideFareSurgePricingFareStrategy rideFareSurgePricingFareStrategy;
    private final RideFareDefaultFareCalculationStrategy rideFareDefaultFareCalculationStrategy;

    public DriverMatchingStrategy driverMatchingStrategy(double riderRating) {
        if (riderRating > 4.8) {
            return driverMatchingHighestRatedDriverStrategy;
        } else {
            return driverMatchingNearestDriverStrategy;
        }
    }

    public RideFareCalculationStrategy rideFareStrategy() {

        LocalTime surgeStartTime = LocalTime.of(17, 30);
        LocalTime surgeEndTime = LocalTime.of(20, 30);
        LocalTime currentTime = LocalTime.now();
        boolean isSurgeTime = currentTime.isAfter(surgeStartTime) && currentTime.isBefore(surgeEndTime);

        if (isSurgeTime) {
            return rideFareSurgePricingFareStrategy;
        } else {
            return rideFareDefaultFareCalculationStrategy;
        }
    }
}
