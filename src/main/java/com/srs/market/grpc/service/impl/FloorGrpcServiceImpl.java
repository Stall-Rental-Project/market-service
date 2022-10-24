package com.srs.market.grpc.service.impl;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.market.grpc.service.FloorGrpcService;
import com.srs.proto.dto.GrpcPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FloorGrpcServiceImpl implements FloorGrpcService {
    @Override
    public CreateFloorResponse createFloor(CreateFloorRequest request, GrpcPrincipal principal) {
        return null;
    }

    @Override
    public UpdateFloorResponse updateFloor(UpdateFloorRequest request, GrpcPrincipal principal) {
        return null;
    }

    @Override
    public GetFloorResponse getFloor(GetFloorRequest request, GrpcPrincipal principal) {
        return null;
    }

    @Override
    public NoContentResponse deleteFloor(DeleteFloorRequest request, GrpcPrincipal principal) {
        return null;
    }
}
