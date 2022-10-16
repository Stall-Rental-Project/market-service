package com.srs.market.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.OnlyIdResponse;
import com.srs.common.PageResponse;
import com.srs.market.*;
import com.srs.proto.dto.GrpcPrincipal;

public interface MarketGrpcService {
    PageResponse listMarkets(ListMarketsRequest request, GrpcPrincipal principal);

    GetMarketResponse getMarket(GetMarketRequest request, GrpcPrincipal principal);

    OnlyIdResponse createMarket(UpsertMarketRequest request, GrpcPrincipal principal);

    UpdateMarketResponse updateMarket(UpsertMarketRequest request, GrpcPrincipal principal);

    NoContentResponse deleteMarket(FindByIdRequest request, GrpcPrincipal principal);
}
