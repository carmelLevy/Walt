package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

    List<Delivery> findDeliveriesByDriver(Driver driver);

    @Query("SELECT d.driver AS driver, SUM(d.distance) AS totalDistance FROM Delivery d GROUP BY" +
            " d.driver ORDER BY totalDistance DESC")
    List<DriverDistance> findAllDistancesByDriver();

    @Query("SELECT d.driver AS driver, SUM(d.distance) AS totalDistance FROM Delivery d WHERE " +
            "d.driver.city =: chosenCity GROUP BY d.driver ORDER BY totalDistance DESC")
    List<DriverDistance> findAllCityDistancesByDriver(@Param("chosenCity") City city);
}


