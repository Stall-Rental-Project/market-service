package com.srs.market.grpc.validator.impl;


import com.srs.common.Error;
import com.srs.common.ErrorCode;
import com.srs.common.FindByIdRequest;
import com.srs.market.ListMarketsRequest;
import com.srs.market.MarketLocation;
import com.srs.market.UpsertMarketRequest;
import com.srs.market.grpc.validator.MarketRequestValidator;
import com.srs.market.repository.MarketRepository;
import com.srs.market.util.WebCommonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
@Log4j2
public class MarketRequestValidatorImpl extends AbstractRequestValidator implements MarketRequestValidator {

    private final MarketRepository marketRepository;

    public MarketRequestValidatorImpl(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Override
    public Error validate(ListMarketsRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        return fromValidationResult(errors);
    }

    @Override
    public Error validate(UpsertMarketRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        if (!StringUtils.hasText(request.getName())) {
            addMissingRequiredError("name", errors);
        }

        if (!request.hasLocation()) {
            addMissingRequiredError("province", errors);
            addMissingRequiredError("city", errors);
            addMissingRequiredError("ward", errors);
            addMissingRequiredError("address", errors);
        } else {
            validateLocationRequest(request.getLocation(), errors);
        }

        if (errors.isEmpty()) {
            if (StringUtils.hasText(request.getMarketId())) {
                var marketId = UUID.fromString(request.getMarketId());

                var markets = marketRepository.findAllByNameAndCity(request.getName(), request.getLocation().getCity());

                boolean isDuplicatedInName = markets.stream().anyMatch(market -> !(marketId.equals(market.getMarketId()) || marketId.equals(market.getPreviousVersion())));

                if (isDuplicatedInName) {
                    addAlreadyExistsError("name", "Market", errors);
                }
            } else if (marketRepository.existsByNameIgnoreCaseAndLocation_CityIgnoreCaseAndDeletedFalse(request.getName(), request.getLocation().getCity())) {
                addAlreadyExistsError("name", "Market", errors);
            }
        }

        return fromValidationResult(errors);
    }

    private void validateLocationRequest(MarketLocation location, Map<String, String> errors) {
        if (!StringUtils.hasText(location.getProvince())) {
            addMissingRequiredError("province", errors);
        }
        if (!StringUtils.hasText(location.getCity())) {
            addMissingRequiredError("city", errors);
        }
        if (!StringUtils.hasText(location.getWard())) {
            addMissingRequiredError("ward", errors);
        }
        if (!StringUtils.hasText(location.getAddress())) {
            addMissingRequiredError("address", errors);
        }
    }

    @Override
    public Error validate(FindByIdRequest request, UUID userId) {
        Map<String, String> errors = new HashMap<>();

        if (WebCommonUtil.isInvalidUUID(request.getId())) {
            addInvalidValueError("market_id", errors);
        }

        return fromValidationResult(errors);
    }
}
