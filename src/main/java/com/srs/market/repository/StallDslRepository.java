package com.srs.market.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.market.dto.projection.StallCodeProjection;
import com.srs.market.entity.QStallEntity;
import com.srs.market.entity.StallEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Log4j2
public class StallDslRepository {
    private final QStallEntity stall = QStallEntity.stallEntity;

    private final JPAQueryFactory queryFactory;

    public List<StallEntity> findAllByFloorIdAndDraft(UUID primaryFloorId, boolean shouldGetDraft) {
        var query = queryFactory.select(stall)
                .from(stall)
                .where(stall.floor.floorId.eq(primaryFloorId))
                .where(stall.deleted.isFalse());

        var allStalls = query.fetch();

        Map<UUID, UUID> stallHasDraft = new HashMap<>();
        Set<UUID> primaryStallIds = new HashSet<>();

        for (StallEntity stall : allStalls) {
            if (stall.isPrimaryVersion()) {
                primaryStallIds.add(stall.getStallId());
            } else {
                stallHasDraft.put(stall.getPreviousVersion(), stall.getStallId());
            }
        }

        List<StallEntity> returned = new ArrayList<>();

        for (var stall : allStalls) {
            if (shouldGetDraft) {
                if (!primaryStallIds.contains(stall.getStallId()) || !stallHasDraft.containsKey(stall.getStallId())) {
                    returned.add(stall);
                }
            } else {
                if (primaryStallIds.contains(stall.getStallId())) {
                    returned.add(stall);
                }
            }
        }

        return returned;
    }

    public Optional<StallEntity> findById4Update(UUID stallId) {
        JPAQuery<StallEntity> query = queryFactory.select(stall)
                .from(stall)
                .join(stall.floor).fetchJoin()
                .where(stall.stallId.eq(stallId).or(stall.previousVersion.eq(stallId)));

        List<StallEntity> stalls = query.fetch();

        if (CollectionUtils.isEmpty(stalls)) {
            return Optional.empty();
        } else if (stalls.size() == 1) {
            return Optional.of(stalls.get(0));
        } else {
            log.info("Stall with id {} is draft version or has draft version. Return draft version", stallId);
            return stalls.stream().filter(s -> s.getPreviousVersion() != null).findFirst();
        }
    }

    public Optional<StallEntity> findById4Get(UUID stallId, boolean draft) {
        JPAQuery<StallEntity> query = queryFactory.select(stall)
                .from(stall)
                .where(stall.stallId.eq(stallId).or(stall.previousVersion.eq(stallId)))
                .where(stall.deleted.eq(false));

        List<StallEntity> stalls = query.fetch();

        if (CollectionUtils.isEmpty(stalls)) {
            return Optional.empty();
        } else if (stalls.size() == 1) {
            return Optional.of(stalls.get(0));
        } else {
            if (draft) {
                log.info("Stall with id {} is draft version or has draft version. Return draft version", stallId);
                return stalls.stream().filter(s -> s.getPreviousVersion() != null).findFirst();
            } else {
                return stalls.stream().filter(s -> s.getPreviousVersion() == null).findFirst();
            }
        }
    }

    public StallCodeProjection findFloorAndMarketCodeOfStall(UUID stallId) {
        JPAQuery<StallCodeProjection> query = queryFactory.select(Projections.constructor(StallCodeProjection.class, stall.floor.code, stall.market.code, stall.code))
                .from(stall)
                .where(stall.stallId.eq(stallId));

        return query.fetchOne();
    }

}
