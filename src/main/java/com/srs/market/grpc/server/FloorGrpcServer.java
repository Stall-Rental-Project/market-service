package com.srs.market.grpc.server;

import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.market.grpc.service.FloorGrpcService;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@Log4j2
@RequiredArgsConstructor
public class FloorGrpcServer extends FloorServiceGrpc.FloorServiceImplBase {
    private final FloorGrpcService floorServiceGrpc;
    @Override
    public void createFloor(CreateFloorRequest request, StreamObserver<CreateFloorResponse> responseObserver) {
        super.createFloor(request, responseObserver);
    }

    @Override
    public void updateFloor(UpdateFloorRequest request, StreamObserver<UpdateFloorResponse> responseObserver) {
        super.updateFloor(request, responseObserver);
    }

    @Override
    public void getFloor(GetFloorRequest request, StreamObserver<GetFloorResponse> responseObserver) {
        super.getFloor(request, responseObserver);
    }

    @Override
    public void deleteFloor(DeleteFloorRequest request, StreamObserver<NoContentResponse> responseObserver) {
        super.deleteFloor(request, responseObserver);
    }
}
