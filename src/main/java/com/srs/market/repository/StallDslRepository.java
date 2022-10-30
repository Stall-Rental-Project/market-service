package com.srs.market.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.market.entity.QStallEntity;
import com.srs.market.entity.StallEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
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
}
