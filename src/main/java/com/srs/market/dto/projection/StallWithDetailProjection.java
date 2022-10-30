package com.srs.market.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author duynt on 10/20/21
 */
@Getter
@Setter
@AllArgsConstructor
public class StallWithDetailProjection implements Serializable {
    private UUID stallId;
    private UUID floorId;
    private String name;
    private String shape;
    private UUID refId;

    public StallWithDetailProjection(UUID stallId, UUID floorId, String name, String shape) {
        this.stallId = stallId;
        this.floorId = floorId;
        this.name = name;
        this.shape = shape;
    }
}
