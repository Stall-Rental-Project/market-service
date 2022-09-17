package com.srs.market.kafka.service;

import com.srs.common.kafka.message.market.DemoKafkaMessage;

public interface MarketKafkaService {
void saveMarket(DemoKafkaMessage message);
}
