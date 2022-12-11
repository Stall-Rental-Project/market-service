package com.srs.market.grpc.service;

import com.srs.market.GetMarketStallAnalyticsRequest;
import com.srs.market.GetMarketStallAnalyticsResponse;

public interface MarketAnalyticGrpcService {
    GetMarketStallAnalyticsResponse getMarketStallAnalytics(GetMarketStallAnalyticsRequest request);
}
