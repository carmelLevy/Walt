package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData() {

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa, bash);

        createRestaurant(jerusalem, tlv, bash);
    }

    private void createRestaurant(City jerusalem, City tlv, City bash) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");
        Restaurant mozes = new Restaurant("mozes", bash, "hamburger restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican,
                mozes));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa, City bash) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer richi = new Customer("Richi", jerusalem, "Richongo");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");
        Customer moshe = new Customer("Moshe", bash, "Moshe's address");
        Customer itamar = new Customer("Itamar", bash, "itamar's address");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff,
                bach, moshe, itamar,richi));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert
                , david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics() {
        assertEquals(((List<City>) cityRepository.findAll()).size(), 4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva"
        )).size()), 2);
    }

    @Test
    public void testAddOneOrder() {
        Customer testCustomer = customerRepository.findByName("Bach");
        Restaurant testRestaurant = restaurantRepository.findByName("cafe");
        Date date = new Date();
        Driver testDriver = driverRepository.findByName("Mary");
        Delivery delivery = waltService.createOrderAndAssignDriver(testCustomer, testRestaurant,
                date);
        assertEquals(testDriver.getId(), delivery.getDriver().getId());
    }

    @Test
    public void testDriverCanBeAvailableAfterHour() {
        Customer testCustomer1 = customerRepository.findByName("Mozart");
        Customer testCustomer2 = customerRepository.findByName("Richi");
        Restaurant testRestaurant = restaurantRepository.findByName("meat");
        Date date = new Date();

        Driver testDriver = driverRepository.findByName("Robert");

        Delivery delivery = waltService.createOrderAndAssignDriver(testCustomer1, testRestaurant,
                date);

        Date date2 = new Date();
        date2.setTime(date2.getTime() + TimeUnit.HOURS.toMillis(1));

        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer2, testRestaurant,
                date2);
        assertEquals(testDriver.getId(), delivery.getDriver().getId());
    }

    @Test
    public void testTwoOrdersSameRestaurant() {
        Customer testCustomer1 = customerRepository.findByName("Bach");
        Customer testCustomer2 = customerRepository.findByName("Rachmaninoff");

        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Date date = new Date();

        Delivery delivery = waltService.createOrderAndAssignDriver(testCustomer1, restaurant, date);

        Driver driver1 = driverRepository.findByName("Mary");
        Driver driver2 = driverRepository.findByName("Patricia");

        Long supposedSecDriverID;
        supposedSecDriverID = delivery.getDriver().getId().equals(driver1.getId()) ?
                driver2.getId() : driver1.getId();


        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer2, restaurant,
                date);
        assertEquals(supposedSecDriverID, delivery2.getDriver().getId());
    }

    @Test
    public void testTwoOrdersDiffTime() {
        Customer testCustomer1 = customerRepository.findByName("Bach");
        Customer testCustomer2 = customerRepository.findByName("Rachmaninoff");

        Restaurant restaurant = restaurantRepository.findByName("vegan");

        Date date1 = new Date();

        Delivery delivery1 = waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                date1);

        Date date2 = new Date();
        date2.setTime(date2.getTime() + TimeUnit.HOURS.toMillis(2));

        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer2, restaurant,
                date2);

        assertNotEquals(delivery1.getDeliveryTime(), delivery2.getDeliveryTime());
    }


    @Test
    public void testBusiestDriver() {
        Customer testCustomer1 = customerRepository.findByName("Moshe");
        Customer testCustomer2 = customerRepository.findByName("Itamar");

        Restaurant restaurant = restaurantRepository.findByName("mozes");
        Date date = new Date();
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(10000));


        Delivery delivery = waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                date);

        Driver driver1 = driverRepository.findByName("James");
        Driver driver2 = driverRepository.findByName("John");

        Long supposedSecDriverID;
        supposedSecDriverID = delivery.getDriver().getId().equals(driver1.getId()) ?
                driver2.getId() : driver1.getId();

        Date date2 = new Date();

        date2.setTime(date.getTime() + TimeUnit.HOURS.toMillis(1));
        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer2, restaurant,
                date2);
        assertEquals(supposedSecDriverID, delivery2.getDriver().getId());

        Date date3 = new Date();

        date3.setTime(date.getTime() + TimeUnit.HOURS.toMillis(2));
        Delivery delivery3 = waltService.createOrderAndAssignDriver(testCustomer2, restaurant,
                date3);

        supposedSecDriverID = delivery.getDistance() > delivery2.getDistance() ?
                delivery2.getDriver().getId() : delivery.getDriver().getId();

        assertEquals(supposedSecDriverID, delivery3.getDriver().getId());
    }

    @Test
    public void testDriverRankReport() {
        Customer testCustomer1 = customerRepository.findByName("Moshe");
        Customer testCustomer2 = customerRepository.findByName("Mozart");

        Restaurant restaurant = restaurantRepository.findByName("mozes");
        Restaurant restaurant2 = restaurantRepository.findByName("meat");

        Date date = new Date();
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(10000));


        Delivery delivery1 = waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                date);
        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer2, restaurant2,
                date);

        List<DriverDistance> driverDistances = waltService.getDriverRankReport();

        System.out.println("\nDrivers rank based on their total distance report:");
        System.out.println("--------------------------------------------------\n");

        for (DriverDistance d : driverDistances) {
            System.out.println("Driver Name: " + d.getDriver().getName() +
                    ", Driver Distance: " + d.getTotalDistance());
        }
    }

    @Test
    public void testDriverRankReportByCity() {

        Customer testCustomer1 = customerRepository.findByName("Moshe");
        Customer testCustomer2 = customerRepository.findByName("Itamar");

        Restaurant restaurant = restaurantRepository.findByName("mozes");
        Date date = new Date();
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(10000));


        Delivery delivery1 = waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                date);
        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer2, restaurant,
                date);

        City city = cityRepository.findByName("Beer-Sheva");

        List<DriverDistance> driverDistancesByCity = waltService.getDriverRankReportByCity(city);


        System.out.println("\nDrivers rank based on their total distance report by city " +
                city.getName() +
                ":");
        System.out.println(
                "----------------------------------------------------------------------\n");

        for (DriverDistance d : driverDistancesByCity) {
            System.out.println("Driver Name: " + d.getDriver().getName() +
                    ", Driver Distance: " + d.getTotalDistance());
        }
    }

    @Test
    public void testDifferentCityOfCustomerAndRestaurant() {
        Customer testCustomer1 = customerRepository.findByName("Moshe"); //in bash

        Restaurant restaurant = restaurantRepository.findByName("chinese"); //in tlv
        Date date = new Date();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                    date);
        });

        String expectedMessage = "ERROR : The customer and restaurant are in the different cities";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testAvailableDriversIsEmpty() {
        Customer testCustomer1 = customerRepository.findByName("Moshe");

        Restaurant restaurant = restaurantRepository.findByName("mozes");
        Date date = new Date();

        Delivery delivery1 = waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                date);
        Delivery delivery2 = waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                date);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                    date);
        });

        String expectedMessage = "ERROR : There isn't an available driver";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testInvalidInput() {
        Customer testCustomer1 = customerRepository.findByName("Moshe");

        Restaurant restaurant = restaurantRepository.findByName("mozes");
        Date date = new Date();


        // Test for null customer name
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(null, restaurant,
                    date);
        });
        String expectedMessage = "ERROR : An invalid input was provided to this method";
        String actualMessage1 = exception1.getMessage();
        assertTrue(actualMessage1.contains(expectedMessage));

        // Test for null restaurant name
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(testCustomer1, null,
                    date);
        });
        String actualMessage2 = exception2.getMessage();
        assertTrue(actualMessage2.contains(expectedMessage));

        // Test for null delivery time
        Exception exception3 = assertThrows(RuntimeException.class, () -> {
            waltService.createOrderAndAssignDriver(testCustomer1, restaurant,
                    null);
        });
        String actualMessage3 = exception3.getMessage();
        assertTrue(actualMessage3.contains(expectedMessage));
    }

}
