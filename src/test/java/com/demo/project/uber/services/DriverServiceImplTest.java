package com.demo.project.uber.services;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.RideDto;
import com.demo.project.uber.dto.RiderDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.RideRequest;
import com.demo.project.uber.entities.Rider;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.enums.RideRequestStatus;
import com.demo.project.uber.entities.enums.RideStatus;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.repositories.DriverRepository;
import com.demo.project.uber.services.impl.DriverServiceImpl;
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
class DriverServiceImplTest {

    @Mock
    private RideRequestService rideRequestService;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideService rideService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver driver;
    private Ride ride;
    private RideRequest rideRequest;
    private User driverUser;

    @BeforeEach
    void setUp() {
        driverUser = new User();
        driverUser.setId(1L);
        driverUser.setEmail("driver@test.com");

        driver = new Driver();
        driver.setId(1L);
        driver.setUser(driverUser);
        driver.setRating(4.0);
        driver.setIsAvailable(true);

        Rider rider = new Rider();
        rider.setId(1L);

        ride = new Ride();
        ride.setId(1L);
        ride.setDriver(driver);
        ride.setRider(rider);
        ride.setRideStatus(RideStatus.CONFIRMED);
        ride.setOtp("1234");

        rideRequest = new RideRequest();
        rideRequest.setId(1L);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);

        mockSecurityContext();
    }

    private void mockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(driverUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void acceptRide_shouldCreateRideSuccessfully() {
        when(rideRequestService.getRideRequestById(1L)).thenReturn(rideRequest);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(rideService.createNewRide(any(RideRequest.class), any(Driver.class))).thenReturn(ride);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        RideDto result = driverService.acceptRide(1L);

        assertThat(result).isNotNull();
        verify(rideService, times(1)).createNewRide(rideRequest, driver);
    }

    @Test
    void acceptRide_shouldSetDriverUnavailable() {
        when(rideRequestService.getRideRequestById(1L)).thenReturn(rideRequest);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(rideService.createNewRide(any(), any())).thenReturn(ride);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        driverService.acceptRide(1L);

        assertThat(driver.getIsAvailable()).isFalse();
    }

    @Test
    void acceptRide_shouldThrowException_whenRideRequestNotPending() {
        rideRequest.setRideRequestStatus(RideRequestStatus.CONFIRMED);
        when(rideRequestService.getRideRequestById(1L)).thenReturn(rideRequest);

        assertThatThrownBy(() -> driverService.acceptRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("RideRequestStatus cannot be accepted");
    }

    @Test
    void acceptRide_shouldThrowException_whenDriverUnavailable() {
        driver.setIsAvailable(false);
        when(rideRequestService.getRideRequestById(1L)).thenReturn(rideRequest);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> driverService.acceptRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver cannot accept ride due to unavailability");
    }

    @Test
    void startRide_shouldStartRideSuccessfully() {
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(rideService.updateRideStatus(ride, RideStatus.ONGOING)).thenReturn(ride);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        RideDto result = driverService.startRide(1L, "1234");

        assertThat(result).isNotNull();
        verify(rideService, times(1)).updateRideStatus(ride, RideStatus.ONGOING);
        verify(paymentService, times(1)).createNewPayment(ride);
        verify(ratingService, times(1)).createNewRating(ride);
    }

    @Test
    void startRide_shouldThrowException_whenOtpIsInvalid() {
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> driverService.startRide(1L, "9999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("OTP is not valid");
    }

    @Test
    void startRide_shouldThrowException_whenRideNotConfirmed() {
        ride.setRideStatus(RideStatus.ONGOING);
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> driverService.startRide(1L, "1234"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride status is not CONFIRMED");
    }

    @Test
    void endRide_shouldEndRideSuccessfully() {
        ride.setRideStatus(RideStatus.ONGOING);
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(rideService.updateRideStatus(ride, RideStatus.ENDED)).thenReturn(ride);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        RideDto result = driverService.endRide(1L);

        assertThat(result).isNotNull();
        verify(rideService, times(1)).updateRideStatus(ride, RideStatus.ENDED);
        verify(paymentService, times(1)).processPayment(ride);
    }

    @Test
    void endRide_shouldSetDriverAvailable_afterRideEnds() {
        ride.setRideStatus(RideStatus.ONGOING);
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(rideService.updateRideStatus(ride, RideStatus.ENDED)).thenReturn(ride);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        driverService.endRide(1L);

        assertThat(driver.getIsAvailable()).isTrue();
    }

    @Test
    void endRide_shouldThrowException_whenRideNotOngoing() {
        ride.setRideStatus(RideStatus.CONFIRMED);
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> driverService.endRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride status is not ONGOING");
    }

    @Test
    void cancelRide_shouldCancelRideSuccessfully() {
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(rideService.updateRideStatus(ride, RideStatus.CANCELLED)).thenReturn(ride);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        RideDto result = driverService.cancelRide(1L);

        assertThat(result).isNotNull();
        verify(rideService, times(1)).updateRideStatus(ride, RideStatus.CANCELLED);
    }

    @Test
    void cancelRide_shouldThrowException_whenDriverNotOwner() {
        Driver anotherDriver = new Driver();
        anotherDriver.setId(2L);
        ride.setDriver(anotherDriver);

        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> driverService.cancelRide(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver is not the owner");
    }

    @Test
    void rateRider_shouldRateRiderSuccessfully() {
        ride.setRideStatus(RideStatus.ENDED);
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(ratingService.rateRider(ride, 5)).thenReturn(new RiderDto());

        RiderDto result = driverService.rateRider(1L, 5);

        assertThat(result).isNotNull();
        verify(ratingService, times(1)).rateRider(ride, 5);
    }

    @Test
    void rateRider_shouldThrowException_whenRideNotEnded() {
        ride.setRideStatus(RideStatus.ONGOING);
        when(rideService.getRideById(1L)).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> driverService.rateRider(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ride status is not ENDED");
    }

    @Test
    void getMyProfile_shouldReturnDriverProfile() {
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(modelMapper.map(driver, DriverDto.class)).thenReturn(new DriverDto());

        DriverDto result = driverService.getMyProfile();

        assertThat(result).isNotNull();
        verify(modelMapper, times(1)).map(driver, DriverDto.class);
    }

    @Test
    void getCurrentDriver_shouldThrowException_whenDriverNotFound() {
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getCurrentDriver())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver not found with id 1");
    }

    @Test
    void getAllMyRides_shouldReturnPageOfRides() {
        Page<Ride> ridePage = new PageImpl<>(List.of(ride));
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(driver));
        when(rideService.getAllRidesOfDriver(eq(driver), any(PageRequest.class)))
                .thenReturn(ridePage);
        when(modelMapper.map(ride, RideDto.class)).thenReturn(new RideDto());

        Page<RideDto> result = driverService.getAllMyRides(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}