package com.srs.market.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author duynt on 10/13/21
 */
@Getter
@Setter
@AllArgsConstructor
public class FloorStallCountProjection implements Serializable {
    private UUID floorplanId;
    private Long numStalls;
}
