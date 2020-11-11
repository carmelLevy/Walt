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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff,
                bach));
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

    }
}
