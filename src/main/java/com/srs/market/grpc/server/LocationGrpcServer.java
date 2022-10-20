package com.srs.market.grpc.server;

import com.srs.common.FindByIdsRequest;
import com.srs.common.ListResponse;
import com.srs.market.*;
import com.srs.market.grpc.service.LocationGrpcService;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Log4j2
@RequiredArgsConstructor
public class LocationGrpcServer extends LocationServiceGrpc.LocationServiceImplBase {
    private final LocationGrpcService locationGrpcService;
    @Override
    public void listProvinces(ListProvinceRequest request, StreamObserver<ListProvinceResponse> responseObserver) {
        try {
            responseObserver.onNext(locationGrpcService.listProvinces(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListProvinceResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void listCities(ListCityRequest request, StreamObserver<ListCityResponse> responseObserver) {
        try {
            responseObserver.onNext(locationGrpcService.listCities(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListCityResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void listWards(ListWardRequest request, StreamObserver<ListWardResponse> responseObserver) {
        try {
            responseObserver.onNext(locationGrpcService.listWards(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListWardResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }     }

    @Override
    public void listLocations(FindByIdsRequest request, StreamObserver<ListResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(locationGrpcService.listLocations(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }     }

    @Override
    public void getLocation(GetLocationRequest request, StreamObserver<GetLocationResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(locationGrpcService.getLocation(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetLocationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }     }
}
