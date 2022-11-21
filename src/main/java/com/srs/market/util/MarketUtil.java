package com.srs.market.util;

import com.querydsl.core.Tuple;
import com.srs.market.StallLeaseStatus;
import com.srs.market.entity.LocationEntity;
import com.srs.market.entity.MarketEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class MarketUtil {


    public String getFullAddress(MarketEntity market) {
        return String.format("%s, Ward %s, %s, %s",
                market.getAddress(),
                market.getLocation().getWard(),
                market.getLocation().getDistrict(),
                market.getLocation().getCity());
    }

    public String getFullAddress(MarketEntity market, LocationEntity location) {
        return String.format("%s, Ward %s, %s, %s",
                market.getAddress(),
                location.getWard(),
                location.getDistrict(),
                location.getCity());
    }

    public StallByLeaseStatusCounter countAndGroupStallsByLeaseStatus(List<Tuple> stalls) {
        long totalStalls = 0L;
        long availableStalls = 0L;
        long reservedStalls = 0L;
        long occupiedStalls = 0L;

        for (var item : stalls) {
            var leaseStatus = item.get(1, Integer.class);
            var stallId = item.get(0, UUID.class);
            assert leaseStatus != null;
            switch (leaseStatus) {
                case StallLeaseStatus.STALL_AVAILABLE_VALUE:
                    availableStalls++;
                    break;
                case StallLeaseStatus.STALL_OCCUPIED_VALUE:
                    occupiedStalls++;
                    break;
                default:
                    log.warn("Invalid lease status found for stall {}, status = {}", stallId, leaseStatus);
                    break;
            }

            totalStalls++;
        }

        var result = new StallByLeaseStatusCounter();
        result.totalStalls = totalStalls;
        result.availableStalls = availableStalls;
        result.reservedStalls = reservedStalls;
        result.occupiedStalls = occupiedStalls;

        return result;
    }

    @Getter
    public static class StallByLeaseStatusCounter {
        long totalStalls;
        long availableStalls;
        long reservedStalls;
        long occupiedStalls;
    }
}
