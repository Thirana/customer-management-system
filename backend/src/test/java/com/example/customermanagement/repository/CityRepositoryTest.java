package com.example.customermanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.customermanagement.entity.City;
import com.example.customermanagement.entity.Country;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void findAllByOrderByCountryNameAscNameAscSortsByCountryThenCity() {
        Country sriLanka = country("Sri Lanka");
        Country australia = country("Australia");
        countryRepository.save(sriLanka);
        countryRepository.save(australia);
        cityRepository.save(city("Kandy", sriLanka));
        cityRepository.save(city("Colombo", sriLanka));
        cityRepository.save(city("Melbourne", australia));

        List<City> cities = cityRepository.findAllByOrderByCountryNameAscNameAsc();

        assertEquals("Melbourne", cities.get(0).getName());
        assertEquals("Colombo", cities.get(1).getName());
        assertEquals("Kandy", cities.get(2).getName());
    }

    private Country country(String name) {
        Country country = new Country();
        country.setName(name);
        return country;
    }

    private City city(String name, Country country) {
        City city = new City();
        city.setName(name);
        city.setCountry(country);
        return city;
    }
}
