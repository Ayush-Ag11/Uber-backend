package com.demo.project.uber.services.impl;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.SignupDto;
import com.demo.project.uber.dto.UserDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.enums.Role;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.exceptions.RuntimeConflictExceptions;
import com.demo.project.uber.repositories.UserRepository;
import com.demo.project.uber.security.JWTService;
import com.demo.project.uber.services.AuthService;
import com.demo.project.uber.services.DriverService;
import com.demo.project.uber.services.RiderService;
import com.demo.project.uber.services.UserService;
import com.demo.project.uber.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final RiderService riderService;
    private final WalletService walletService;
    private final DriverService driverService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserService userService;

    @Override
    public String[] login(String email, String password) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));

        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new String[]{accessToken, refreshToken};
    }

    @Override
    @Transactional
    public UserDto signUp(SignupDto signupDto) {

        User user = userRepository.findByEmail(signupDto.getEmail()).orElse(null);

        if (user != null) {
            throw new RuntimeConflictExceptions("User already exists with email " + signupDto.getEmail());
        }

        User mappedUser = modelMapper.map(signupDto, User.class);
        mappedUser.setRoles(Set.of(Role.RIDER));

        mappedUser.setPassword(passwordEncoder.encode(mappedUser.getPassword()));

        User savedUser = userRepository.save(mappedUser);

        riderService.createNewRider(savedUser);

        walletService.createNewWallet(savedUser);

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public DriverDto onBoardNewDriver(Long userId, String vehicleId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (user.getRoles().contains(Role.DRIVER)) {
            throw new RuntimeConflictExceptions("Driver already exists with id " + user.getId());
        }

        Driver createdDriver = Driver.builder()
                .user(user)
                .rating(0.0)
                .vehicleId(vehicleId)
                .isAvailable(true)
                .build();

        user.getRoles().add(Role.DRIVER);

        userRepository.save(user);

        Driver savedDriver = driverService.createNewDriver(createdDriver);
        return modelMapper.map(savedDriver, DriverDto.class);

    }

    @Override
    public String refreshToken(String refreshToken) {
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userService.getUserById(userId);

        return jwtService.generateAccessToken(user);
    }
}
