package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.*;

@Service
public class WaltServiceImpl implements WaltService {

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant,
                                               Date deliveryTime) {
        List<Driver> availableDrivers = searchForAvailableDrivers(restaurant, deliveryTime);
        if (availableDrivers.isEmpty()) {
            throw new RuntimeException(String.format("There isn't an available driver"));
        }
        Driver driver = getMostLessBusyDriver(availableDrivers);
        Delivery delivery = new Delivery(driver, restaurant, customer, deliveryTime);
        deliveryRepository.save(delivery);
        return delivery;
    }

    private List<Driver> searchForAvailableDrivers(Restaurant restaurant, Date deliveryTime) {
        List<Driver> availableDrivers = new ArrayList<>();
        for (Driver driver : driverRepository.findAllDriversByCity(restaurant.getCity())) {
            if (checkIfDriverIsAvailable(deliveryTime, driver)) {
                availableDrivers.add(driver);
            }
        }
        return availableDrivers;
    }

    private boolean checkIfDriverIsAvailable(Date deliveryTime, Driver driver) {
        return CollectionUtils.filter(deliveryRepository.findDeliveriesByDriver(driver),
                d -> ((Delivery) d).getDeliveryTime().equals(
                        new Timestamp(deliveryTime.getTime()))).isEmpty();
    }

    private Driver getMostLessBusyDriver(List<Driver> availableDrivers) {
        Collections.sort(availableDrivers, (d1, d2) ->
                Double.compare(getDriverTotalKM(d1), getDriverTotalKM(d2)));
        return availableDrivers.get(0);
    }


    private double getDriverTotalKM(Driver driver) {
        return deliveryRepository.findDeliveriesByDriver(driver).stream()
                .mapToDouble(Delivery::getDistance)
                .sum();
    }


    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findTotalDistanceByDriver();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return null;
    }
}
