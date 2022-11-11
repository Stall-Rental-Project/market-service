package com.srs.market.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.proto.dto.GrpcPrincipal;

public interface FloorGrpcService {
    UpsertFloorResponse createFloor(UpsertFloorRequest request, GrpcPrincipal principal);

    UpsertFloorResponse updateFloor(UpsertFloorRequest request, GrpcPrincipal principal);

    GetFloorResponse getFloor(GetFloorRequest request, GrpcPrincipal principal);

    NoContentResponse deleteFloor(DeleteFloorRequest request, GrpcPrincipal principal);
    ListFloorsResponse listFloors(ListFloorsRequest request, GrpcPrincipal principal);
    ListFloorsResponse listPublishedFloors(FindByIdRequest request, GrpcPrincipal principal);

    GetFloorResponse getPublishedFloor(GetPublishedFloorRequest request, GrpcPrincipal principal);
    GetFloorCodeAndMarketCodeResponse getFloorCodeAndMarketCode(FindByIdRequest request, GrpcPrincipal principal);

}
