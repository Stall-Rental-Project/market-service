package com.srs.market.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srs.common.kafka.KafkaTopic;
import com.srs.common.kafka.message.rental.LeaseApprovedKafkaMessage;
import com.srs.common.kafka.message.rental.LeaseTerminatedKafkaMessage;
import com.srs.market.kafka.service.LeaseKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@KafkaListener(topics = {
        KafkaTopic.LEASE_APPROVAL,
        KafkaTopic.LEASE_EVENTS,
}, containerFactory = "CustomKafkaListenerContainerFactory")
@RequiredArgsConstructor
@Log4j2
public class LeaseKafkaConsumer {
    private final ObjectMapper objectMapper;

    private final LeaseKafkaService leaseKafkaService;

    @KafkaHandler
    public void setStallLeaseStatusToOccupied(@Payload LeaseApprovedKafkaMessage message) {
        try {
            leaseKafkaService.setStallLeaseStatusToOccupied(message);
        } catch (Exception e) {
            log.error("Failed to update stall lease status after lease get approved. {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaHandler
    public void setStallLeaseStatusToAvailable(@Payload LeaseTerminatedKafkaMessage message) {
        try {
            leaseKafkaService.setStallLeaseStatusToAvailable(message);
        } catch (Exception e) {
            log.error(
                    "Failed to update stall lease status after lease being expired/terminated. {} - {}",
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
