package com.srs.market.grpc.validator.impl;

import com.srs.common.Error;
import com.srs.common.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.Map;

public class AbstractRequestValidator {
    protected void addMissingRequiredError(String field, Map<String, String> errors) {
        errors.put(field, StringUtils.capitalize(field) + " is required");
    }

    protected void addInvalidValueError(String field, Map<String, String> errors) {
        errors.put(field, StringUtils.capitalize(field) + " was given with invalid value");
    }

    protected void addMinExceedsError(String field, Comparable<?> minValue, Map<String, String> errors) {
        errors.put(field, StringUtils.capitalize(field) + " cannot less than " + minValue);
    }

    protected void addMaxExceedsError(String field, Comparable<?> maxValue, Map<String, String> errors) {
        errors.put(field, StringUtils.capitalize(field) + " cannot greater than " + maxValue);
    }

    protected void addNotExistsError(String field, String resourceName, Map<String, String> errors) {
        errors.put(field, StringUtils.capitalize(resourceName) + " not exists with given " + field);
    }

    protected void addAlreadyExistsError(String field, String resourceName, Map<String, String> errors) {
        errors.put(field, StringUtils.capitalize(resourceName) + " already exists with given " + field);
    }

    protected Error fromValidationResult(Map<String, String> validationResult) {
        if (validationResult.isEmpty()) {
            return Error.newBuilder()
                    .setCode(ErrorCode.SUCCESS)
                    .build();
        } else {
            return Error.newBuilder()
                    .setCode(ErrorCode.BAD_REQUEST)
                    .setMessage("Bad request was given")
                    .putAllDetails(validationResult)
                    .build();
        }
    }
}
