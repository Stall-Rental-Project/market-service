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
                .withName("")
                .withStatus(StallStatus.STALL_STATUS_INACTIVE_VALUE)
                .withType(StallType.STALL_TYPE_TEMPORARY_VALUE)
                .withState(StallState.STALL_STATE_UNPUBLISHED_VALUE)
                .withXAxis(request.getX())
                .withYAxis(request.getY())
                .withWAxis(request.getWidth())
                .withHAxis(request.getHeight())
                .withRotate(request.getRotation())
                .withFloor(floor)
                .withMarket(floor.getMarket())
                .withLeaseStatus(StallLeaseStatus.STALL_AVAILABLE_VALUE)
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
    public GetStallResponse updateStallPosition(CreateStallRequest request, GrpcPrincipal principal) {
        var userId = principal.getUserId();

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

    private GetStallResponse doUpdateStallPosition(StallEntity stall, CreateStallRequest request, GrpcPrincipal principal) {

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
        stall.setUpdatedDetail(true);
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

        if (StringUtils.hasText(request.getStallName()) && !Objects.equals(stall.getName(), request.getStallName())) {
            stall.setName(request.getStallName());
            hasChanged = true;
        }

        if (request.hasStallStatus() && !Objects.equals(stall.getStatus(), request.getStallStatus())) {
            stall.setStatus(request.getStallStatusValue());
            hasChanged = true;
        }

        if (request.hasStallType() && !Objects.equals(stall.getType(), request.getStallType())) {
            stall.setType(request.getStallTypeValue());
            hasChanged = true;
        }

        if (request.hasStallClass() && !Objects.equals(stall.getClazz(), request.getStallClass())) {
            stall.setClazz(request.getStallClassValue());
            hasChanged = true;
        }

        if (request.hasArea() && !Objects.equals(stall.getArea(), request.getArea())) {
            stall.setArea(request.getArea());
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean updateStallPosition(StallEntity stall, CreateStallRequest request, GrpcPrincipal principal) {
        boolean hasChanged = false;


        if (request.hasX() && !Objects.equals(stall.getXAxis(), request.getX())) {
            stall.setXAxis(request.getX());
            hasChanged = true;
        }

        if (request.hasY() && !Objects.equals(stall.getYAxis(), request.getY())) {
            stall.setYAxis(request.getY());
            hasChanged = true;
        }

        if (request.hasWidth() && !Objects.equals(stall.getWAxis(), request.getWidth())) {
            stall.setWAxis(request.getWidth());
            hasChanged = true;
        }

        if (request.hasHeight() && !Objects.equals(stall.getHAxis(), request.getHeight())) {
            stall.setHAxis(request.getHeight());
            hasChanged = true;
        }

        if (request.hasRotation() && !Objects.equals(stall.getRotate(), request.getRotation())) {
            stall.setRotate(request.getRotation());
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
        Collection<UUID> stallIds=new ArrayList<>();
        var stallId=UUID.fromString(request.getId());
        stallIds.add(stallId);

        stallIds.addAll(stallRepository.findAllPrimaryIdsByDraftIds(stallIds));

        var stalls = stallDslRepository.findAllById4Delete(stallIds);



        Set<UUID> softDeletedIds = new HashSet<>();
        Set<UUID> hardDeletedIds = new HashSet<>();

        for (var stall : stalls) {
            if (stall.isPrimaryVersion()) {
                softDeletedIds.add(stall.getStallId());
            } else {
                hardDeletedIds.add(stall.getStallId());
            }
        }
        stallRepository.softDeleteNonDraftVersionByIds(softDeletedIds);


        stallRepository.hardDeleteDraftVersionByIds(hardDeletedIds);

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
