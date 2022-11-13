package com.srs.market.kafka.service;

import com.srs.common.kafka.message.rental.LeaseApprovedKafkaMessage;
import com.srs.common.kafka.message.rental.LeaseTerminatedKafkaMessage;


public interface LeaseKafkaService {
    void setStallLeaseStatusToOccupied(LeaseApprovedKafkaMessage message);

    void setStallLeaseStatusToAvailable(LeaseTerminatedKafkaMessage message);

}
