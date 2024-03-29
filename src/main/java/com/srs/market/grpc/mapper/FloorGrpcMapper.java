package com.srs.market.grpc.mapper;

import com.srs.market.Floor;
import com.srs.market.FloorState;
import com.srs.market.UpsertFloorRequest;
import com.srs.market.entity.FloorEntity;
import com.srs.market.grpc.generator.FloorCodeGenerator;
import com.srs.proto.mapper.BaseGrpcMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class FloorGrpcMapper implements BaseGrpcMapper<FloorEntity, Floor> {
    private final FloorCodeGenerator floorCodeGenerator;

    @Override
    public Floor toGrpcMessage(FloorEntity entity) {
        return this.toGrpcBuilder(entity).build();
    }

    public Floor.Builder toGrpcBuilder(FloorEntity floor) {
        var floorState = FloorState.forNumber(floor.getState());

        assert floorState != null;

        return Floor.newBuilder()
                .setFloorplanId(floor.getFloorId().toString())
                .setFloorName(floor.getName())
                .setCode(floor.getCode())
                .setImageName(floor.getImage())
                .setImageUrl(floor.getImageUrl())
                .setPreviousVersion(floor.getPreviousVersion() != null ? floor.getPreviousVersion().toString() : "");
    }

    public FloorEntity createFloor(UpsertFloorRequest request) {
        var floor = new FloorEntity();
        var floorCode = floorCodeGenerator.generate();

        floor.setCode(floorCode);
        floor.setName(request.getFloorName());
        floor.setImage(request.getImageName());
        floor.setImageUrl(request.getImageUrl());
        floor.setState(FloorState.FLOOR_STATE_UNPUBLISHED_VALUE);

        return floor;
    }

    public FloorEntity cloneFloor(FloorEntity floor) {
        var draft = new FloorEntity();

        draft.setImage(floor.getImage());
        draft.setImageUrl(floor.getImageUrl());
        draft.setCode(floor.getCode());
        draft.setName(floor.getName());
        draft.setMarket(floor.getMarket());
        draft.setState(FloorState.FLOOR_STATE_UNPUBLISHED_VALUE);
        draft.setPreviousVersion(floor.getFloorId());
        draft.setPublishedAtLeastOnce(floor.isPublishedAtLeastOnce());

        return draft;
    }
}
