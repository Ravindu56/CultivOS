package com.cultivos.identity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps to the V1 `roles` table. The 8 platform roles are seeded by Flyway V1. */
@Entity
@Table(name = "roles")
public class Role {

    public static final String FARMER = "FARMER";
    public static final String FIELD_OFFICER = "FIELD_OFFICER";
    public static final String REGIONAL_MANAGER = "REGIONAL_MANAGER";
    public static final String TRADER = "TRADER";
    public static final String RETAILER = "RETAILER";
    public static final String CONSUMER = "CONSUMER";
    public static final String SUPPLIER = "SUPPLIER";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    protected Role() {
        // JPA
    }

    public Role(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
