package com.srs.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.srs.common.util.TimestampUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity
@Table(name = "market")
@Getter
@Setter
public class MarketEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID marketId;

    private String code;

    @Column(nullable = false)
    private String name;

    private String address;

    private String googleMap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @Column(nullable = false)
    private int status;

    @Column(nullable = false)
    private int type;

    @Column(name = "class")
    private int clazz;

    @Column(nullable = false)
    private int state;

    private UUID previousVersion;

    private boolean deleted = false;

    @OneToOne(mappedBy = "market", fetch = FetchType.LAZY, orphanRemoval = true)
    private SupervisorEntity supervisor;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Transient
    @JsonIgnore
    public boolean isPrimaryVersion() {
        return previousVersion == null;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = TimestampUtil.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = TimestampUtil.now();
    }
}
