package com.demo.project.uber.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PointDto {

    @NotNull(message = "Coordinates are required")
    @Size(min = 2, max = 2, message = "Coordinates must have exactly 2 values [longitude, latitude]")
    private Double[] coordinates;

    private String type = "Point";

    public PointDto(Double[] coordinates) {
        this.coordinates = coordinates;
    }


}
