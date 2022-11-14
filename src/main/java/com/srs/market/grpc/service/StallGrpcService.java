package com.srs.market.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.proto.dto.GrpcPrincipal;

public interface StallGrpcService {

    GetStallResponse createStall(CreateStallRequest request, GrpcPrincipal principal);

    GetStallResponse updateStallMetadata(UpdateStallMetadataRequest request, GrpcPrincipal principal);

    GetStallResponse updateStallPosition(CreateStallRequest request, GrpcPrincipal principal);

    GetStallResponse getStall(GetStallRequest request, GrpcPrincipal principal);

    GetStallResponse getPublishedStall(FindByIdRequest request, GrpcPrincipal principal);

    NoContentResponse deleteStall(FindByIdRequest request, GrpcPrincipal principal);
    GetStallInfoResponse getStallInfo(GetStallInfoRequest request, GrpcPrincipal principal);
    ListStallsInfoResponse listStallsInfo(ListStallsInfoRequest request, GrpcPrincipal principal);

}
