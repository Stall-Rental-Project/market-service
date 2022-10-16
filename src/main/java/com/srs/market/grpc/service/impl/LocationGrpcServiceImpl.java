package com.srs.market.grpc.service.impl;

import com.google.protobuf.Any;
import com.srs.common.FindByIdsRequest;
import com.srs.common.ListResponse;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.market.*;
import com.srs.market.grpc.mapper.LocationGrpcMapper;
import com.srs.market.grpc.service.LocationGrpcService;
import com.srs.market.repository.LocationDslRepository;
import com.srs.market.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class LocationGrpcServiceImpl implements LocationGrpcService {
    private final LocationDslRepository locationDslRepository;

    private final LocationRepository locationRepository;

    private final LocationGrpcMapper locationGrpcMapper;

    @Override
    public ListProvinceResponse listProvinces(ListProvinceRequest request) {
        var provinces = locationDslRepository.findAllProvinces(request.getSearchTerm());

        return ListProvinceResponse.newBuilder()
                .setSuccess(true)
                .setData(ListProvinceResponse.Data.newBuilder()
                        .addAllProvinces(provinces)
                        .build())
                .build();    }

    @Override
    public ListCityResponse listCities(ListCityRequest request) {
        var cities = locationDslRepository.findAllCitiesByProvince(request);

        return ListCityResponse.newBuilder()
                .setSuccess(true)
                .setData(ListCityResponse.Data.newBuilder()
                        .addAllCities(cities)
                        .build())
                .build();    }

    @Override
    public ListWardResponse listWards(ListWardRequest request) {
        var wards = locationDslRepository.findAllWardsByProvinceAndCity(request);

        return ListWardResponse.newBuilder()
                .setSuccess(true)
                .setData(ListWardResponse.Data.newBuilder()
                        .addAllWards(wards)
                        .build())
                .build();    }

    @Override
    public GetLocationResponse getLocation(GetLocationRequest request) {
        var location = locationDslRepository.findByProvinceAndCityAndWard(request.getProvince(), request.getCity(), request.getWard())
                .orElseThrow(() -> new ObjectNotFoundException("Location not found"));

        return GetLocationResponse.newBuilder()
                .setSuccess(true)
                .setData(GetLocationResponse.Data.newBuilder()
                        .setLocation(locationGrpcMapper.toGrpcMessage(location))
                        .build())
                .build();    }

    @Override
    public ListResponse listLocations(FindByIdsRequest request) {
        var locationIds = request.getIdsList().stream()
                .filter(StringUtils::isNotBlank)
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        var locations = locationRepository.findAllByIds(locationIds).stream()
                .map(locationGrpcMapper::toGrpcMessage)
                .map(Any::pack)
                .collect(Collectors.toList());

        return ListResponse.newBuilder()
                .setSuccess(true)
                .setData(ListResponse.Data.newBuilder()
                        .addAllItems(locations)
                        .build())
                .build();    }
}
