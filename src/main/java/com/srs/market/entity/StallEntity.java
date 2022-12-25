package com.srs.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.srs.common.util.TimestampUtil;
import com.srs.market.StallState;
import com.srs.market.common.dto.StallPoint;
import com.srs.market.entity.converter.StallPointConverter;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stall")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class StallEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID stallId;

    @Column(nullable = false)
    private String code;

    private String name;

    @Column(nullable = false)
    private int state;

    private UUID previousVersion;

    @Column(nullable = false)
    private int status;

    @Column(nullable = false)
    private int type;

    @Column(name = "class")
    private int clazz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floorplan_id", nullable = false)
    private FloorEntity floor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private MarketEntity market;

    private Double area;

    private boolean deleted;

    //////////////////////////////////////////////////////////////////
    // The following props help UI re-render the stall shape in floorplan map
    //////////////////////////////////////////////////////////////////
    private Double rotate;
    private Double xAxis;
    private Double yAxis;
    private Double wAxis;
    private Double hAxis;
    private UUID clonedFrom;

    private boolean publishedAtLeastOnce;

    private int leaseStatus;

    private UUID occupiedBy;

    private boolean isUpdatedDetail;

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

    @Transient
    @JsonIgnore
    public boolean isPrimaryVersion() {
        return previousVersion == null;
    }

    public static StallEntity clone(StallEntity entity) {
        return StallEntity.builder()
                .withCode(entity.getCode())
                .withName(entity.getName())
                .withPreviousVersion(entity.getStallId())
                .withState(StallState.STALL_STATE_UNPUBLISHED_VALUE)
                .withStatus(entity.getStatus())
                .withType(entity.getType())
                .withClazz(entity.getClazz())
                .withArea(entity.getArea())
                .withXAxis(entity.getXAxis())
                .withYAxis(entity.getYAxis())
                .withWAxis(entity.getWAxis())
                .withHAxis(entity.getHAxis())
                .withRotate(entity.getRotate())
                .withFloor(entity.getFloor())
                .withMarket(entity.getMarket())
                .withPublishedAtLeastOnce(entity.isPublishedAtLeastOnce())
                .withLeaseStatus(entity.getLeaseStatus())
                .withOccupiedBy(entity.getOccupiedBy())
                .withClonedFrom(entity.getStallId())
                .build();
    }

}
