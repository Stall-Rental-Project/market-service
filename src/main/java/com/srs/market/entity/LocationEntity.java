package com.srs.market.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author duynt on 9/24/21
 */
@Entity
@Table(name = "location")
@Getter
@Setter
public class LocationEntity implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID locationId;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String barangay;

    @Column
    private String zipcode;

    @Column
    private String district;

    @Column(nullable = false)
    private boolean builtin = false;

}
