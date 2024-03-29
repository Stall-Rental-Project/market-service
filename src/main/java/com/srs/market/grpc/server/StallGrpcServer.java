package com.srs.market.grpc.server;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.market.grpc.service.StallGrpcService;
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
public class StallGrpcServer extends StallServiceGrpc.StallServiceImplBase {
    private final StallGrpcService stallGrpcService;

    @Override
    public void createStall(CreateStallRequest request, StreamObserver<GetStallResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.createStall(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }


    @Override
    public void updateStallMetadata(UpdateStallMetadataRequest request, StreamObserver<GetStallResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.updateStallMetadata(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void updateStallPosition(CreateStallRequest request, StreamObserver<GetStallResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.updateStallPosition(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void getStall(GetStallRequest request, StreamObserver<GetStallResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.getStall(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void getPublishedStall(FindByIdRequest request, StreamObserver<GetStallResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.getPublishedStall(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void deleteStall(FindByIdRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.deleteStall(request, principal));
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
    public void getStallInfo(GetStallInfoRequest request, StreamObserver<GetStallInfoResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.getStallInfo(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetStallInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }
    @Override
    public void listStallsInfo(ListStallsInfoRequest request, StreamObserver<ListStallsInfoResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(stallGrpcService.listStallsInfo(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListStallsInfoResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }
}
