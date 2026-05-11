package com.demo.project.uber.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@Setter
@Table(name = "driver", schema = "uber")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double rating;

    private Boolean isAvailable;

    private String vehicleId;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;
}