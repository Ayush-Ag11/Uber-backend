package com.demo.project.uber.services;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.RideDto;
import com.demo.project.uber.dto.RideRequestDto;
import com.demo.project.uber.dto.RiderDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.entities.Rider;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.enums.PaymentMethod;
import com.demo.project.uber.entities.enums.RideRequestStatus;
import com.demo.project.uber.entities.enums.RideStatus;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.RideRequestRepository;
import com.demo.project.uber.repositories.RiderRepository;
import com.demo.project.uber.services.impl.RiderServiceImpl;
import com.demo.project.uber.strategies.RideStrategyManager;
import com.demo.project.uber.strategies.DriverMatchingStrategy;
import com.demo.project.uber.strategies.RideFareCalculationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiderServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RideStrategyManager rideStrategyManager;

    @Mock
    private RideRequestRepository rideRequestRepository;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private RideService rideService;

    @Mock
    private DriverService driverService;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RiderServiceImpl riderService;

    private User riderUser;
    private Rider rider;
    private Driver driver;
    private Ride ride;
    private RideRequest rideRequest;
    private RideRequestDto rideRequestDto;

    @BeforeEach
    void setUp() {
        riderUser = new User();
        riderUser.setId(1L);
        riderUser.setEmail("rider@test.com");

        rider = new Rider();
        rider.setId(1L);
        rider.setUser(riderUser);
        rider.setRating(4.0);

        driver = new Driver();
        driver.setId(1L);
        driver.setIsAvailable(true);

        ride = new Ride();
        ride.setId(1L);
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.CONFIRMED);

        rideRequest = new RideRequest();
        rideRequest.setId(1L);
        rideRequest.setRider(rider);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setPaymentMethod(PaymentMethod.WALLET);

        rideRequestDto = new RideRequestDto();

        mockSecurityContext();
    }

    private void mockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(riderUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void requestRide_shouldCreateRideRequestSuccessfully() {
        RideFareCalculationStrategy fareStrategy = mock(RideFareCalculationStrategy.class);
        DriverMatchingStrategy matchingStrategy = mock(DriverMatchingStrategy.class);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(modelMapper.map(rideRequestDto, RideRequest.class)).thenReturn(rideRequest);
        when(rideStrategyManager.rideFareStrategy()).thenReturn(fareStrategy);
        when(fareStrategy.calculateFare(rideRequest)).thenReturn(150.0);
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(rideRequest);
        when(rideStrategyManager.driverMatchingStrategy(rider.getRating()))
                .thenReturn(matchingStrategy);
        when(matchingStrategy.findMatchingDrivers(rideRequest)).thenReturn(List.of(driver));
        when(modelMapper.map(rideRequest, RideRequestDto.class)).thenReturn(rideRequestDto);

        RideRequestDto result = riderService.requestRide(rideRequestDto);

        assertThat(result).isNotNull();
        verify(rideRequestRepository, times(1)).save(any(RideRequest.class));
    }

    @Test
    void requestRide_shouldSetFareOnRideRequest() {
        RideFareCalculationStrategy fareStrategy = mock(RideFareCalculationStrategy.class);
        DriverMatchingStrategy matchingStrategy = mock(DriverMatchingStrategy.class);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(modelMapper.map(rideRequestDto, RideRequest.class)).thenReturn(rideRequest);
        when(rideStrategyManager.rideFareStrategy()).thenReturn(fareStrategy);
        when(fareStrategy.calculateFare(rideRequest)).thenReturn(150.0);
        when(rideRequestRepository.save(any())).thenReturn(rideRequest);
        when(rideStrategyManager.driverMatchingStrategy(rider.getRating()))
                .thenReturn(matchingStrategy);
        when(matchingStrategy.findMatchingDrivers(rideRequest)).thenReturn(List.of(driver));
        when(modelMapper.map(rideRequest, RideRequestDto.class)).thenReturn(rideRequestDto);

        riderService.requestRide(rideRequestDto);

        assertThat(rideRequest.getFare()).isEqualTo(150.0);
    }

    @Test
    void requestRide_shouldSetStatusToPending() {
        RideFareCalculationStrategy fareStrategy = mock(RideFareCalculationStrategy.class);
        DriverMatchingStrategy matchingStrategy = mock(DriverMatchingStrategy.class);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(modelMapper.map(rideRequestDto, RideRequest.class)).thenReturn(rideRequest);
        when(rideStrategyManager.rideFareStrategy()).thenReturn(fareStrategy);
        when(fareStrategy.calculateFare(rideRequest)).thenReturn(150.0);
        when(rideRequestRepository.save(any())).thenReturn(rideRequest);
        when(rideStrategyManager.driverMatchingStrategy(rider.getRating()))
                .thenReturn(matchingStrategy);
        when(matchingStrategy.findMatchingDrivers(rideRequest)).thenReturn(List.of(driver));
        when(modelMapper.map(rideRequest, RideRequestDto.class)).thenReturn(rideRequestDto);

        riderService.requestRide(rideRequestDto);

        assertThat(rideRequest.getRideRequestStatus()).isEqualTo(RideRequestStatus.PENDING);
    }

    @Test
    void cancelRide_shouldCancelRideSuccessfully() {
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(rideService.updateRideStatus(ride, RideStatus.CANCELLED)).thenReturn(ride);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        RideDto result = riderService.cancelRide(1L);

        assertThat(result).isNotNull();
        verify(rideService, times(1)).updateRideStatus(ride, RideStatus.CANCELLED);
        verify(driverService, times(1)).updateDriverAvailability(driver, true);
    }

    @Test
    void cancelRide_shouldThrowException_whenRiderNotOwner() {
        Rider anotherRider = new Rider();
        anotherRider.setId(2L);
        ride.setRider(anotherRider);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getRideById(1L)).thenReturn(ride);

        assertThatThrownBy(() -> riderService.cancelRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rider do not own this ride");
    }

    @Test
    void cancelRide_shouldThrowException_whenRideNotConfirmed() {
        ride.setRideStatus(RideStatus.ONGOING);
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getRideById(1L)).thenReturn(ride);

        assertThatThrownBy(() -> riderService.cancelRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride cannot be cancelled");
    }

    @Test
    void rateDriver_shouldRateDriverSuccessfully() {
        ride.setRideStatus(RideStatus.ENDED);
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(ratingService.rateDriver(ride, 5)).thenReturn(new DriverDto());

        DriverDto result = riderService.rateDriver(1L, 5);

        assertThat(result).isNotNull();
        verify(ratingService, times(1)).rateDriver(ride, 5);
    }

    @Test
    void rateDriver_shouldThrowException_whenRideNotEnded() {
        ride.setRideStatus(RideStatus.ONGOING);
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getRideById(1L)).thenReturn(ride);

        assertThatThrownBy(() -> riderService.rateDriver(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver cannot be rated");
    }

    @Test
    void rateDriver_shouldThrowException_whenRiderNotOwner() {
        Rider anotherRider = new Rider();
        anotherRider.setId(2L);
        ride.setRider(anotherRider);
        ride.setRideStatus(RideStatus.ENDED);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getRideById(1L)).thenReturn(ride);

        assertThatThrownBy(() -> riderService.rateDriver(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rider do not own this ride");
    }

    @Test
    void getMyProfile_shouldReturnRiderProfile() {
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(modelMapper.map(rider, RiderDto.class)).thenReturn(new RiderDto());

        RiderDto result = riderService.getMyProfile();

        assertThat(result).isNotNull();
        verify(modelMapper, times(1)).map(rider, RiderDto.class);
    }

    @Test
    void getCurrentRider_shouldThrowException_whenRiderNotFound() {
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riderService.getCurrentRider())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rider not associated with user");
    }

    @Test
    void createNewRider_shouldCreateRiderWithZeroRating() {
        when(riderRepository.save(any(Rider.class))).thenReturn(rider);

        Rider result = riderService.createNewRider(riderUser);

        assertThat(result).isNotNull();
        verify(riderRepository, times(1)).save(any(Rider.class));
    }

    @Test
    void getAllMyRides_shouldReturnPageOfRides() {
        Page<Ride> ridePage = new PageImpl<>(List.of(ride));
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(rider));
        when(rideService.getAllRidesOfRider(eq(rider), any(PageRequest.class)))
                .thenReturn(ridePage);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        Page<RideDto> result = riderService.getAllMyRides(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}