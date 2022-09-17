package com.srs.market.kafka.service.impl;

import com.market.market.MarketClass;
import com.market.market.MarketStatus;
import com.market.market.MarketType;
import com.srs.common.kafka.message.market.DemoKafkaMessage;
import com.srs.market.entity.MarketEntity;
import com.srs.market.exception.ObjectNotFoundException;
import com.srs.market.kafka.service.MarketKafkaService;
import com.srs.market.repository.LocationRepository;
import com.srs.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class MarketKafkaServiceImpl implements MarketKafkaService {
    private final MarketRepository marketRepository;
    private final LocationRepository locationRepository;

    @Override
    public void saveMarket(DemoKafkaMessage message) {
        log.info("Succeed to receive message");
        var location = locationRepository.findById(message.getBarangayId())
                .orElseThrow(() -> new ObjectNotFoundException("Location not found"));
        var market=new MarketEntity();
        market.setName(message.getMarketName());
        market.setLocation(location);
        market.setAddress(message.getStreet());
        market.setGoogleMap(message.getMappedLocation());
        market.setType(message.getMarketType() == 0 ? MarketType.MARKET_TYPE_PRIVATE_VALUE : message.getMarketType());
        market.setCode("dacd8946-7a4d-4248-bf2b-d2bfed2c3eb0");
        market.setClazz(MarketClass.MARKET_CLASS_A_VALUE);
        market.setStatus(MarketStatus.MARKET_STATUS_ACTIVE_VALUE);
        marketRepository.save(market);
    }
}
