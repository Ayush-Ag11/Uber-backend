package com.demo.project.uber.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RideStartDto {

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 4, message = "OTP must be exactly 4 digits")
    private String otp;
}
