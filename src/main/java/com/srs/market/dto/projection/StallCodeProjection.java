package com.srs.market.dto.projection;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author duynt on 10/15/21
 */
@Getter
@Setter
@AllArgsConstructor
public class StallCodeProjection implements Serializable {
    private String floorCode;
    private String marketCode;
    private String stallCode;

    public StallCodeProjection(String floorCode, String marketCode) {
        this.floorCode = floorCode;
        this.marketCode = marketCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StallCodeProjection that = (StallCodeProjection) o;
        return Objects.equal(floorCode, that.floorCode) && Objects.equal(marketCode, that.marketCode) && Objects.equal(stallCode, that.stallCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(floorCode, marketCode, stallCode);
    }
}
