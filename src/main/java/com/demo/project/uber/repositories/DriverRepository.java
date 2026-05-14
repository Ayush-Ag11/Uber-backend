package com.demo.project.uber.repositories;

import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Query(value = "SELECT d.*, ST_Distance(d.current_location, :pickupLocation) AS distance " + "FROM driver d " + "WHERE d.is_available = true AND ST_DWithin(d.current_location, :pickupLocation, 10000) " + "ORDER BY distance " + "LIMIT 10", nativeQuery = true)
    List<Driver> find10NearestDrivers(Point pickupLocation);

    @Query(value = "SELECT d.* " + "FROM driver d " + "WHERE d.is_available = true AND ST_DWithin(d.current_location, :pickupLocation, 15000) " + "ORDER BY d.rating DESC " + "LIMIT 10", nativeQuery = true)
    List<Driver> find10NearByTopRatedDrivers(Point pickupLocation);

    Optional<Driver> findByUser(User user);
}

