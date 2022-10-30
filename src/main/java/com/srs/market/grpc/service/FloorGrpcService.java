package com.srs.market.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.proto.dto.GrpcPrincipal;

public interface FloorGrpcService {
    CreateFloorResponse createFloor(CreateFloorRequest request, GrpcPrincipal principal);

    UpdateFloorResponse updateFloor(UpdateFloorRequest request, GrpcPrincipal principal);

    GetFloorResponse getFloor(GetFloorRequest request, GrpcPrincipal principal);

    NoContentResponse deleteFloor(DeleteFloorRequest request, GrpcPrincipal principal);
    ListFloorsResponse listFloors(ListFloorsRequest request, GrpcPrincipal principal);

}
