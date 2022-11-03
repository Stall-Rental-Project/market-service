package com.srs.market.grpc.validator.impl;

import com.srs.common.Error;
import com.srs.market.UpsertFloorRequest;
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
    public Error validate(UpsertFloorRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (WebCommonUtil.isInvalidUUID(request.getMarketId())) {
            addInvalidValueError("market_id", errors);
        }

        if (!StringUtils.hasText(request.getFloorName())) {
            addMissingRequiredError("floor_name", errors);
        }

        if (!StringUtils.hasText(request.getImageName())) {
            addMissingRequiredError("image_name", errors);
        }

        if (!StringUtils.hasText(request.getImageUrl())) {
            addMissingRequiredError("image_url", errors);
        }

        if (!StringUtils.hasText(request.getFloorplanId())) {

            List<RefIdProjection> refs = floorRepository.findAllByNameIgnoreCaseAndFloorIdIsNot(request.getFloorName(), UUID.fromString(request.getFloorplanId()));

            if (refs.size() > 0) {
                addAlreadyExistsError("floor_name", "Floor", errors);
            }
        } else {
            if (errors.isEmpty() && floorRepository.existsByMarketMarketIdAndNameIgnoreCaseAndDeletedIsFalse(UUID.fromString(request.getMarketId()), request.getFloorName())) {
                addAlreadyExistsError("floor_name", "Floor", errors);
            }
        }

        return fromValidationResult(errors);
    }
}
