package com.srs.market.grpc.mapper;

import com.srs.market.*;
import com.srs.market.entity.MarketEntity;
import com.srs.market.grpc.generator.MarketCodeGenerator;
import com.srs.market.util.MarketUtil;
import com.srs.proto.mapper.BaseGrpcMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MarketGrpcMapper implements BaseGrpcMapper<MarketEntity, Market> {
    private final MarketUtil marketUtil;
    private final LocationGrpcMapper locationGrpcMapper;
    private final SupervisorGrpcMapper supervisorGrpcMapper;

    private final MarketCodeGenerator marketCodeGenerator;

    @Override
    public Market toGrpcMessage(MarketEntity entity) {

        return toGrpcBuilder(entity, false, false).build();
    }
    public Market.Builder toGrpcBuilder(MarketEntity market, boolean hasDraft, boolean fetchLocation) {
        return this.toGrpcBuilder(market, hasDraft, fetchLocation, false);
    }
    public Market.Builder toGrpcBuilder(MarketEntity market, boolean hasDraft, boolean fetchLocation, boolean fetchSupervisor) {
        var builder = Market.newBuilder()
                .setMarketId(market.getMarketId().toString())
                .setName(market.getName())
                .setHasPublished(market.getState() == MarketState.MARKET_STATE_PUBLISHED_VALUE)
                .setHasDeleted(market.isDeleted())
                .setHasDraft(hasDraft)
                .setTypeValue(market.getType())
                .setStatusValue(market.getStatus())
                .setStateValue(market.getState())
                .setClazzValue(market.getClazz())
                .setGoogleMap(Objects.requireNonNullElse(market.getGoogleMap(), ""))
                .setCode(market.getCode());

        if (fetchLocation) {
            builder.setLocation(locationGrpcMapper.toGrpcMessage(market.getLocation(), market.getAddress()))
                    .setFullAddress(marketUtil.getFullAddress(market));
        }

        if (fetchSupervisor) {
            builder.setSupervisor(supervisorGrpcMapper.toGrpcMessage(market.getSupervisor()));
        }

        return builder;
    }

    public MarketEntity createMarket(UpsertMarketRequest request) {
        var market = new MarketEntity();

        market.setCode(marketCodeGenerator.generate());
        market.setName(request.getName());
        market.setAddress(request.getLocation().getAddress());
        market.setState(MarketState.MARKET_STATE_UNPUBLISHED_VALUE);
        market.setStatus(request.getStatusValue());
        market.setType(request.hasType() ? request.getTypeValue() : MarketType.MARKET_TYPE_UNSPECIFIED_VALUE);
        market.setPreviousVersion(null);

        if (market.getType() == MarketType.MARKET_TYPE_PUBLIC_VALUE) {
            market.setClazz(request.hasClazz() ? request.getClazzValue() : MarketClass.MARKET_CLASS_A_VALUE);
        }

        market.setDeleted(false);
        market.setGoogleMap(request.getGoogleMap());

        return market;
    }
}
