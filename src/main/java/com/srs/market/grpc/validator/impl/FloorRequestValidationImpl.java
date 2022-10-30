package com.srs.market.grpc.validator.impl;

import com.srs.common.Error;
import com.srs.market.CreateFloorRequest;
import com.srs.market.UpdateFloorRequest;
import com.srs.market.dto.projection.RefIdProjection;
import com.srs.market.grpc.validator.FloorRequestValidator;
import com.srs.market.repository.FloorRepository;
import com.srs.market.util.WebCommonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@Log4j2
public class FloorRequestValidationImpl extends AbstractRequestValidator implements FloorRequestValidator {

    private final FloorRepository floorRepository;

    public FloorRequestValidationImpl(FloorRepository floorRepository) {
        this.floorRepository = floorRepository;
    }

    @Override
    public Error validate(CreateFloorRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (WebCommonUtil.isInvalidUUID(request.getMarketId())) {
            addInvalidValueError("market_id", errors);
        }

        if (!StringUtils.hasText(request.getName())) {
            addMissingRequiredError("name", errors);
        }

        if (!StringUtils.hasText(request.getImage())) {
            addMissingRequiredError("image", errors);
        }

        if (errors.isEmpty() && floorRepository.existsByMarketMarketIdAndNameIgnoreCaseAndDeletedIsFalse(UUID.fromString(request.getMarketId()), request.getName())) {
            addAlreadyExistsError("name", "Floor", errors);
        }

        return fromValidationResult(errors);
    }

    @Override
    public Error validate(UpdateFloorRequest request) {
        Map<String, String> errors = new HashMap<>();

        List<RefIdProjection> refs = floorRepository.findAllByNameIgnoreCaseAndFloorIdIsNot(request.getName(), UUID.fromString(request.getFloorplanId()));

        if (refs.size() > 0) {
            addAlreadyExistsError("name", "Floor", errors);
        }

        return fromValidationResult(errors);
    }
}
