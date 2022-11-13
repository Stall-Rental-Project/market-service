
package com.srs.market.kafka.service.impl;

import com.srs.common.kafka.message.rental.LeaseApprovedKafkaMessage;
import com.srs.common.kafka.message.rental.LeaseTerminatedKafkaMessage;
import com.srs.market.StallLeaseStatus;
import com.srs.market.kafka.service.LeaseKafkaService;
import com.srs.market.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class LeaseKafkaServiceImpl implements LeaseKafkaService {
    private final StallRepository stallRepository;

    @Override
    @Transactional
    public void setStallLeaseStatusToOccupied(LeaseApprovedKafkaMessage message) {
        var stalls = stallRepository.findAllByMarketCodeAndFloorCodeAndStallCode(
                message.getMarketCode(), message.getFloorCode(), message.getStallCode());

        for (var stall : stalls) {
            stall.setLeaseStatus(StallLeaseStatus.STALL_OCCUPIED_VALUE);
            stall.setOccupiedBy(message.getApplicationId());
        }

        stallRepository.saveAll(stalls);
    }

    @Override
    public void setStallLeaseStatusToAvailable(LeaseTerminatedKafkaMessage message) {
        var stalls = stallRepository.findAllByMarketCodeAndFloorCodeAndStallCode(
                message.getMarketCode(), message.getFloorCode(), message.getStallCode());

        for (var stall : stalls) {
            stall.setLeaseStatus(StallLeaseStatus.STALL_AVAILABLE_VALUE);
            stall.setOccupiedBy(null);
        }

        stallRepository.saveAll(stalls);
    }

}
