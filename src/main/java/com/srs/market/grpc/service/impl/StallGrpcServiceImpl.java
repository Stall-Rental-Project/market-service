package com.srs.market.grpc.service.impl;

import com.srs.common.Error;
import com.srs.common.ErrorCode;
import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.market.*;
import com.srs.market.common.dto.StallPoint;
import com.srs.market.entity.StallEntity;
import com.srs.market.grpc.mapper.StallGrpcMapper;
import com.srs.market.grpc.service.StallGrpcService;
import com.srs.market.grpc.validator.StallRequestValidator;
import com.srs.market.repository.*;
import com.srs.proto.dto.GrpcPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElse;

@Service
@RequiredArgsConstructor
@Log4j2
public class StallGrpcServiceImpl implements StallGrpcService {
    private final StallDslRepository stallDslRepository;
    private final StallRepository stallRepository;

    private final FloorStallIndexRepository floorStallIndexRepository;
    private final StallGrpcMapper stallGrpcMapper;
    private final FloorRepository floorRepository;

    private final MarketRepository marketRepository;
    private final FloorDslRepository floorDslRepository;
    private final StallRequestValidator requestValidator;

    @Override
    @Transactional
    public GetStallResponse createStall(CreateStallRequest request, GrpcPrincipal principal) {
        var userId = principal.getUserId();
        var errors = requestValidator.validate(request, userId);

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var floorId = UUID.fromString(request.getFloorplanId());

        var floor = floorDslRepository.findById4CreateStall(floorId)
                .orElseThrow(() -> new ObjectNotFoundException("Floor not found"));

        if (!floor.isPrimaryVersion()) {
            log.info("Finding original floor to create stall...");

            floor = floorDslRepository.findById4CreateStall(floor.getPreviousVersion())
                    .orElseThrow(() -> new ObjectNotFoundException("Floor not found"));
        }

        var currentStallIndex = floorStallIndexRepository.getCurrentIndexOfFloor(floor.getCode());

        if (currentStallIndex == null) {
            throw new IllegalStateException("Cannot find current stall index of floor " + floor.getFloorId());
        }

        var stall = StallEntity.builder()
                .withCode(currentStallIndex + "")
                .withName(requireNonNullElse(request.getName(), ""))
                .withStatus(request.hasStatus() ? request.getStatusValue() : StallStatus.STALL_STATUS_INACTIVE_VALUE)
                .withType(request.hasType() ? request.getTypeValue() : StallType.STALL_TYPE_TEMPORARY_VALUE)
                .withClazz(request.getClazzValue())
                .withState(StallState.STALL_STATE_UNPUBLISHED_VALUE)
                .withArea(request.getArea())
                .withShape(request.getShape())
                .withXAxis(request.getXAxis())
                .withYAxis(request.getYAxis())
                .withWAxis(request.getWAxis())
                .withHAxis(request.getHAxis())
                .withPoints(request.getPointsList().stream().map(p -> new StallPoint(p.getXAxis(), p.getYAxis())).collect(Collectors.toList()))
                .withLabel(request.getLabel())
                .withRotate(request.getRotate())
                .withFloor(floor)
                .withMarket(floor.getMarket())
                .withLeaseStatus(StallLeaseStatus.STALL_AVAILABLE_VALUE)
                .withFontSize(request.getFontSize())
                .build();

        var created = stallRepository.save(stall);

        var grpcStall = stallGrpcMapper.toGrpcMessage(created, true, floor.getMarket().getCode(), floor.getCode());

        floorStallIndexRepository.increaseFloorIndex(floor.getCode(), 1);


        return GetStallResponse.newBuilder()
                .setSuccess(true)
                .setData(GetStallResponse.Data.newBuilder()
                        .setStall(grpcStall)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public GetStallResponse updateStallMetadata(UpdateStallMetadataRequest request, GrpcPrincipal principal) {
        var userId = principal.getUserId();
        var errors = requestValidator.validate(request, userId);

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        Optional<StallEntity> optional = stallDslRepository.findById4Update(UUID.fromString(request.getStallId()));

        if (optional.isEmpty() || optional.get().isDeleted()) {
            if (optional.isPresent() && optional.get().isDeleted()) {
                log.error("Stall have been marked as deleted");
            }

            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot update stall")
                            .putDetails("stall_id", "Stall not exists with given stall_id")
                            .build())
                    .build();
        }

        var stall = optional.get();

        if (stall.isPrimaryVersion() && !stallRepository.existsByPreviousVersion(stall.getStallId())) {
            log.info("Creating draft version for stall {}", stall.getStallId());

            var draft = StallEntity.clone(stall);

            return doUpdateStallMetadata(draft, request, principal);
        } else if (stall.isPrimaryVersion()) {
            log.info("Finding to update draft version for stall {}", stall.getStallId());

            var draft = stallRepository.findDraftVersionById(stall.getStallId())
                    .orElseThrow(() -> new ObjectNotFoundException("Stall not found with given stall_id"));

            return doUpdateStallMetadata(draft, request, principal);
        } else {
            log.info("Updating draft version for stall {}", stall.getStallId());

            return doUpdateStallMetadata(stall, request, principal);
        }
    }

    @Override
    @Transactional
    public GetStallResponse updateStallPosition(UpdateStallPositionRequest request, GrpcPrincipal principal) {
        var userId = principal.getUserId();
        var errors = requestValidator.validate(request, userId);

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        Optional<StallEntity> optional = stallDslRepository.findById4Update(UUID.fromString(request.getStallId()));

        if (optional.isEmpty() || optional.get().isDeleted()) {
            if (optional.isPresent() && optional.get().isDeleted()) {
                log.error("Stall have been marked as deleted");
            }

            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot update stall")
                            .putDetails("stall_id", "Stall not exists with given stall_id")
                            .build())
                    .build();
        }

        var stall = optional.get();

        if (stall.isPrimaryVersion() && !stallRepository.existsByPreviousVersion(stall.getStallId())) {
            log.info("Creating draft version for stall {}", stall.getStallId());

            var draft = StallEntity.clone(stall);

            return doUpdateStallPosition(draft, request, principal);
        } else if (stall.isPrimaryVersion()) {
            log.info("Finding to update draft version for stall {}", stall.getStallId());

            var draft = stallRepository.findDraftVersionById(stall.getStallId())
                    .orElseThrow(() -> new ObjectNotFoundException("Stall not found with given stall_id"));

            return doUpdateStallPosition(draft, request, principal);
        } else {
            log.info("Updating draft version for stall {}", stall.getStallId());

            return doUpdateStallPosition(stall, request, principal);
        }
    }

    private GetStallResponse doUpdateStallPosition(StallEntity stall, UpdateStallPositionRequest request, GrpcPrincipal principal) {

        this.updateStallPosition(stall, request, principal);


        var created = stallRepository.save(stall);

        var grpcStall = stallGrpcMapper.toGrpcMessage(stall, true, created.getMarket().getCode(), created.getFloor().getCode());


        return GetStallResponse.newBuilder()
                .setSuccess(true)
                .setData(GetStallResponse.Data.newBuilder()
                        .setStall(grpcStall)
                        .build())
                .build();
    }

    @Override
    public GetStallResponse getStall(GetStallRequest request, GrpcPrincipal principal) {
        Optional<StallEntity> optional = stallDslRepository.findById4Get(UUID.fromString(request.getStallId()), request.getDraft());

        if (optional.isEmpty()) {
            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Stall not found")
                            .putDetails("stall_id", "Stall not exists with given stall_id")
                            .build())
                    .build();
        }

        var stall = optional.get();

        var codeProjection = stallDslRepository.findFloorAndMarketCodeOfStall(stall.getStallId());

        var grpcStall = stallGrpcMapper.toGrpcMessage(stall, true, codeProjection.getMarketCode(), codeProjection.getFloorCode());

        return GetStallResponse.newBuilder()
                .setSuccess(true)
                .setData(GetStallResponse.Data.newBuilder()
                        .setStall(grpcStall)
                        .build())
                .build();
    }

    @Override
    public GetStallResponse getPublishedStall(FindByIdRequest request, GrpcPrincipal principal) {
        Optional<StallEntity> optional = stallRepository.findById4SubmitApplication(UUID.fromString(request.getId()));

        if (optional.isEmpty()) {
            return GetStallResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Stall not found")
                            .putDetails("stall_id", "Stall not exists with given stall_id " + request.getId())
                            .build())
                    .build();
        }

        var stall = optional.get();

        var codeProjection = stallDslRepository.findFloorAndMarketCodeOfStall(stall.getStallId());

        var grpcStall = stallGrpcMapper.toGrpcMessage(stall, true, codeProjection.getMarketCode(), codeProjection.getFloorCode());

        return GetStallResponse.newBuilder()
                .setSuccess(true)
                .setData(GetStallResponse.Data.newBuilder()
                        .setStall(grpcStall)
                        .build())
                .build();
    }

    private GetStallResponse doUpdateStallMetadata(StallEntity stall, UpdateStallMetadataRequest request, GrpcPrincipal principal) {

        this.updateStallMetadata(stall, request, principal);

        var created = stallRepository.save(stall);

        var grpcStall = stallGrpcMapper.toGrpcMessage(created, true, created.getMarket().getCode(), created.getFloor().getCode());

        return GetStallResponse.newBuilder()
                .setSuccess(true)
                .setData(GetStallResponse.Data.newBuilder()
                        .setStall(grpcStall)
                        .build())
                .build();
    }

    private boolean updateStallMetadata(StallEntity stall, UpdateStallMetadataRequest request, GrpcPrincipal principal) {
        boolean hasChanged = false;

        if (StringUtils.hasText(request.getName()) && !Objects.equals(stall.getName(), request.getName())) {
            stall.setName(request.getName());
            hasChanged = true;
        }

        if (request.hasStatus() && !Objects.equals(stall.getStatus(), request.getStatusValue())) {
            stall.setStatus(request.getStatusValue());
            hasChanged = true;
        }

        if (request.hasType() && !Objects.equals(stall.getType(), request.getTypeValue())) {
            stall.setType(request.getTypeValue());
            hasChanged = true;
        }

        if (request.hasClazz() && !Objects.equals(stall.getClazz(), request.getClazzValue())) {
            stall.setClazz(request.getClazzValue());
            hasChanged = true;
        }

        if (request.hasArea() && !Objects.equals(stall.getArea(), request.getArea())) {
            stall.setArea(request.getArea());
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean updateStallPosition(StallEntity stall, UpdateStallPositionRequest request, GrpcPrincipal principal) {
        boolean hasChanged = false;

        if (!Objects.equals(stall.getShape(), request.getShape())) {
            stall.setShape(request.getShape());
            hasChanged = true;
        }

        if (request.hasXAxis() && !Objects.equals(stall.getXAxis(), request.getXAxis())) {
            stall.setXAxis(request.getXAxis());
            hasChanged = true;
        }

        if (request.hasYAxis() && !Objects.equals(stall.getYAxis(), request.getYAxis())) {
            stall.setYAxis(request.getYAxis());
            hasChanged = true;
        }

        if (request.hasWAxis() && !Objects.equals(stall.getWAxis(), request.getWAxis())) {
            stall.setWAxis(request.getWAxis());
            hasChanged = true;
        }

        if (request.hasHAxis() && !Objects.equals(stall.getHAxis(), request.getHAxis())) {
            stall.setHAxis(request.getHAxis());
            hasChanged = true;
        }

        var points = request.getPointsList().stream()
                .map(p -> new StallPoint(p.getXAxis(), p.getYAxis()))
                .collect(Collectors.toList());
        if (!Objects.deepEquals(stall.getPoints(), points)) {
            stall.setPoints(points);
            hasChanged = true;
        }

        if (request.hasLabel() && !Objects.equals(stall.getLabel(), request.getLabel())) {
            stall.setLabel(request.getLabel());
            hasChanged = true;
        }

        if (request.hasRotate() && !Objects.equals(stall.getRotate(), request.getRotate())) {
            stall.setRotate(request.getRotate());
            hasChanged = true;
        }

        if (request.hasFontSize() && !Objects.equals(stall.getFontSize(), request.getFontSize())) {
            stall.setFontSize(request.getFontSize());
            hasChanged = true;
        }

        return hasChanged;
    }

    @Override
    @Transactional
    public NoContentResponse deleteStall(FindByIdRequest request, GrpcPrincipal principal) {
        var userId = principal.getUserId();
        var errors = requestValidator.validate(request, userId);

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        log.info("Preparing to delete stalls in batch");
        var stallId = UUID.fromString(request.getId());
        var stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new ObjectNotFoundException("Stall not found"));

        if(stall.isPrimaryVersion()){
            stallRepository.softDeleteNonDraftVersionByIds(stallId);
        } else {
            stallRepository.hardDeleteDraftVersionByIds(stallId);
        }

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public GetStallInfoResponse getStallInfo(GetStallInfoRequest request, GrpcPrincipal principal) {
        var searcher = String.format("%s%s%s", request.getSearcher().getMarketCode(), request.getSearcher().getFloorCode(), request.getSearcher().getStallCode());

        var stall = stallRepository.findByMarketCodeAndFloorCodeAndStallCode(searcher)
                .orElseThrow(() -> new ObjectNotFoundException("Stall not found with given information"));

        var grpcStall = stallGrpcMapper.toGrpcStallInfo(stall);

        return GetStallInfoResponse.newBuilder()
                .setSuccess(true)
                .setData(GetStallInfoResponse.Data.newBuilder()
                        .setStall(grpcStall)
                        .build())
                .build();
    }

    @Override
    public ListStallsInfoResponse listStallsInfo(ListStallsInfoRequest request, GrpcPrincipal principal) {
        var filter = request.getSearchersList().stream()
                .map(s -> String.join("",
                        requireNonNullElse(s.getMarketCode(), ""),
                        requireNonNullElse(s.getFloorCode(), ""),
                        requireNonNullElse(s.getStallCode(), ""))
                )
                .collect(Collectors.toSet());

        var stalls = stallRepository.findAnd4Rent(filter);

        var grpcStalls = stalls.stream()
                .map(stallGrpcMapper::toGrpcStallInfo)
                .collect(Collectors.toUnmodifiableList());

        return ListStallsInfoResponse.newBuilder()
                .setSuccess(true)
                .setData(ListStallsInfoResponse.Data.newBuilder()
                        .addAllStalls(grpcStalls)
                        .build())
                .build();
    }
}
