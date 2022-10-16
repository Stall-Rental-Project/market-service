package com.srs.market.grpc.mapper;

import com.srs.market.Supervisor;
import com.srs.market.entity.SupervisorEntity;
import com.srs.proto.mapper.BaseGrpcMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
@RequiredArgsConstructor
public class SupervisorGrpcMapper implements BaseGrpcMapper<SupervisorEntity, Supervisor> {

    @Override
    public Supervisor toGrpcMessage(SupervisorEntity supervisor) {
        return Supervisor.newBuilder()
                .setFirstName(Objects.requireNonNullElse(supervisor.getFirstName(), ""))
                .setMiddleName(Objects.requireNonNullElse(supervisor.getMiddleName(), ""))
                .setLastName(Objects.requireNonNullElse(supervisor.getLastName(), ""))
                .setPosition(Objects.requireNonNullElse(supervisor.getPosition(), ""))
                .setTelephone(Objects.requireNonNullElse(supervisor.getTelephone(), ""))
                .setMobilePhone(Objects.requireNonNullElse(supervisor.getMobilePhone(), ""))
                .setEmail(Objects.requireNonNullElse(supervisor.getEmail(), ""))
                .setSupervisorId(supervisor.getSupervisorId() != null ? supervisor.getSupervisorId().toString() : "")
                .build();
    }

    public SupervisorEntity createSupervisor(Supervisor supervisor) {
        var entity = new SupervisorEntity();

        BeanUtils.copyProperties(supervisor, entity);

        return entity;
    }
}
