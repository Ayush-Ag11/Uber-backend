package com.demo.project.uber.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnBoardDriverDto {

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;
}
