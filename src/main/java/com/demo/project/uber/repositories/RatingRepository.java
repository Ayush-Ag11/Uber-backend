package com.demo.project.uber.repositories;

import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.Rating;
import com.demo.project.uber.entities.Ride;
import com.demo.project.uber.entities.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByRide(Ride ride);

    @Query("""
            SELECT AVG(r.riderRating)
            FROM Rating r
            WHERE r.rider = :rider
            AND r.riderRating IS NOT NULL
            """)
    Double getAverageRiderRating(@Param("rider") Rider rider);

    @Query("""
            SELECT AVG(r.driverRating)
            FROM Rating r
            WHERE r.driver = :driver
            AND r.driverRating IS NOT NULL
            """)
    Double getAverageDriverRating(@Param("driver") Driver driver);
}


