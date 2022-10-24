package com.srs.market.grpc.mapper;

import com.srs.market.Floor;
import com.srs.market.entity.FloorEntity;
import com.srs.proto.mapper.BaseGrpcMapper;
import org.springframework.stereotype.Component;

@Component
public class FloorGrpcMapper implements BaseGrpcMapper<FloorEntity, Floor> {
    @Override
    public Floor toGrpcMessage(FloorEntity entity) {
        return null;
    }
}
