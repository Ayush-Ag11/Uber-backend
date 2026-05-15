package com.demo.project.uber.services.impl;

import com.demo.project.uber.entities.ServiceCommunicationException;
import com.demo.project.uber.services.DistanceService;
import lombok.Data;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class DistanceServiceOSRMImpl implements DistanceService {

    private final RestClient restClient;

    public DistanceServiceOSRMImpl(@Value("${osrm.api.base-url}") String osrmBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(osrmBaseUrl)
                .build();
    }

    @Override
    public double calculateDistance(Point src, Point dest) {

        try {
            String uri = src.getX() + "," + src.getY() + ";" + dest.getX() + "," + dest.getY();

            log.info("Calculating distance from [{},{}] to [{},{}]",
                    src.getX(), src.getY(), dest.getX(), dest.getY());

            OSRMResponseDto responseDto = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OSRMResponseDto.class);

            if (responseDto == null
                    || responseDto.getRoutes() == null
                    || responseDto.getRoutes().isEmpty()) {
                throw new ServiceCommunicationException(
                        "OSRM returned empty response for route: " + uri);
            }

            double distanceInKm = responseDto.getRoutes().get(0).getDistance() / 1000.0;

            log.info("Distance calculated: {} km", distanceInKm);

            return distanceInKm;
        } catch (ServiceCommunicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling OSRM API: {}", e.getMessage());
            throw new ServiceCommunicationException(
                    "Error getting distance from OSRM API: " + e.getMessage());
        }
    }

    @Data
    private static class OSRMResponseDto {
        private List<OSRMRoute> routes;
    }

    @Data
    private static class OSRMRoute {
        private Double distance;
    }

}
