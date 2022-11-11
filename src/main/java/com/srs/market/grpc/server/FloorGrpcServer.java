package com.srs.market.grpc.server;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.market.grpc.service.FloorGrpcService;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@Log4j2
@RequiredArgsConstructor
public class FloorGrpcServer extends FloorServiceGrpc.FloorServiceImplBase {
    private final FloorGrpcService floorGrpcService;
    @Override
    public void createFloor(UpsertFloorRequest request, StreamObserver<UpsertFloorResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.createFloor(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(UpsertFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void updateFloor(UpsertFloorRequest request, StreamObserver<UpsertFloorResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.updateFloor(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(UpsertFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void getFloor(GetFloorRequest request, StreamObserver<GetFloorResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.getFloor(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void deleteFloor(DeleteFloorRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.deleteFloor(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void listFloors(ListFloorsRequest request, StreamObserver<ListFloorsResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.listFloors(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListFloorsResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void getPublishedFloor(GetPublishedFloorRequest request, StreamObserver<GetFloorResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.getPublishedFloor(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void listPublishedFloors(FindByIdRequest request, StreamObserver<ListFloorsResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.listPublishedFloors(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListFloorsResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void getFloorCodeAndMarketCode(FindByIdRequest request, StreamObserver<GetFloorCodeAndMarketCodeResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(floorGrpcService.getFloorCodeAndMarketCode(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetFloorCodeAndMarketCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }
}
