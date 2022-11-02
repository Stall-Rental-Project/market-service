package com.srs.market.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.market.*;
import com.srs.proto.dto.GrpcPrincipal;

public interface StallGrpcService {

    GetStallResponse createStall(CreateStallRequest request, GrpcPrincipal principal);

    GetStallResponse updateStallMetadata(UpdateStallMetadataRequest request, GrpcPrincipal principal);

    GetStallResponse updateStallPosition(UpdateStallPositionRequest request, GrpcPrincipal principal);

    GetStallResponse getStall(GetStallRequest request, GrpcPrincipal principal);

    GetStallResponse getPublishedStall(FindByIdRequest request, GrpcPrincipal principal);


}
