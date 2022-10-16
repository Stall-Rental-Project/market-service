package com.srs.market.grpc.service.impl;

import com.google.protobuf.Any;
import com.srs.common.Error;
import com.srs.common.*;
import com.srs.market.*;
import com.srs.market.common.Constant;
import com.srs.market.entity.LocationEntity;
import com.srs.market.entity.MarketEntity;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private final MarketDslRepository marketDslRepository;
    private final LocationDslRepository locationDslRepository;


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
    public UpdateMarketResponse updateMarket(UpsertMarketRequest request, GrpcPrincipal principal) {
        return null;
    }

    @Override
    public NoContentResponse deleteMarket(FindByIdRequest request, GrpcPrincipal principal) {

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();

    }
}
