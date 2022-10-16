package com.srs.market.grpc.validator;

import com.srs.common.Error;
import com.srs.common.FindByIdRequest;
import com.srs.market.ListMarketsRequest;
import com.srs.market.UpsertMarketRequest;

import java.util.UUID;


public interface MarketRequestValidator {
    Error validate(ListMarketsRequest request, UUID userId);

    Error validate(UpsertMarketRequest request, UUID userId);

    Error validate(FindByIdRequest request, UUID userId);

}
