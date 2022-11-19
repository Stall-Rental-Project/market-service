package com.srs.market.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.common.PageRequest;
import com.srs.common.Status;
import com.srs.common.domain.Page;
import com.srs.market.ListMarketsRequest;
import com.srs.market.MarketState;
import com.srs.market.common.Constant;
import com.srs.market.entity.MarketEntity;
import com.srs.market.entity.QLocationEntity;
import com.srs.market.entity.QMarketEntity;
import com.srs.market.entity.QSupervisorEntity;
import com.srs.market.grpc.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import static com.srs.market.MarketStatus.MARKET_STATUS_ACTIVE_VALUE;

@Repository
@RequiredArgsConstructor
@Log4j2
public class MarketDslRepository {


    private final QMarketEntity market = QMarketEntity.marketEntity;
    private final QLocationEntity location = QLocationEntity.locationEntity;
    private final QSupervisorEntity owner = QSupervisorEntity.supervisorEntity;

    private final JPAQueryFactory queryFactory;

    public Page<MarketEntity> listMarkets(ListMarketsRequest request) {
        var query = queryFactory.from(market)
                .innerJoin(location).on(location.locationId.eq(market.location.locationId))
                .where(market.previousVersion.isNull())
                .where(market.deleted.isFalse());

        if (StringUtils.isNotBlank(request.getSearchTerm())) {
            query.where(market.name.containsIgnoreCase(request.getSearchTerm())
                    .or(market.address.containsIgnoreCase(request.getSearchTerm()))
                    .or(location.ward.containsIgnoreCase(request.getSearchTerm()))
                    .or(location.district.containsIgnoreCase(request.getSearchTerm()))
                    .or(location.city.containsIgnoreCase(request.getSearchTerm()))
            );
        }

        if (request.getTypesCount() > 0) {
            query.where(market.type.in(request.getTypesValueList()));
        }

        if (request.getCodesCount() > 0) {
            query.where(market.code.in(request.getCodesList()));
        }

        if (request.getPublishedOnly()) {
            query.where(market.state.eq(MarketState.MARKET_STATE_PUBLISHED_VALUE))
                    .where(market.status.eq(Status.ACTIVE_VALUE));
        }

        if (!request.getAll()) {
            var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.MARKET_SORTS);
            var selectQuery = query.clone().select(market)
                    .limit(pageRequest.getSize())
                    .offset(PageUtil.calcPageOffset(pageRequest.getPage(), pageRequest.getSize()))
                    .orderBy(this.getOrderSpecifier(pageRequest));
            var countQuery = query.clone().select(market.count());

            return Page.from(selectQuery.fetch(), countQuery.fetchFirst());
        } else {
            var markets = query.clone().select(market).fetch();

            return Page.from(markets, markets.size());
        }
    }

    private OrderSpecifier<?> getOrderSpecifier(PageRequest pageRequest) {
        var order = Order.valueOf(pageRequest.getDirection().toUpperCase());
        switch (pageRequest.getSort()) {
            case "location":
                return new OrderSpecifier<>(order, (market.address
                        .concat(location.ward)
                        .concat(location.district)
                        .concat(location.city)).toLowerCase());
            case "types":
                return new OrderSpecifier<>(order, market.type);
            case "statuses":
                return new OrderSpecifier<>(order, market.status);
            default:
                return new OrderSpecifier<>(order, market.name.toLowerCase());
        }
    }
}
