package com.srs.market.grpc.validator;

import com.srs.common.Error;
import com.srs.market.CreateFloorRequest;
import com.srs.market.UpdateFloorRequest;


public interface FloorRequestValidator {
    Error validate(CreateFloorRequest request);

    Error validate(UpdateFloorRequest request);
}
