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
@Table(name = "floorplan")
@Getter
@Setter
public class FloorEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false, name = "floorplan_id")
    private UUID floorId;

    private String code;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String imageUrl;
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private MarketEntity market;

    @Column(nullable = false)
    private int state;

    private UUID previousVersion;

    private boolean deleted = false;

    private boolean publishedAtLeastOnce = false;

    @Transient
    @JsonIgnore
    public boolean isPrimaryVersion() {
        return previousVersion == null;
    }
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = TimestampUtil.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = TimestampUtil.now();
    }
}
