package com.srs.market.grpc.validator.impl;

import com.srs.common.Error;
import com.srs.common.FindByIdsRequest;
import com.srs.market.CreateStallRequest;
import com.srs.market.UpdateStallMetadataRequest;
import com.srs.market.UpdateStallPositionRequest;
import com.srs.market.grpc.validator.StallRequestValidator;
import com.srs.market.repository.FloorRepository;
import com.srs.market.util.WebCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@Log4j2
@RequiredArgsConstructor
public class StallRequestValidatorImpl extends AbstractRequestValidator implements StallRequestValidator {

    private final FloorRepository floorRepository;

    @Override
    public Error validate(CreateStallRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        if (request.hasArea() && request.getArea() < 0) {
            addMinExceedsError("area", 0, errors);
        }

        return fromValidationResult(errors);
    }


    @Override
    public Error validate(UpdateStallMetadataRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        if (request.hasArea() && request.getArea() < 0) {
            addMinExceedsError("area", 0, errors);
        }

        return fromValidationResult(errors);
    }

    @Override
    public Error validate(UpdateStallPositionRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        if (!StringUtils.hasText(request.getShape())) {
            addMissingRequiredError("shape", errors);
        }

        return fromValidationResult(errors);
    }


    @Override
    public Error validate(FindByIdsRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        if (!CollectionUtils.isEmpty(request.getIdsList())) {
            List<String> stallsList = request.getIdsList();
            for (int i = 0, stallsListSize = stallsList.size(); i < stallsListSize; i++) {
                if (WebCommonUtil.isInvalidUUID(stallsList.get(i))) {
                    addInvalidValueError("ids[" + i + "]", errors);
                }
            }
        }

        return fromValidationResult(errors);
    }

}
