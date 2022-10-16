package com.srs.market.util;

import com.srs.market.entity.LocationEntity;
import com.srs.market.entity.MarketEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

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

}
