package com.srs.market.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.market.StallState;
import com.srs.market.entity.FloorEntity;
import com.srs.market.entity.QFloorEntity;
import com.srs.market.entity.QMarketEntity;
import com.srs.market.entity.QStallEntity;
import com.srs.market.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Repository
@Log4j2
@RequiredArgsConstructor
public class FloorDslRepository {
    private final QMarketEntity market = QMarketEntity.marketEntity;
    private final QFloorEntity floor=QFloorEntity.floorEntity;

    private final QStallEntity stall=QStallEntity.stallEntity;
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

    public List<Tuple> countStallsByMarketIdGroupByFloor(UUID marketId, boolean draft) {
        var query = queryFactory.select(stall.floor.floorId, stall.count())
                .from(stall)
                .where(stall.market.code.eq(queryFactory.select(market.code)
                                .from(market)
                                .where(market.marketId.eq(marketId))
                        )
                )
                .groupBy(stall.floor.floorId);

        this.addCountStallsFilterByDraftFlag(draft, query);

        return query.fetch();
    }

    public List<Tuple> countStallsHasDetailByMarketIdGroupByFloor(UUID marketId, boolean draft) {
        var query = queryFactory.select(stall.floor.floorId, stall.count())
                .from(stall)
                .where(stall.market.code.eq(queryFactory.select(market.code)
                                .from(market)
                                .where(market.marketId.eq(marketId))
                        )
                )
                .where(stall.name.isNotNull())
                .where(stall.name.isNotEmpty())
                .groupBy(stall.floor.floorId);

        this.addCountStallsFilterByDraftFlag(draft, query);

        return query.fetch();
    }
    private void addCountStallsFilterByDraftFlag(boolean draft, JPAQuery<Tuple> query) {
        var other = new QStallEntity("other");
        query.where(stall.deleted.isFalse());
        if (draft) {
            query.where(Expressions.anyOf(
                    stall.previousVersion.isNotNull(),
                    stall.previousVersion.isNull()
                            .and(queryFactory.selectZero()
                                    .from(other)
                                    .where(other.previousVersion.eq(stall.stallId))
                                    .notExists())
            ));
        } else {
            query.where(stall.previousVersion.isNull())
                    .where(stall.state.eq(StallState.STALL_STATE_PUBLISHED_VALUE));

        }
    }


    public Optional<FloorEntity> findById4CreateStall(UUID floorplanId) {
        JPAQuery<FloorEntity> query = queryFactory.select(floor)
                .from(floor)
                .join(floor.market).fetchJoin()
                .where(floor.floorId.eq(floorplanId).or(floor.previousVersion.eq(floorplanId)))
                .where(floor.deleted.eq(false));

        List<FloorEntity> floors = query.fetch();

        if (CollectionUtils.isEmpty(floors)) {
            return Optional.empty();
        } else if (floors.size() == 1) {
            return Optional.of(floors.get(0));
        } else {
            log.info("Floor with id {} is draft version or has draft version. Returns the primary version", floorplanId);
            return floors.stream().filter(s -> s.getPreviousVersion() == null).findFirst();
        }
    }
}
