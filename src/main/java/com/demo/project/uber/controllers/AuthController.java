package com.demo.project.uber.controllers;

import com.demo.project.uber.dto.DriverDto;
import com.demo.project.uber.dto.OnBoardDriverDto;
import com.demo.project.uber.dto.SignupDto;
import com.demo.project.uber.dto.UserDto;
import com.demo.project.uber.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<UserDto> signup(@RequestBody SignupDto signupDto) {
        return new ResponseEntity<>(authService.signUp(signupDto), HttpStatus.CREATED);
    }

    @PostMapping("onBoardNewDriver/{userId}")
    public ResponseEntity<DriverDto> onBoardNewDriver(
            @PathVariable Long userId, @RequestBody OnBoardDriverDto onBoardDriverDto) {
        return new ResponseEntity<>(
                authService.onBoardNewDriver(userId, onBoardDriverDto.getVehicleId()), HttpStatus.CREATED);
    }
}
