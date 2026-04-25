package com.example.customermanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.customermanagement.dto.response.CustomerSummaryDTO;
import com.example.customermanagement.entity.Address;
import com.example.customermanagement.entity.City;
import com.example.customermanagement.entity.Country;
import com.example.customermanagement.entity.Customer;
import com.example.customermanagement.entity.MobileNumber;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void findByNicNumberReturnsMatchingCustomer() {
        Customer customer = customerRepository.save(customer("Nimal Perera", "NIC-001"));

        Optional<Customer> foundCustomer = customerRepository.findByNicNumber("NIC-001");

        assertTrue(foundCustomer.isPresent());
        assertEquals(customer.getId(), foundCustomer.get().getId());
    }

    @Test
    void duplicateNicChecksRespectCurrentCustomerId() {
        Customer customer = customerRepository.save(customer("Nimal Perera", "NIC-002"));

        assertTrue(customerRepository.existsByNicNumber("NIC-002"));
        assertFalse(customerRepository.existsByNicNumberAndIdNot("NIC-002", customer.getId()));
        assertTrue(customerRepository.existsByNicNumberAndIdNot("NIC-002", customer.getId() + 1));
    }

    @Test
    void customerSummaryQueryReturnsCountsWithoutDetailGraph() {
        City city = saveCity("Sri Lanka", "Colombo");
        Customer customer = customer("Kasun Silva", "NIC-003");
        customer.addMobileNumber(mobileNumber("0771234567"));
        customer.addMobileNumber(mobileNumber("0777654321"));
        customer.addAddress(address("Line 1", city));
        customerRepository.saveAndFlush(customer);

        Page<CustomerSummaryDTO> page = customerRepository.findCustomerSummaries(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Kasun Silva", page.getContent().get(0).getName());
        assertEquals(2, page.getContent().get(0).getMobileNumberCount());
        assertEquals(1, page.getContent().get(0).getAddressCount());
    }

    @Test
    void detailQueryLoadsRequiredAssociations() {
        City city = saveCity("Sri Lanka", "Kandy");
        Customer familyMember = customerRepository.save(customer("Family Member", "NIC-004-F"));
        Customer customer = customer("Amal Fernando", "NIC-004");
        customer.addMobileNumber(mobileNumber("0711234567"));
        customer.addAddress(address("Kandy Road", city));
        customer.replaceFamilyMembers(Collections.singleton(familyMember));
        customerRepository.saveAndFlush(customer);

        Customer detail = customerRepository.findDetailById(customer.getId()).get();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(detail, "mobileNumbers"));
        assertTrue(unitUtil.isLoaded(detail, "addresses"));
        assertTrue(unitUtil.isLoaded(detail, "familyMembers"));
        assertEquals(1, detail.getMobileNumbers().size());
        assertEquals(1, detail.getAddresses().size());
        assertEquals(1, detail.getFamilyMembers().size());
        assertTrue(unitUtil.isLoaded(detail.getAddresses().iterator().next(), "city"));
        assertTrue(unitUtil.isLoaded(detail.getAddresses().iterator().next().getCity(), "country"));
    }

    @Test
    void bulkNicLookupReturnsMatchingCustomers() {
        customerRepository.save(customer("First Customer", "NIC-005-A"));
        customerRepository.save(customer("Second Customer", "NIC-005-B"));
        customerRepository.save(customer("Third Customer", "NIC-005-C"));

        assertEquals(
                2,
                customerRepository.findByNicNumberIn(Arrays.asList("NIC-005-A", "NIC-005-C")).size()
        );
    }

    private City saveCity(String countryName, String cityName) {
        Country country = new Country();
        country.setName(countryName);
        countryRepository.save(country);

        City city = new City();
        city.setName(cityName);
        city.setCountry(country);
        return cityRepository.save(city);
    }

    private Customer customer(String name, String nicNumber) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setNicNumber(nicNumber);
        return customer;
    }

    private MobileNumber mobileNumber(String number) {
        MobileNumber mobileNumber = new MobileNumber();
        mobileNumber.setMobileNumber(number);
        return mobileNumber;
    }

    private Address address(String line1, City city) {
        Address address = new Address();
        address.setAddressLine1(line1);
        address.setCity(city);
        return address;
    }
}
