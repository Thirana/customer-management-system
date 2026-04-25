package com.example.customermanagement.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"mobileNumbers", "addresses", "familyMembers"})
@EqualsAndHashCode(of = "nicNumber")
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "nic_number", nullable = false, unique = true, length = 50)
    private String nicNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MobileNumber> mobileNumbers = new LinkedHashSet<MobileNumber>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Address> addresses = new LinkedHashSet<Address>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_family",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "family_member_id")
    )
    private Set<Customer> familyMembers = new LinkedHashSet<Customer>();

    public void replaceMobileNumbers(Collection<MobileNumber> replacementMobileNumbers) {
        mobileNumbers.clear();
        if (replacementMobileNumbers == null) {
            return;
        }
        for (MobileNumber mobileNumber : replacementMobileNumbers) {
            addMobileNumber(mobileNumber);
        }
    }

    public void replaceAddresses(Collection<Address> replacementAddresses) {
        addresses.clear();
        if (replacementAddresses == null) {
            return;
        }
        for (Address address : replacementAddresses) {
            addAddress(address);
        }
    }

    public void replaceFamilyMembers(Collection<Customer> replacementFamilyMembers) {
        familyMembers.clear();
        if (replacementFamilyMembers != null) {
            familyMembers.addAll(replacementFamilyMembers);
        }
    }

    public void addMobileNumber(MobileNumber mobileNumber) {
        if (mobileNumber != null) {
            mobileNumber.setCustomer(this);
            mobileNumbers.add(mobileNumber);
        }
    }

    public void addAddress(Address address) {
        if (address != null) {
            address.setCustomer(this);
            addresses.add(address);
        }
    }

    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
