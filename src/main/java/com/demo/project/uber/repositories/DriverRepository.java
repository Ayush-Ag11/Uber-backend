package com.demo.project.uber.repositories;

import com.demo.project.uber.entities.Driver;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    //    @Query("""
//    SELECT d
//    FROM Driver d
//    WHERE d.isAvailable = true
//      AND function(
//            'ST_DWithin',
//            d.currentLocation,
//            :pickupLocation,
//            10000
//      ) = true
//    ORDER BY function(
//            'ST_Distance',
//            d.currentLocation,
//            :pickupLocation
//      )
//""")
    @Query(value = "SELECT d.*, ST_Distance(d.current_location, :pickupLocation) AS distance " + "FROM driver d " + "WHERE d.is_available = true AND ST_DWithin(d.current_location, :pickupLocation, 10000) " + "ORDER BY distance " + "LIMIT 10", nativeQuery = true)
    List<Driver> find10NearestDrivers(Point pickupLocation);

    @Query(value = "SELECT d.* " + "FROM driver d " + "WHERE d.is_available = true AND ST_DWithin(d.current_location, :pickupLocation, 15000) " + "ORDER BY d.rating DESC " + "LIMIT 10", nativeQuery = true)
    List<Driver> find10NearByTopRatedDrivers(Point pickupLocation);
}
