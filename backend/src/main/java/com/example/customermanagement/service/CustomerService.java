package com.example.customermanagement.service;

import com.example.customermanagement.dto.request.AddressRequestDTO;
import com.example.customermanagement.dto.request.CustomerCreateDTO;
import com.example.customermanagement.dto.request.CustomerUpdateDTO;
import com.example.customermanagement.dto.response.CustomerResponseDTO;
import com.example.customermanagement.dto.response.CustomerSummaryDTO;
import com.example.customermanagement.dto.response.PageResponse;
import com.example.customermanagement.entity.Address;
import com.example.customermanagement.entity.City;
import com.example.customermanagement.entity.Customer;
import com.example.customermanagement.entity.MobileNumber;
import com.example.customermanagement.exception.CustomerNotFoundException;
import com.example.customermanagement.exception.DuplicateNicException;
import com.example.customermanagement.exception.InvalidRequestException;
import com.example.customermanagement.exception.MasterDataNotFoundException;
import com.example.customermanagement.mapper.CustomerMapper;
import com.example.customermanagement.repository.CityRepository;
import com.example.customermanagement.repository.CustomerRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            new HashSet<String>(Arrays.asList("id", "name", "nicNumber", "dateOfBirth"));

    private final CustomerRepository customerRepository;
    private final CityRepository cityRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(
            CustomerRepository customerRepository,
            CityRepository cityRepository,
            CustomerMapper customerMapper
    ) {
        this.customerRepository = customerRepository;
        this.cityRepository = cityRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerSummaryDTO> listCustomers(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                Sort.by(resolveSortDirection(sortDir), resolveSortField(sortBy))
        );
        Page<CustomerSummaryDTO> customers = customerRepository.findCustomerSummaries(pageable);
        return PageResponse.from(customers);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomer(Long id) {
        return customerMapper.toCustomerResponse(loadDetail(id));
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerCreateDTO request) {
        if (customerRepository.existsByNicNumber(request.getNicNumber())) {
            LOGGER.warn("Customer create rejected because the NIC is already in use");
            throw new DuplicateNicException();
        }

        Customer customer = new Customer();
        applyCreateFields(customer, request);

        Customer savedCustomer = customerRepository.save(customer);
        // Flush before mapping so generated child IDs are available in the response body.
        customerRepository.flush();
        CustomerResponseDTO response = customerMapper.toCustomerResponse(savedCustomer);
        LOGGER.info("Customer created customerId={}", response.getId());
        return response;
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerUpdateDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (customerRepository.existsByNicNumberAndIdNot(request.getNicNumber(), id)) {
            LOGGER.warn("Customer update rejected because the NIC is already in use customerId={}", id);
            throw new DuplicateNicException();
        }

        applyUpdateFields(customer, request);
        customerRepository.flush();
        LOGGER.info("Customer updated customerId={}", customer.getId());
        return customerMapper.toCustomerResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.replaceFamilyMembers(new ArrayList<Customer>());
        customerRepository.delete(customer);
        LOGGER.info("Customer deleted customerId={}", id);
    }

    private Customer loadDetail(Long id) {
        return customerRepository.findDetailById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private void applyCreateFields(Customer customer, CustomerCreateDTO request) {
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());
        customer.replaceMobileNumbers(toMobileNumberEntities(request.getMobileNumbers()));
        customer.replaceAddresses(toAddressEntities(request.getAddresses()));
        customer.replaceFamilyMembers(resolveFamilyMembers(request.getFamilyMemberIds(), null));
    }

    private void applyUpdateFields(Customer customer, CustomerUpdateDTO request) {
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());
        customer.replaceMobileNumbers(toMobileNumberEntities(request.getMobileNumbers()));
        customer.replaceAddresses(toAddressEntities(request.getAddresses()));
        customer.replaceFamilyMembers(resolveFamilyMembers(request.getFamilyMemberIds(), customer.getId()));
    }

    private List<MobileNumber> toMobileNumberEntities(List<String> numbers) {
        List<MobileNumber> mobileNumbers = new ArrayList<MobileNumber>();
        if (numbers == null) {
            return mobileNumbers;
        }

        for (String number : numbers) {
            if (number == null || number.trim().isEmpty()) {
                continue;
            }
            MobileNumber mobileNumber = new MobileNumber();
            mobileNumber.setMobileNumber(number.trim());
            mobileNumbers.add(mobileNumber);
        }
        return mobileNumbers;
    }

    private List<Address> toAddressEntities(List<AddressRequestDTO> requests) {
        List<Address> addresses = new ArrayList<Address>();
        if (requests == null) {
            return addresses;
        }

        for (AddressRequestDTO request : requests) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> {
                        LOGGER.warn("Customer write rejected because the city was not found cityId={}", request.getCityId());
                        return new MasterDataNotFoundException("City not found with ID: " + request.getCityId());
                    });

            Address address = new Address();
            address.setAddressLine1(request.getAddressLine1());
            address.setAddressLine2(request.getAddressLine2());
            address.setCity(city);
            addresses.add(address);
        }
        return addresses;
    }

    private List<Customer> resolveFamilyMembers(List<Long> familyMemberIds, Long currentCustomerId) {
        List<Customer> familyMembers = new ArrayList<Customer>();
        if (familyMemberIds == null || familyMemberIds.isEmpty()) {
            return familyMembers;
        }

        Set<Long> uniqueIds = new HashSet<Long>(familyMemberIds);
        if (currentCustomerId != null && uniqueIds.contains(currentCustomerId)) {
            LOGGER.warn("Customer update rejected because a customer cannot be their own family member customerId={}",
                    currentCustomerId);
            throw new InvalidRequestException("A customer cannot be added as their own family member.");
        }

        List<Customer> foundFamilyMembers = customerRepository.findAllById(uniqueIds);
        Set<Long> foundIds = foundFamilyMembers.stream()
                .map(Customer::getId)
                .collect(Collectors.toSet());
        for (Long familyMemberId : uniqueIds) {
            if (!foundIds.contains(familyMemberId)) {
                LOGGER.warn("Customer write rejected because a family member was not found familyMemberId={}",
                        familyMemberId);
                throw new CustomerNotFoundException(familyMemberId);
            }
        }

        familyMembers.addAll(foundFamilyMembers);
        return familyMembers;
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String resolveSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "name";
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            LOGGER.warn("Customer list rejected because an unsupported sort field was requested sortBy={}", sortBy);
            throw new InvalidRequestException("Unsupported sort field: " + sortBy);
        }
        return sortBy;
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        if ("desc".equalsIgnoreCase(sortDir)) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }
}
