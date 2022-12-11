package com.srs.market.grpc.service.impl;

import com.srs.market.GetMarketStallAnalyticsRequest;
import com.srs.market.GetMarketStallAnalyticsResponse;
import com.srs.market.MarketStall;
import com.srs.market.StallLeaseStatus;
import com.srs.market.grpc.service.MarketAnalyticGrpcService;
import com.srs.market.repository.StallDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketAnalyticGrpcServiceImpl implements MarketAnalyticGrpcService {
    private final StallDslRepository stallDslRepository;

    @Override
    public GetMarketStallAnalyticsResponse getMarketStallAnalytics(GetMarketStallAnalyticsRequest request) {
        var marketName = request.getMarketName();
        var stalls = stallDslRepository.findAllPublishStallByMarketName(marketName);
        var totalStalls = stalls.size();
        int availableStall = 0;
        int occupiedStall = 0;

        for (var stall : stalls) {
            if (stall.getLeaseStatus() == StallLeaseStatus.STALL_OCCUPIED_VALUE) {
                occupiedStall++;
            } else {
                availableStall++;
            }
        }

        var marketVendorDetail = MarketStall.newBuilder()
                .setMarketName(marketName)
                .setTotalStalls(totalStalls)
                .setTotalStallsAvailable(availableStall)
                .setTotalStallsOccupied(occupiedStall).build();

        return GetMarketStallAnalyticsResponse.newBuilder()
                .setSuccess(true)
                .setData(GetMarketStallAnalyticsResponse.Data.newBuilder()
                        .setMarketVendorDetail(marketVendorDetail)
                        .build())
                .build();
    }
}
