package com.srs.market.grpc.generator;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Component
@Log4j2
public class MarketCodeGenerator {
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
