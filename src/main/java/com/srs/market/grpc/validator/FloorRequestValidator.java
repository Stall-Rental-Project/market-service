package com.srs.market.grpc.validator;

import com.srs.common.Error;
import com.srs.market.UpsertFloorRequest;


public interface FloorRequestValidator {
    Error validate(UpsertFloorRequest request);

}
