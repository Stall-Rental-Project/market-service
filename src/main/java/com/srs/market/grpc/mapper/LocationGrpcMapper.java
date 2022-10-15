package com.srs.market.grpc.mapper;

import com.srs.market.Location;
import com.srs.market.entity.LocationEntity;
import com.srs.proto.mapper.BaseGrpcMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class LocationGrpcMapper implements BaseGrpcMapper<LocationEntity, Location> {

    public Location.Builder toGrpcBuilder(LocationEntity location) {
        return Location.newBuilder()
                .setLocationId(location.getLocationId().toString())
                .setProvince(Objects.requireNonNullElse(location.getProvince(), ""))
                .setCity(Objects.requireNonNullElse(location.getCity(), ""))
                .setWard(Objects.requireNonNullElse(location.getWard(), ""))
                .setDistrict(Objects.requireNonNullElse(location.getDistrict(), ""))
                .setZipcode(Objects.requireNonNullElse(location.getZipcode(), ""));
    }

    @Override
    public Location toGrpcMessage(LocationEntity location) {
        return this.toGrpcMessage(location, "");
    }

    public Location toGrpcMessage(LocationEntity location, String address) {
        return toGrpcBuilder(location)
                .setAddress(Objects.requireNonNullElse(address, ""))
                .build();
    }
}
