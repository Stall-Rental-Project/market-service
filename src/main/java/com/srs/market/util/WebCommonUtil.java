package com.srs.market.util;

import org.springframework.util.StringUtils;

import java.util.UUID;

public class WebCommonUtil {

    public static boolean isInvalidUUID(String uuid) {
        if (!StringUtils.hasText(uuid)) {
            return true;
        }

        try {
            UUID.fromString(uuid);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
