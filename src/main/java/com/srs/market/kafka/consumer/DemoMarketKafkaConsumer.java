package com.srs.market.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srs.common.kafka.KafkaTopic;
import com.srs.common.kafka.message.market.DemoKafkaMessage;
import com.srs.market.kafka.service.MarketKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        KafkaTopic.KAFKA_DEMO
}, containerFactory = "CustomKafkaListenerContainerFactory")
@RequiredArgsConstructor
@Log4j2
public class DemoMarketKafkaConsumer {
    private final ObjectMapper objectMapper;
    private final MarketKafkaService marketKafkaService;

    @KafkaHandler
    public void testKafkaDemo(
            @Payload DemoKafkaMessage message) {
        try {
            marketKafkaService.saveMarket(message);
        } catch (Exception e) {
            log.error(
                    "Failed to save market when create user. {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaHandler(isDefault = true)
    public void unrecognizedMessage(Object unrecognizedMessage) {
        try {
            log.warn("Unrecognized message found with type {} and content {}",
                    unrecognizedMessage.getClass().getSimpleName(),
                    objectMapper.writeValueAsString(unrecognizedMessage));
        } catch (JsonProcessingException e) {
            log.error("Error when write message to string. {}", e.getMessage());
            log.warn("Unrecognized malformed message found with type {}",
                    unrecognizedMessage.getClass().getSimpleName());
        }
    }
}
