package com.srs.market.grpc.validator;

import com.srs.common.Error;
import com.srs.common.FindByIdRequest;
import com.srs.common.FindByIdsRequest;
import com.srs.market.CreateStallRequest;
import com.srs.market.UpdateStallMetadataRequest;

import java.util.UUID;


public interface StallRequestValidator {

    Error validate(UpdateStallMetadataRequest request, UUID userId);

    Error validate(FindByIdsRequest request, UUID userId);

    Error validate(FindByIdRequest request, UUID userId);

}
