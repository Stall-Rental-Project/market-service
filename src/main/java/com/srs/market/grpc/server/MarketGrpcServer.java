package com.srs.market.grpc.server;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.OnlyIdResponse;
import com.srs.common.PageResponse;
import com.srs.market.*;
import com.srs.market.grpc.service.MarketGrpcService;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@Log4j2
@RequiredArgsConstructor
public class MarketGrpcServer extends MarketServiceGrpc.MarketServiceImplBase {
    private final MarketGrpcService marketGrpcService;

    @Override
    public void listMarkets(ListMarketsRequest request, StreamObserver<PageResponse> responseObserver) {
        super.listMarkets(request, responseObserver);
    }

    @Override
    public void createMarket(UpsertMarketRequest request, StreamObserver<OnlyIdResponse> responseObserver) {
        super.createMarket(request, responseObserver);
    }

    @Override
    public void getMarket(GetMarketRequest request, StreamObserver<GetMarketResponse> responseObserver) {
        super.getMarket(request, responseObserver);
    }

    @Override
    public void updateMarket(UpsertMarketRequest request, StreamObserver<UpdateMarketResponse> responseObserver) {
        super.updateMarket(request, responseObserver);
    }

    @Override
    public void deleteMarket(FindByIdRequest request, StreamObserver<NoContentResponse> responseObserver) {
        super.deleteMarket(request, responseObserver);
    }
}
