package com.demo.project.uber.services;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.SignupDto;
import com.demo.project.uber.dto.UserDto;

public interface AuthService {

    String[] login(String email, String password);

    UserDto signUp(SignupDto signupDto);

    DriverDto onBoardNewDriver(Long userId, String vehicleId);

    String refreshToken(String refreshToken);
}
