package com.srs.market.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author duynt on 11/8/21
 */
@Getter
@Setter
@AllArgsConstructor
public class StallLeaseStatusProjection {
    private UUID stallId;
    private int leaseStatus;
}
