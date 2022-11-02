package com.srs.market.grpc.service.impl;

import com.srs.common.Error;
import com.srs.common.ErrorCode;
import com.srs.common.NoContentResponse;
import com.srs.market.*;
import com.srs.market.entity.FloorEntity;
import com.srs.market.entity.FloorStallIndexEntity;
import com.srs.market.entity.MarketEntity;
import com.srs.market.exception.ObjectNotFoundException;
import com.srs.market.grpc.mapper.FloorGrpcMapper;
import com.srs.market.grpc.mapper.StallGrpcMapper;
import com.srs.market.grpc.service.FloorGrpcService;
import com.srs.market.grpc.validator.FloorRequestValidator;
import com.srs.market.repository.*;
import com.srs.proto.dto.GrpcPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class FloorGrpcServiceImpl implements FloorGrpcService {
    private final FloorRepository floorRepository;
    private final FloorDslRepository floorDslRepository;
    private final FloorStallIndexRepository floorStallIndexRepository;
    private final StallDslRepository stallDslRepository;
    private final MarketRepository marketRepository;

    private final StallRepository stallRepository;
    private final FloorGrpcMapper floorGrpcMapper;
    private final StallGrpcMapper stallGrpcMapper;
    private final FloorRequestValidator requestValidator;


    @Override
    public CreateFloorResponse createFloor(CreateFloorRequest request, GrpcPrincipal principal) {
        var errors = requestValidator.validate(request);

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return CreateFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var marketId = UUID.fromString(request.getMarketId());

        var fetchedMarket = marketRepository.findById(marketId)
                .orElseThrow(() -> new ObjectNotFoundException("Market not found"));

        if (fetchedMarket.isDeleted()) {
            log.error("Market has been marked as deleted");
            return CreateFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot create floor")
                            .putDetails("market_id", "Market not exists with given market_id")
                            .build())
                    .build();
        }

        MarketEntity market;

        if (!fetchedMarket.isPrimaryVersion()) {
            log.info("Finding original version of market {}", request.getMarketId());

            var primaryMarket = marketRepository.findById(fetchedMarket.getPreviousVersion())
                    .orElseThrow(() -> new ObjectNotFoundException("Market not found"));

            if (primaryMarket.isDeleted()) {
                log.error("Market not found has been marked as deleted");

                return CreateFloorResponse.newBuilder()
                        .setSuccess(false)
                        .setError(Error.newBuilder()
                                .setCode(ErrorCode.CANNOT_EXECUTE)
                                .setMessage("Cannot create floor")
                                .putDetails("market_id", "Market not exists with given market_id")
                                .build())
                        .build();
            }

            market = primaryMarket;
        } else {
            market = fetchedMarket;
        }

        var floor = floorGrpcMapper.createFloor(request);
        floor.setMarket(market);

        floorRepository.save(floor);
        var floorIndex = new FloorStallIndexEntity();
        floorIndex.setFloorCode(floor.getCode());
        floorIndex.setCurrentIndex(1);

        floorStallIndexRepository.save(floorIndex);

        return CreateFloorResponse.newBuilder()
                .setSuccess(true)
                .setData(CreateFloorResponse.Data.newBuilder()
                        .setMarketId(market.getMarketId().toString())
                        .setFloorplanId(floor.getFloorId().toString())
                        .build())
                .build();
    }

    @Override
    public UpdateFloorResponse updateFloor(UpdateFloorRequest request, GrpcPrincipal principal) {
        var errors = requestValidator.validate(request);

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return UpdateFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var floorId = UUID.fromString(request.getFloorplanId());

        var fetchedFloor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ObjectNotFoundException("Floor not found"));

        if (fetchedFloor.isDeleted()) {
            log.error("Floor has been marked as deleted");

            return UpdateFloorResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot update floor")
                            .putDetails("floorplan_id", "Floor not exists with given floorplan_id").build())
                    .build();
        }

        if (fetchedFloor.isPrimaryVersion() && !floorRepository.existsByPreviousVersion(fetchedFloor.getFloorId())) {
            log.info("Creating draft version for floor {}", fetchedFloor.getFloorId());

            var draft = floorGrpcMapper.cloneFloor(fetchedFloor);

            return this.doUpdateFloorMetadata(draft, request, principal);
        } else if (fetchedFloor.isPrimaryVersion()) {
            log.info("Finding to update draft version for floor {}", fetchedFloor.getFloorId());

            var draft = floorRepository.findDraftVersionById(fetchedFloor.getFloorId())
                    .orElseThrow(() -> new ObjectNotFoundException("Floor not found"));

            return this.doUpdateFloorMetadata(draft, request, principal);
        } else {
            log.info("Updating draft version for floor {}", fetchedFloor.getPreviousVersion());

            return this.doUpdateFloorMetadata(fetchedFloor, request, principal);
        }
    }

    @Override
    public GetFloorResponse getFloor(GetFloorRequest request, GrpcPrincipal principal) {
        var floorId = UUID.fromString(request.getFloorplanId());

        var floor = floorDslRepository.findOneById(floorId, request.getDraft());

        if (floor == null) {
            throw new ObjectNotFoundException("Floor not found");
        }

        if (floor.isDeleted()) {
            log.warn("Floor has been marked as deleted");
            throw new ObjectNotFoundException("Floor not found");
        }

        var primaryFloorId = floor.isPrimaryVersion() ? floor.getFloorId() : floor.getPreviousVersion();

        var stalls = stallDslRepository.findAllByFloorIdAndDraft(primaryFloorId, request.getDraft()).stream()
                .map(stall -> stallGrpcMapper.toGrpcMessage(stall, true))
                .collect(Collectors.toList());

        var grpcFloor = floorGrpcMapper.toGrpcBuilder(floor)
                .addAllStalls(stalls)
                .setTotalStalls(stalls.size())
                .build();

        return GetFloorResponse.newBuilder()
                .setSuccess(true)
                .setData(GetFloorResponse.Data.newBuilder()
                        .setFloor(grpcFloor)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public NoContentResponse deleteFloor(DeleteFloorRequest request, GrpcPrincipal principal) {
        var id = UUID.fromString(request.getFloorplanId());

        var floors = floorRepository.findAllById4Delete(id);

        if (floors.isEmpty()) {
            log.warn("No floor found with id {}", id);
            return NoContentResponse.newBuilder()
                    .setSuccess(true)
                    .build();
        }

        log.info("Preparing to delete floor");

        var marketCode = floors.get(0).getMarket().getCode();

        if (floors.size() == 1) {
            log.info("There is only 1 version for floor id {}", id);
            log.warn("Deleting floor");

            var floor = floors.get(0);


            floorRepository.softDeleteById(id);
            stallRepository.softDeleteNonDraftVersionByFloorId(id);
            stallRepository.hardDeleteDraftVersionByFloorId(id);
        } else if (floors.size() == 2) {
            log.info("There are 2 versions for floor id {}", id);
            UUID draftId, primaryId;
            if (floors.get(0).isPrimaryVersion()) {
                primaryId = floors.get(0).getFloorId();
                draftId = floors.get(1).getFloorId();
            } else {
                primaryId = floors.get(1).getFloorId();
                draftId = floors.get(0).getFloorId();
            }

            if (request.getDraftOnly()) {
                log.info("Deleting draft version only");
            } else {
                log.warn("Deleting both versions of floor");
                floorRepository.softDeleteById(primaryId);
                stallRepository.softDeleteNonDraftVersionByFloorIdId(primaryId);
            }

            floorRepository.deleteById(draftId);
            stallRepository.hardDeleteDraftVersionByFloorId(primaryId);
        } else {
            log.error("Floor {} has more than one draft version", id);
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot delete floor")
                            .putDetails("floorplan_id", "Floor has more than one draft version")
                            .build())
                    .build();
        }

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public ListFloorsResponse listFloors(ListFloorsRequest request, GrpcPrincipal principal) {
        var marketId = UUID.fromString(request.getMarketId());
        var market = marketRepository.findByMarketIdAndDeletedIsFalse(marketId)
                .orElseThrow(() -> new ObjectNotFoundException("Market not found with id " + request.getMarketId()));

        if (!market.isPrimaryVersion()) {
            log.info("Market ID is of draft version. Getting the primary one");
            market = marketRepository.findByMarketIdAndDeletedIsFalse(market.getPreviousVersion())
                    .orElseThrow(() -> new ObjectNotFoundException("Market not found with id " + request.getMarketId()));
        }

        var floors = floorDslRepository.findAllByMarketId4List(market.getMarketId(), request.getDraft());

        return asGrpcFloorList(market, floors, request.getDraft());
    }

    private UpdateFloorResponse doUpdateFloorMetadata(FloorEntity floor, UpdateFloorRequest request, GrpcPrincipal principal) {

        this.updateFloorMetadata(floor, request);

        var created = floorRepository.save(floor);


        return UpdateFloorResponse.newBuilder()
                .setSuccess(true)
                .setData(UpdateFloorResponse.Data.newBuilder()
                        .setFloorplanId(created.getFloorId().toString()))
                .build();
    }

    private boolean updateFloorMetadata(FloorEntity floor, UpdateFloorRequest request) {
        boolean hasChanged = false;

        if (StringUtils.hasText(request.getName()) && !Objects.equals(floor.getName(), request.getName())) {
            floor.setName(request.getName());
            hasChanged = true;
        }

        if (StringUtils.hasText(request.getImage()) && !Objects.equals(floor.getImage(), request.getImage())) {
            floor.setImage(request.getImage());
            hasChanged = true;
        }

        return hasChanged;
    }

    private ListFloorsResponse asGrpcFloorList(MarketEntity market, List<FloorEntity> floors, boolean draft) {
        var stallCount = new HashMap<UUID /*floorId*/, Long /*count*/>();
        for (var s : floorDslRepository.countStallsByMarketIdGroupByFloor(market.getMarketId(), draft)) {
            stallCount.put(s.get(0, UUID.class), s.get(1, Long.class));
        }

        var stallHasDetailCount = new HashMap<UUID /*floorId*/, Long /*count*/>();
        for (var s : floorDslRepository.countStallsHasDetailByMarketIdGroupByFloor(market.getMarketId(), draft)) {
            stallHasDetailCount.put(s.get(0, UUID.class), s.get(1, Long.class));
        }

        var grpcFloors = new ArrayList<Floor>();
        for (var floor : floors) {
            var floorId = floor.getFloorId();

            grpcFloors.add(floorGrpcMapper.toGrpcBuilder(floor)
                    .setTotalStalls(stallCount.getOrDefault(floorId, 0L))
                    .setStallWithDetail(stallHasDetailCount.getOrDefault(floorId, 0L))
                    .build());
        }

        return ListFloorsResponse.newBuilder()
                .setSuccess(true)
                .setData(ListFloorsResponse.Data.newBuilder()
                        .addAllFloors(grpcFloors)
                        .build())
                .build();
    }
}
