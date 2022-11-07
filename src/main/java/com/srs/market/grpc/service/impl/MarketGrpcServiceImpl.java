package com.srs.market.grpc.service.impl;

import com.google.protobuf.Any;
import com.srs.common.Error;
import com.srs.common.*;
import com.srs.market.*;
import com.srs.market.common.Constant;
import com.srs.market.entity.*;
import com.srs.market.exception.ObjectNotFoundException;
import com.srs.market.grpc.mapper.LocationGrpcMapper;
import com.srs.market.grpc.mapper.MarketGrpcMapper;
import com.srs.market.grpc.mapper.SupervisorGrpcMapper;
import com.srs.market.grpc.service.MarketGrpcService;
import com.srs.market.grpc.util.PageUtil;
import com.srs.market.grpc.validator.MarketRequestValidator;
import com.srs.market.repository.*;
import com.srs.market.util.MarketUtil;
import com.srs.proto.dto.GrpcPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class MarketGrpcServiceImpl implements MarketGrpcService {
    private final MarketRepository marketRepository;
    private final LocationRepository locationRepository;
    private final SupervisorRepository supervisorRepository;
    private final StallRepository stallRepository;

    private final FloorRepository floorRepository;
    private final MarketDslRepository marketDslRepository;
    private final LocationDslRepository locationDslRepository;
    private final StallDslRepository stallDslRepository;

    private final MarketGrpcMapper marketGrpcMapper;
    private final SupervisorGrpcMapper supervisorGrpcMapper;
    private final LocationGrpcMapper locationGrpcMapper;

    private final MarketRequestValidator marketRequestValidator;

    private final MarketUtil marketUtil;

    @Override
    public PageResponse listMarkets(ListMarketsRequest request, GrpcPrincipal principal) {
        var pageResponse = marketDslRepository.listMarkets(request);

        var marketIds = pageResponse.getItems().stream()
                .map(MarketEntity::getMarketId)
                .collect(Collectors.toSet());

        var locationIds = new HashSet<UUID>();
        var marketMap = new HashMap<UUID, UUID>();

        for (var market : pageResponse.getItems()) {
            locationIds.add(market.getLocation().getLocationId());
            marketMap.put(market.getMarketId(), market.getLocation().getLocationId());
        }

        var locationMap = new HashMap<UUID, LocationEntity>();
        var locations = locationRepository.findAllByIds(locationIds);
        for (var location : locations) {
            locationMap.put(location.getLocationId(), location);
        }

        var hasDraftMarketIds = marketRepository.findAllHasDraftIn(marketIds);

        var markets = new ArrayList<Any>();

        for (var market : pageResponse.getItems()) {
            var hasDraft = hasDraftMarketIds.contains(market.getMarketId());
            var locationId = marketMap.get(market.getMarketId());
            var location = locationMap.get(locationId);
            var grpcMarket = marketGrpcMapper.toGrpcBuilder(market, hasDraft, false)
                    .setLocation(locationGrpcMapper.toGrpcMessage(location, market.getAddress()))
                    .setFullAddress(marketUtil.getFullAddress(market, location))
                    .build();

            markets.add(Any.pack(grpcMarket));
        }

        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.MARKET_SORTS);

        return PageResponse.newBuilder()
                .setSuccess(true)
                .setData(PageResponse.Data.newBuilder()
                        .addAllItems(markets)
                        .setPage(request.getAll() ? 1 : pageRequest.getPage())
                        .setSize(Math.toIntExact(request.getAll() ? pageResponse.getTotal() : pageRequest.getSize()))
                        .setTotalElements(pageResponse.getTotal())
                        .setTotalPages(request.getAll() ? 1 : PageUtil.calcTotalPages(pageResponse.getTotal(), pageRequest.getSize()))
                        .build())
                .build();
    }

    @Override
    public GetMarketResponse getMarket(GetMarketRequest request, GrpcPrincipal principal) {

        List<MarketEntity> markets;

        if (StringUtils.hasText(request.getMarketId())) {
            var marketId = UUID.fromString(request.getMarketId());
            markets = marketRepository.findAllByIdFetchLocationAndSupervisor(marketId);
        } else if (StringUtils.hasText(request.getCode())) {
            var marketCode = request.getCode();
            markets = marketRepository.findAllByCodeFetchLocationAndSupervisor(marketCode);
        } else {
            throw new IllegalArgumentException("No data was given for getting market");
        }

        if (markets.isEmpty()) {
            return GetMarketResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Market not found")
                            .putDetails("market_id", "Market not exists with given market_id")
                            .build())
                    .build();
        }

        MarketEntity returned;
        boolean hasDraft;

        if (markets.size() == 2) {
            int primaryIndex = markets.get(0).isPrimaryVersion() ? 0 : 1;
            int draftIndex = markets.get(0).isPrimaryVersion() ? 1 : 0;

            returned = request.getDraft() ? markets.get(draftIndex) : markets.get(primaryIndex);
            hasDraft = true;
        } else {
            returned = markets.get(0);
            hasDraft = false;
        }

        if (returned.isDeleted()) {
            log.warn("From getMarket(): Market has been marked as deleted");

            return GetMarketResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Market not found")
                            .putDetails("market_id", "Market not exists with given market_id")
                            .build())
                    .build();
        }

        var grpcMarket = marketGrpcMapper.toGrpcBuilder(returned, hasDraft, true);

        supervisorRepository.findByMarketId(returned.getMarketId())
                .ifPresent(supervisor -> grpcMarket.setSupervisor(supervisorGrpcMapper.toGrpcMessage(supervisor)));


        return GetMarketResponse.newBuilder()
                .setSuccess(true)
                .setData(GetMarketResponse.Data.newBuilder()
                        .setMarket(grpcMarket)
                        .build())
                .build();

    }

    @Override
    @Transactional
    public OnlyIdResponse createMarket(UpsertMarketRequest request, GrpcPrincipal principal) {
        var errors = marketRequestValidator.validate(request, principal.getUserId());

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return OnlyIdResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var province = request.getLocation().getProvince();
        var city = request.getLocation().getCity();
        var ward = request.getLocation().getWard();

        var location = locationRepository.findLocation(province, city, ward)
                .orElseThrow(() -> new ObjectNotFoundException("Location not found"));

        var market = marketGrpcMapper.createMarket(request);
        market.setLocation(location);

        marketRepository.save(market);

        if (request.hasSupervisor()) {
            var supervisor = supervisorGrpcMapper.createSupervisor(request.getSupervisor());
            supervisor.setMarket(market);
            supervisorRepository.save(supervisor);
        }

        return OnlyIdResponse.newBuilder()
                .setSuccess(true)
                .setData(OnlyIdResponse.Data.newBuilder()
                        .setId(market.getMarketId().toString())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public UpdateMarketResponse updateMarket(UpsertMarketRequest request, GrpcPrincipal principal) {
        var errors = marketRequestValidator.validate(request, principal.getUserId());

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return UpdateMarketResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var marketId = UUID.fromString(request.getMarketId());

        var market = marketRepository.findByIdFetchLocationAndSupervisor(marketId)
                .orElseThrow(() -> new ObjectNotFoundException("Market not found"));

        if (market.isDeleted()) {
            return UpdateMarketResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot update market")
                            .putDetails("market_id", "Market not exists with given market_id")
                            .build())
                    .build();
        }

        if (!market.isPrimaryVersion()) {
            log.info("Updating draft version for market {}", market.getPreviousVersion());
            return this.doUpdateMarket(market, principal, request);
        } else {
            var draft = marketRepository.findDraftVersionByIdFetchLocationAndSupervisor(market.getMarketId()).orElse(null);

            if (draft == null) { // Market currently has no draft version
                log.info("Creating draft version for market {}", market.getMarketId());

                draft = new MarketEntity();
                BeanUtils.copyProperties(market, draft, "marketId", "supervisor", "floors", "stalls");
                draft.setPreviousVersion(market.getMarketId());
                draft.setState(MarketState.MARKET_STATE_UNPUBLISHED_VALUE);

            } else { // Given ID belongs to the primary version
                log.info("Finding to update draft version for market {}", market.getMarketId());
            }

            return this.doUpdateMarket(draft, principal, request);
        }
    }

    @Override
    @Transactional
    public NoContentResponse deleteMarket(FindByIdRequest request, GrpcPrincipal principal) {
        var errors = marketRequestValidator.validate(request, principal.getUserId());

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var marketId = UUID.fromString(request.getId());

        var markets = marketRepository.findAllVersionsById(marketId);

        if (markets.isEmpty()) {
            log.warn("No market found with id {}", marketId);
            return NoContentResponse.newBuilder()
                    .setSuccess(true)
                    .build();
        }

        log.info("Preparing to delete market");

        var primary = this.getPrimaryMarket(markets);
        var draft = this.getDraftMarket(markets);


        if (draft == null) {
            log.info("There is only 1 version for market id {}", marketId);
            log.warn("Soft-deleting market {}", marketId);

            primary.setDeleted(true);
            primary.setStatus(MarketStatus.MARKET_STATUS_INACTIVE_VALUE);

            marketRepository.save(primary);
        } else {
            log.warn("Deleting both versions of market");
            primary.setDeleted(true);
            primary.setStatus(MarketStatus.MARKET_STATUS_INACTIVE_VALUE);
            marketRepository.save(primary);
            marketRepository.delete(draft);
        }
        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();

    }

    private MarketEntity getPrimaryMarket(List<MarketEntity> markets) {
        if (CollectionUtils.isEmpty(markets)) {
            throw new ObjectNotFoundException("Market not found");
        } else if (markets.size() == 1) {
            var market = markets.get(0);
            if (market.isPrimaryVersion()) {
                return market;
            } else {
                log.info("Getting primary but found draft. Search in DB...");
                return marketRepository.findPrimaryVersionById(market.getMarketId())
                        .orElseThrow(() -> new ObjectNotFoundException("Market not found"));
            }
        } else if (markets.size() == 2) {
            return markets.get(0).isPrimaryVersion() ? markets.get(0) : markets.get(1);
        } else {
            throw new IllegalStateException("Market code " + markets.get(0).getCode() + " has more than 2 versions");
        }
    }

    private MarketEntity getDraftMarket(List<MarketEntity> markets) {
        if (CollectionUtils.isEmpty(markets)) {
            throw new ObjectNotFoundException("Market not found");
        } else if (markets.size() == 1) {
            var market = markets.get(0);
            if (market.isPrimaryVersion()) {
                log.info("Getting draft but found primary. Search in DB...");
                return marketRepository.findDraftVersionByIdFetchLocationAndSupervisor(market.getMarketId()).orElse(null);
            } else {
                return market;
            }
        } else if (markets.size() == 2) {
            return markets.get(0).isPrimaryVersion() ? markets.get(1) : markets.get(0);
        } else {
            throw new IllegalStateException("Market code " + markets.get(0).getCode() + " has more than 2 versions");
        }
    }

    private UpdateMarketResponse doUpdateMarket(MarketEntity market, GrpcPrincipal principal, UpsertMarketRequest request) {

        this.updateMarketInformation(market, request);

        if (request.hasSupervisor()) {
            this.updateMarketSupervisor(market, request.getSupervisor());
        }

        var updated = marketRepository.save(market);

        return UpdateMarketResponse.newBuilder()
                .setSuccess(true)
                .setData(UpdateMarketResponse.Data.newBuilder()
                        .setMarketId(updated.getMarketId().toString())
                        .build())
                .build();
    }

    private boolean updateMarketInformation(MarketEntity market, UpsertMarketRequest request) {
        var hasChanged = false;

        if (request.hasType() && !Objects.equals(market.getType(), request.getTypeValue())) {
            if (market.getState() == MarketState.MARKET_STATE_PUBLISHED_VALUE) {
                throw new IllegalArgumentException("Cannot change type for market has already been published");
            }
        }

        if (request.hasName() && !Objects.equals(market.getName(), request.getName())) {
            market.setName(request.getName());
            hasChanged = true;
        }

        if (request.hasStatus() && !Objects.equals(market.getStatus(), request.getStatusValue())) {
            market.setStatus(request.getStatusValue());
            hasChanged = true;
        }

        if (request.hasLocation()) {
            var location = locationDslRepository.findByProvinceAndCityAndWard(
                            request.getLocation().getProvince(),
                            request.getLocation().getCity(),
                            request.getLocation().getWard())
                    .orElseThrow(() -> new ObjectNotFoundException("Location not found"));

            if (!Objects.equals(request.getLocation().getAddress(), market.getAddress())
                    || !Objects.equals(location.getLocationId(), market.getLocation().getLocationId())) {
                hasChanged = true;
            }

            market.setLocation(location);
            market.setAddress(request.getLocation().getAddress());
        }

        if (request.hasGoogleMap() && !Objects.equals(market.getGoogleMap(), request.getGoogleMap())) {
            market.setGoogleMap(request.getGoogleMap());
            hasChanged = true;
        }

        switch (request.getType()) {
            case MARKET_TYPE_PUBLIC:
                hasChanged = this.updatePublicFields(market, request) || hasChanged;
                break;
            default:
        }

        return hasChanged;
    }

    private boolean updatePublicFields(MarketEntity market, UpsertMarketRequest request) {
        var hasChanged = false;

        if (request.hasClazz() && !Objects.equals(market.getClazz(), request.getClazzValue())) {
            market.setClazz(request.getClazzValue());
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean updateMarketSupervisor(MarketEntity market, Supervisor request) {
        SupervisorEntity supervisor;

        if (!market.isPrimaryVersion()) {
            if (market.getSupervisor() == null) {
                var primaryMarket = marketRepository.findByIdFetchLocationAndSupervisor(market.getPreviousVersion())
                        .orElseThrow(() -> new ObjectNotFoundException("Primary version of market " + market.getMarketId() + " not found"));

                supervisor = new SupervisorEntity();
                BeanUtils.copyProperties(primaryMarket.getSupervisor(), supervisor, "supervisorId", "market");
                supervisor.setMarket(market);
            } else {
                supervisor = market.getSupervisor();
            }
        } else {
            supervisor = market.getSupervisor();
        }

        var hasChanged = false;

        var firstName = request.getFirstName();
        if (!Objects.equals(firstName, supervisor.getFirstName())) {
            supervisor.setFirstName(firstName);
            hasChanged = true;
        }

        var middleName = request.getMiddleName();
        if (!Objects.equals(middleName, supervisor.getMiddleName())) {
            supervisor.setMiddleName(middleName);
            hasChanged = true;
        }
        var lastName = request.getLastName();
        if (!Objects.equals(lastName, supervisor.getLastName())) {
            supervisor.setLastName(lastName);
            hasChanged = true;
        }

        var position = request.getPosition();
        if (!Objects.equals(position, supervisor.getPosition())) {
            supervisor.setPosition(position);
            hasChanged = true;
        }

        var email = request.getEmail();
        if (!Objects.equals(email, supervisor.getEmail())) {
            supervisor.setEmail(email);
            hasChanged = true;
        }

        var mobile = request.getMobilePhone();
        if (!Objects.equals(mobile, supervisor.getMobilePhone())) {
            supervisor.setMobilePhone(mobile);
            hasChanged = true;
        }

        var phone = request.getTelephone();
        if (!Objects.equals(phone, supervisor.getTelephone())) {
            supervisor.setTelephone(phone);
            hasChanged = true;
        }

        supervisorRepository.save(supervisor);

        return hasChanged;
    }

    @Override
    public CountStallsResponse countStalls(FindByIdRequest request, GrpcPrincipal principal) {
        var marketId = UUID.fromString(request.getId());
        var markets = marketRepository.findAllVersionsById(marketId);

        var market = this.getPrimaryMarket(markets);

        var stallLeaseStatuses = stallDslRepository.findAllStatusOfStallsInMarket(market.getMarketId());

        var stallCounter = marketUtil.countAndGroupStallsByLeaseStatus(stallLeaseStatuses);

        var totalStalls = stallCounter.getTotalStalls();
        var availableStalls = stallCounter.getAvailableStalls();
        var occupiedStalls = stallCounter.getOccupiedStalls();

        return CountStallsResponse.newBuilder()
                .setSuccess(true)
                .setData(CountStallsResponse.Data.newBuilder()
                        .setTotalStalls(totalStalls)
                        .setAvailableStalls(availableStalls)
                        .setOccupiedStalls(occupiedStalls)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public NoContentResponse publishMarket(FindByIdRequest request, GrpcPrincipal principal) {

        var errors = marketRequestValidator.validate(request, principal.getUserId());

        if (!ErrorCode.SUCCESS.equals(errors.getCode())) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(errors)
                    .build();
        }

        var marketId = UUID.fromString(request.getId());

        var markets = marketRepository.findAllVersionsById(marketId);

        if (markets.isEmpty() || markets.stream().allMatch(MarketEntity::isDeleted)) {
            if (markets.size() > 0 && markets.stream().allMatch(MarketEntity::isDeleted)) {
                log.error("Market has been marked as deleted");
            }

            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.CANNOT_EXECUTE)
                            .setMessage("Cannot publish market")
                            .putDetails("market_id", "Market not exists with given market_id")
                            .build())
                    .build();
        }

        log.info("Preparing to publish market");

        var primary = this.getPrimaryMarket(markets);
        var draft = this.getDraftMarket(markets);

        if (primary.getState() == MarketState.MARKET_STATE_PUBLISHED_VALUE) {
            doPublishMarket(primary, draft);
        } else {
           doPublishMarketFirstTime(primary,draft);
        }

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    private void doPublishMarket(MarketEntity primary, MarketEntity draft) {
        Map<UUID, StallEntity> publishedStalls = new HashMap<>();
        Map<UUID, StallEntity> deletedStalls = new HashMap<>();

        Map<UUID, FloorEntity> publishedFloors = new HashMap<>();
        Map<UUID, FloorEntity> deletedFloors = new HashMap<>();

        var floors = floorRepository.findAllByMarketId(primary.getMarketId());
        var floorMap = new HashMap<UUID, FloorEntity>();
        var primaryToDraftFloorIds = new HashMap<UUID, UUID>();
        for (var floor : floors) {
            var floorId = floor.getFloorId();
            floorMap.put(floorId, floor);
            if (floor.getPreviousVersion() != null) {
                primaryToDraftFloorIds.put(floor.getPreviousVersion(), floorId);
            }

            floor.setMarket(draft);

        }

        var stalls = stallRepository.findAllByMarketId(primary.getMarketId());
        var stallMap = new HashMap<UUID, StallEntity>();
        var primaryToDraftStallIds = new HashMap<UUID, UUID>();
        var floorStallMap = new HashMap<UUID, List<StallEntity>>();
        for (var stall : stalls) {
            var stallId = stall.getStallId();
            stallMap.put(stallId, stall);

            if (stall.getPreviousVersion() != null) {
                primaryToDraftStallIds.put(stall.getPreviousVersion(), stallId);
            }

            var floorId = stall.getFloor().getFloorId();
            if (!floorStallMap.containsKey(floorId)) {
                floorStallMap.put(floorId, new ArrayList<>());
            }
            floorStallMap.get(floorId).add(stall);

            stall.setMarket(draft);
        }


        for (var floorEntity : floors) {
            var floorId = floorEntity.getFloorId();
            var floor = floorMap.get(floorId);

            if (floor == null) {
                throw new ObjectNotFoundException("Floor " + floorId + " not found");
            }

            var draftFloorId = primaryToDraftFloorIds.get(floorId);
            var draftFloor = floorMap.get(draftFloorId);

            var floorStalls = floorStallMap.get(floorId);

            if (!CollectionUtils.isEmpty(floorStalls)) {
                for (var stall : floorStalls) {
                    if (primaryToDraftStallIds.containsKey(stall.getStallId())) {
                        deletedStalls.put(stall.getStallId(), stall);
                    } else {
                        stall.setState(StallState.STALL_STATE_PUBLISHED_VALUE);
                        stall.setPublishedAtLeastOnce(true);
                        stall.setPreviousVersion(null);
                        stall.setClonedFrom(null);
                        if (draftFloor != null) {
                            stall.setFloor(draftFloor);
                        }
                        stall.setMarket(draft);

                        if (!deletedStalls.containsKey(stall.getStallId())) {
                            publishedStalls.put(stall.getStallId(), stall);
                        }
                    }
                }
            }

            if (draftFloor != null) {
                draftFloor.setState(FloorState.FLOOR_STATE_PUBLISHED_VALUE);
                draftFloor.setPublishedAtLeastOnce(true);
                draftFloor.setPreviousVersion(null);
                draftFloor.setMarket(draft);

                publishedFloors.put(draftFloor.getFloorId(), draftFloor);
                deletedFloors.put(floor.getFloorId(), floor);
            } else {
                floor.setState(FloorState.FLOOR_STATE_PUBLISHED_VALUE);
                floor.setPublishedAtLeastOnce(true);
                floor.setMarket(draft);
                publishedFloors.put(floor.getFloorId(), floor);
            }

        }

        for (var stallEntity : stalls) {
            var stallId = stallEntity.getStallId();
            var stall = stallMap.get(stallId);

            if (stall == null) {
                throw new ObjectNotFoundException("Stall " + stallId + " not found");
            }

            var draftStallId = primaryToDraftStallIds.get(stallId);
            var draftStall = stallMap.get(draftStallId);

            if (draftStall != null) {
                draftStall.setState(StallState.STALL_STATE_PUBLISHED_VALUE);
                draftStall.setPublishedAtLeastOnce(true);
                draftStall.setPreviousVersion(null);
                draftStall.setClonedFrom(null);
                draftStall.setMarket(draft);

                publishedStalls.put(draftStall.getStallId(), draftStall);
                deletedStalls.put(stall.getStallId(), stall);
            } else {
                stall.setState(StallState.STALL_STATE_PUBLISHED_VALUE);
                stall.setPublishedAtLeastOnce(true);
                stall.setClonedFrom(null);
                stall.setMarket(draft);

                publishedStalls.put(stall.getStallId(), stall);
            }
        }


        draft.setState(MarketState.MARKET_STATE_PUBLISHED_VALUE);
        draft.setPreviousVersion(null);

        log.info(String.format(
                "Delete: %d stalls, %d floors. Publish: %d stalls, %d floors",
                deletedStalls.size(), deletedFloors.size(),
                publishedStalls.size(), publishedFloors.size()
        ));


        var otherFloors = floors.stream()
                .filter(f -> !publishedFloors.containsKey(f.getFloorId()))
                .filter(f -> !deletedFloors.containsKey(f.getFloorId()))
                .collect(Collectors.toList());

        floorRepository.saveAll(otherFloors);

        var otherStalls = stalls.stream()
                .filter(s -> !publishedStalls.containsKey(s.getStallId()))
                .filter(s -> !deletedStalls.containsKey(s.getStallId()))
                .collect(Collectors.toList());
        stallRepository.saveAll(otherStalls);


        floorRepository.saveAll(publishedFloors.values());
        stallRepository.saveAll(publishedStalls.values());

        floorRepository.deleteAll(deletedFloors.values());
        stallRepository.deleteAll(deletedStalls.values());

        log.info("There are updates on market. Promoting new market version");
        marketRepository.save(draft);
        marketRepository.delete(primary);
    }

    private void doPublishMarketFirstTime(MarketEntity primary,MarketEntity draft){
        var floors = floorRepository.findAllByMarketId(primary.getMarketId());

        var primaryFloors = new HashMap<UUID, FloorEntity>();
        var draftFloors = new HashMap<UUID, FloorEntity>();
        var originFloors = new HashMap<UUID, FloorEntity>();
        var primaryToDraftFloorIds = new HashMap<UUID /*primary*/, UUID /*draftId*/>();

        for (var floor : floors) {
            if (!floor.isPrimaryVersion()) {
                draftFloors.put(floor.getFloorId(), floor);
                primaryFloors.put(floor.getPreviousVersion(), null);
                primaryToDraftFloorIds.put(floor.getPreviousVersion(), floor.getFloorId());
            }
        }

        for (var floor : floors) {
            if (primaryFloors.containsKey(floor.getFloorId())) {
                primaryFloors.put(floor.getFloorId(), floor);
            } else {
                originFloors.put(floor.getFloorId(), floor);
            }
        }

        var publishedFloors = new ArrayList<FloorEntity>();
        publishedFloors.addAll(draftFloors.values());
        publishedFloors.addAll(originFloors.values());

        var stalls = stallRepository.findAllByMarketId(primary.getMarketId());

        var primaryStalls = new HashMap<UUID, StallEntity>();
        var draftStalls = new HashMap<UUID, StallEntity>();
        var originStalls = new HashMap<UUID, StallEntity>();

        for (var stall : stalls) {
            if (!stall.isPrimaryVersion()) {
                draftStalls.put(stall.getStallId(), stall);
                primaryStalls.put(stall.getPreviousVersion(), null);
            }
        }

        for (var stall : stalls) {
            if (primaryStalls.containsKey(stall.getStallId())) {
                primaryStalls.put(stall.getStallId(), stall);
            } else {
                originStalls.put(stall.getStallId(), stall);
            }
        }

        var publishedStalls = new ArrayList<StallEntity>();
        publishedStalls.addAll(draftStalls.values());
        publishedStalls.addAll(originStalls.values());

        for (var stall : publishedStalls) {
            stall.setState(StallState.STALL_STATE_PUBLISHED_VALUE);
            stall.setPublishedAtLeastOnce(true);
            if (primaryToDraftFloorIds.containsKey(stall.getFloor().getFloorId())) {
                var primaryFloorId = stall.getFloor().getFloorId();
                var draftFloorId = primaryToDraftFloorIds.get(primaryFloorId);
                var draftFloor = draftFloors.get(draftFloorId);
                stall.setFloor(draftFloor);
            }
            if (draft != null) {
                stall.setMarket(draft);
            }
        }
        stallRepository.saveAll(publishedStalls);
        stallRepository.deleteAll(primaryStalls.values());

        for (var floor : publishedFloors) {
            floor.setState(FloorState.FLOOR_STATE_PUBLISHED_VALUE);
            floor.setPublishedAtLeastOnce(true);
            if (draft != null) {
                floor.setMarket(draft);
            }
        }
        floorRepository.saveAll(publishedFloors);
        floorRepository.deleteAll(primaryFloors.values());

        if (draft != null) {
            draft.setPreviousVersion(null);
            draft.setState(MarketState.MARKET_STATE_PUBLISHED_VALUE);
            marketRepository.save(draft);
            marketRepository.delete(primary);
        } else {
            primary.setState(MarketState.MARKET_STATE_PUBLISHED_VALUE);
            marketRepository.save(primary);
        }
    }
}
