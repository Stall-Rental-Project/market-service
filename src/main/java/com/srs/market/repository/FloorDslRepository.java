package com.srs.market.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.market.entity.FloorEntity;
import com.srs.market.entity.QFloorEntity;
import com.srs.market.entity.QMarketEntity;
import com.srs.market.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Log4j2
@RequiredArgsConstructor
public class FloorDslRepository {
    private final QMarketEntity market = QMarketEntity.marketEntity;
    private final QFloorEntity floor=QFloorEntity.floorEntity;
    private final JPAQueryFactory queryFactory;

    public FloorEntity findOneById(UUID floorId, boolean shouldGetDraft) {
        var query = queryFactory.select(floor)
                .from(floor)
                .where(floor.floorId.eq(floorId)
                        .or(floor.previousVersion.eq(floorId))
                );

        var floors = query.fetch();

        if (floors.size() > 1) {
            var primaryIndex = floors.get(0).isPrimaryVersion() ? 0 : 1;
            return shouldGetDraft ? floors.get(Math.abs(1 - primaryIndex)) : floors.get(primaryIndex);
        } else if (floors.isEmpty()) {
            throw new ObjectNotFoundException("Floor not found");
        } else {
            var fetchedFloor = floors.get(0);
            var isDraft = !fetchedFloor.isPrimaryVersion();

            if (isDraft && !shouldGetDraft) {
                return queryFactory.select(floor)
                        .from(floor)
                        .where(floor.floorId.eq(fetchedFloor.getPreviousVersion()))
                        .fetchFirst();
            } else {
                return fetchedFloor;
            }
        }
    }
    public List<FloorEntity> findAllByMarketId4List(UUID marketId, boolean draft) {
        var query = queryFactory.select(floor)
                .from(floor)
                .where(floor.market.marketId.eq(marketId))
                .where(floor.deleted.isFalse())
                .orderBy(new OrderSpecifier<>(Order.ASC, floor.name));

        var entities = query.fetch();

        var primaryFloors = new ArrayList<FloorEntity>();
        var originFloors = new ArrayList<FloorEntity>();
        var draftFloors = new ArrayList<FloorEntity>();

        var primaryToDraftIds = new HashMap<UUID, UUID>();

        for (var floor : entities) {
            if (!floor.isPrimaryVersion()) {
                draftFloors.add(floor);
                primaryToDraftIds.put(floor.getPreviousVersion(), floor.getFloorId());
            }
        }

        for (var floor : entities) {
            if (floor.isPrimaryVersion()) {
                if (primaryToDraftIds.containsKey(floor.getFloorId())) {
                    primaryFloors.add(floor);
                } else {
                    originFloors.add(floor);
                }
            }
        }

        var returned = new ArrayList<>(originFloors);
        if (draft) {
            returned.addAll(draftFloors);
        } else {
            returned.addAll(primaryFloors);
        }

        returned.sort(Comparator.comparing(FloorEntity::getName));

        return returned;
    }
}
