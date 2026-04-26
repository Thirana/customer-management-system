package com.example.customermanagement.repository;

import com.example.customermanagement.dto.response.CustomerSummaryDTO;
import com.example.customermanagement.entity.Customer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNicNumber(String nicNumber);

    boolean existsByNicNumber(String nicNumber);

    // Update validation uses this variant so the current customer does not conflict with itself.
    boolean existsByNicNumberAndIdNot(String nicNumber, Long id);

    // Bulk import resolves existing customers in one query instead of one NIC lookup per row.
    List<Customer> findByNicNumberIn(Collection<String> nicNumbers);

    // Detail views need the full graph, while normal entity relationships remain lazy by default.
    @EntityGraph(attributePaths = {
            "mobileNumbers",
            "addresses",
            "addresses.city",
            "addresses.city.country",
            "familyMembers"
    })
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findDetailById(@Param("id") Long id);

    // List views use a compact DTO projection to keep pagination queries predictable and cheap.
    @Query(
            value = "select new com.example.customermanagement.dto.response.CustomerSummaryDTO("
                    + "c.id, c.name, c.dateOfBirth, c.nicNumber, "
                    + "count(distinct m.id), count(distinct a.id)) "
                    + "from Customer c "
                    + "left join c.mobileNumbers m "
                    + "left join c.addresses a "
                    + "group by c.id, c.name, c.dateOfBirth, c.nicNumber",
            countQuery = "select count(c) from Customer c"
    )
    Page<CustomerSummaryDTO> findCustomerSummaries(Pageable pageable);

    // Search stays on the same summary query shape so large family-member lookups do not need a separate API.
    @Query(
            value = "select new com.example.customermanagement.dto.response.CustomerSummaryDTO("
                    + "c.id, c.name, c.dateOfBirth, c.nicNumber, "
                    + "count(distinct m.id), count(distinct a.id)) "
                    + "from Customer c "
                    + "left join c.mobileNumbers m "
                    + "left join c.addresses a "
                    + "where lower(c.name) like lower(concat('%', :search, '%')) "
                    + "or lower(c.nicNumber) like lower(concat('%', :search, '%')) "
                    + "group by c.id, c.name, c.dateOfBirth, c.nicNumber",
            countQuery = "select count(c) from Customer c "
                    + "where lower(c.name) like lower(concat('%', :search, '%')) "
                    + "or lower(c.nicNumber) like lower(concat('%', :search, '%'))"
    )
    Page<CustomerSummaryDTO> findCustomerSummariesBySearch(@Param("search") String search, Pageable pageable);
}
