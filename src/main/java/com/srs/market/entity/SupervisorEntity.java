package com.srs.market.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "supervisor")
@Getter
@Setter
public class SupervisorEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID supervisorId;

    @OneToOne(fetch = FetchType.LAZY, optional = false,cascade = CascadeType.ALL)
    @JoinColumn(name = "market_id", nullable = false)
    private MarketEntity market;


    private String firstName;
    private String middleName;
    private String lastName;

    private String email;
    private String position;
    private String telephone;
    private String mobilePhone;
}
