package com.srs.market.grpc.server;

import com.srs.market.GetMarketStallAnalyticsRequest;
import com.srs.market.GetMarketStallAnalyticsResponse;
import com.srs.market.MarketAnalyticsServiceGrpc;
import com.srs.market.grpc.service.MarketAnalyticGrpcService;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.util.GrpcExceptionUtil;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@Log4j2
@RequiredArgsConstructor
public class MarketAnalyticGrpcServer extends MarketAnalyticsServiceGrpc.MarketAnalyticsServiceImplBase {
    private final MarketAnalyticGrpcService marketAnalyticGrpcService;

    @Override
    public void getMarketStallAnalytics(GetMarketStallAnalyticsRequest request, StreamObserver<GetMarketStallAnalyticsResponse> responseObserver) {
        try {
            responseObserver.onNext(marketAnalyticGrpcService.getMarketStallAnalytics(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetMarketStallAnalyticsResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }
}
