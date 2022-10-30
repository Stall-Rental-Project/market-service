package com.srs.market.grpc.mapper;

import com.srs.market.*;
import com.srs.market.entity.StallEntity;
import com.srs.market.exception.ObjectNotFoundException;
import com.srs.market.repository.MarketRepository;
import com.srs.market.util.MarketUtil;
import com.srs.proto.mapper.BaseGrpcMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElse;


@Component
@RequiredArgsConstructor
public class StallGrpcMapper implements BaseGrpcMapper<StallEntity, Stall> {

    private final MarketUtil marketUtil;
    private final MarketRepository marketRepository;

    public StallInfo toGrpcStallInfo(StallEntity stall) {
        var market = marketRepository.findByIdFetchLocationAndSupervisor(stall.getMarket().getMarketId())
                .orElseThrow(() -> new ObjectNotFoundException("Market not found"));

        return StallInfo.newBuilder()
                .setStallId(stall.getStallId().toString())
                .setCode(stall.getCode())
                .setStallName(stall.getName())
                .setStallTypeValue(stall.getType())
                .setStallClassValue(stall.getClazz())
                .setMarketId(stall.getMarket().getMarketId().toString())
                .setMarketCode(stall.getMarket().getCode())
                .setMarketName(stall.getMarket().getName())
                .setMarketTypeValue(stall.getMarket().getType())
                .setMarketClassValue(
                        stall.getMarket().getType() == MarketType.MARKET_TYPE_PUBLIC_VALUE
                                ? stall.getMarket().getClazz()
                                : MarketClass.MARKET_CLASS_UNSPECIFIED_VALUE)
                .setArea(requireNonNullElse(stall.getArea(), 0.0))
                .setFloorCode(stall.getFloor().getCode())
                .setFloorName(stall.getFloor().getName())
                .setLeaseStatusValue(stall.getLeaseStatus())
                .setMarketAddress(marketUtil.getFullAddress(market))
                .build();
    }

    @Override
    public Stall toGrpcMessage(StallEntity stall) {
        return this.toGrpcMessage(stall, false);
    }

    public Stall toGrpcMessage(StallEntity stall, boolean fetchSection) {
        return this.toGrpcMessage(stall, fetchSection, null, null);
    }

    public Stall toGrpcMessage(StallEntity stall, boolean fetchSection, String marketCode,
                               String floorCode) {
        var stallStatus = StallStatus.forNumber(stall.getStatus());
        var stallType = StallType.forNumber(stall.getType());
        var stallState = StallState.forNumber(stall.getState());
        var stallClazz = StallClass.forNumber(stall.getClazz());

        assert stallStatus != null && stallType != null && stallState != null;

        var builder = Stall.newBuilder()
                .setStallId(stall.getStallId().toString())
                .setCode(stall.getCode())
                .setName(stall.getName())
                .setStatusValue(stall.getStatus())
                .setTypeValue(stall.getType())
                .setStateValue(stall.getState())
                .setClazzValue(stall.getClazz())
                .setArea(requireNonNullElse(stall.getArea(), -1.0))
                .setPreviousVersion(
                        stall.getPreviousVersion() != null ? stall.getPreviousVersion().toString()
                                : "")
                .setXAxis(requireNonNullElse(stall.getXAxis(), 0d))
                .setYAxis(requireNonNullElse(stall.getYAxis(), 0d))
                .setWAxis(requireNonNullElse(stall.getWAxis(), 0d))
                .setHAxis(requireNonNullElse(stall.getHAxis(), 0d))
                .setShape(requireNonNullElse(stall.getShape(), ""))
                .setFontSize(stall.getFontSize())
                .addAllPoints(stall.getPoints().stream()
                        .map(p -> Point.newBuilder()
                                .setXAxis(p.getXAxis())
                                .setYAxis(p.getYAxis())
                                .build())
                        .collect(Collectors.toList()))
                .setLabel(requireNonNullElse(stall.getLabel(), ""))
                .setRotate(requireNonNullElse(stall.getRotate(), 0))
                .setLeaseStatusValue(stall.getLeaseStatus())
                .setOccupiedBy(
                        stall.getOccupiedBy() != null ? stall.getOccupiedBy().toString() : "");

        if (StringUtils.isNotBlank(floorCode)) {
            builder.setFloorCode(floorCode);
        }

        if (StringUtils.isNotBlank(marketCode)) {
            builder.setMarketCode(marketCode);
        }

        try {
            var marketType = MarketType.forNumber(stall.getMarket().getType());
            if (marketType != null) {
                builder.setMarketType(marketType);
            }
        } catch (Exception ignored) {
        }

        try {
            var marketClass = MarketClass.forNumber(stall.getMarket().getClazz());
            if (marketClass != null) {
                builder.setMarketClass(marketClass);
            }
        } catch (Exception ignored) {
        }

        return builder.build();
    }


}
