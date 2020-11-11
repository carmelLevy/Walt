package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class WaltServiceImpl implements WaltService {

    protected static final int LESS_BUSY_DRIVER_IDX = 0;
    protected static final String ERROR_MSG_NO_AVAILABLE_DRIVERS =
            "ERROR : There isn't an available driver";
    protected static final String ERROR_MSG_DIFF_CITY =
            "ERROR : The customer and restaurant are in the different cities";
    protected static final String ERROR_MSG_BAD_ARGS =
            "ERROR : An invalid input was provided to this method";

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;

    /**
     * this method creates an order for a given customer from a given restaurant in a given time. If
     * there is not an available driver at that time in the customer's city, a run time exception
     * is being thrown.
     *
     * @param customer
     * @param restaurant
     * @param deliveryTime
     * @return
     */
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant,
                                               Date deliveryTime) {
        validateInput(customer, restaurant, deliveryTime);
        List<Driver> availableDrivers = searchForAvailableDrivers(restaurant, deliveryTime);
        Driver driver = getMostLessBusyDriver(availableDrivers);
        Delivery delivery = new Delivery(driver, restaurant, customer, deliveryTime);
        deliveryRepository.save(delivery);
        return delivery;
    }

    private void validateInput(Customer customer, Restaurant restaurant, Date deliveryTime) {
        if (customer == null || restaurant == null || deliveryTime == null) {
            throw new RuntimeException(String.format(ERROR_MSG_BAD_ARGS));
        }
        if (!customer.getCity().getId().equals(restaurant.getCity().getId())) {
            throw new RuntimeException(String.format(ERROR_MSG_DIFF_CITY));
        }
    }

    /**
     * this method creates a list of all the drivers who is available at deliveryTime
     *
     * @param restaurant
     * @param deliveryTime
     * @return a list of all available drivers
     */
    private List<Driver> searchForAvailableDrivers(Restaurant restaurant, Date deliveryTime) {
        List<Driver> availableDrivers = new ArrayList<>();
        for (Driver driver : driverRepository.findAllDriversByCity(restaurant.getCity())) {
            if (checkIfDriverIsAvailable(deliveryTime, driver)) {
                availableDrivers.add(driver);
            }
        }
        if (availableDrivers.isEmpty()) {
            throw new RuntimeException(String.format(ERROR_MSG_NO_AVAILABLE_DRIVERS));
        }
        return availableDrivers;
    }

    /**
     * this method checks if a driver is available at deliveryTime. It does so by checking the
     * list of all the deliveries of the given driver and filtering any delivery which has a
     * different delivery time then the one were checking. This way- if we are left with an empty
     * list we know he has no delivery at deliveryTime, aka - he is free.
     *
     * @param deliveryTime
     * @param driver
     * @return
     */
    private boolean checkIfDriverIsAvailable(Date deliveryTime, Driver driver) {
        return CollectionUtils.filter(deliveryRepository.findDeliveriesByDriver(driver),
                d -> ((Delivery) d).getDeliveryTime().equals(
                        new Timestamp(deliveryTime.getTime()))).isEmpty();
    }

    /**
     * this method returns the most less busy driver by sorting all the available drivers list by
     * the distance of their deliveries.
     *
     * @param availableDrivers
     * @return
     */
    private Driver getMostLessBusyDriver(List<Driver> availableDrivers) {
        Collections.sort(availableDrivers, Comparator.comparingDouble(this::getDriverTotalKM));
        return availableDrivers.get(LESS_BUSY_DRIVER_IDX);
    }

    /**
     * this method returns a driver total distance in km
     *
     * @param driver
     * @return
     */
    private double getDriverTotalKM(Driver driver) {
        return deliveryRepository.findDeliveriesByDriver(driver).stream()
                .mapToDouble(Delivery::getDistance)
                .sum();
    }

    /***
     * this method gets a list of DriverDistance object which has all drivers names and distances
     * @return
     */
    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findAllDistancesByDriver();
    }

    /**
     * this method gets a list of DriverDistance object which has all drivers names and distances
     * in a given city
     *
     * @param city
     * @return
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return deliveryRepository.findAllCityDistancesByDriver(city);
    }
}
