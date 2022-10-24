package com.srs.market.repository;

import com.srs.market.entity.QFloorEntity;
import com.srs.market.entity.QMarketEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
@RequiredArgsConstructor
public class FloorDslRepository {
    private final QMarketEntity market = QMarketEntity.marketEntity;
    private final QFloorEntity floor=QFloorEntity.floorEntity;
}
