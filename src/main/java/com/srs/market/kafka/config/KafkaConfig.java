package com.srs.market.kafka.config;

import com.srs.common.kafka.BaseKafkaConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/**
 * @author duynt on 2/15/22
 */
@Configuration
public class KafkaConfig extends BaseKafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    //
    // Consumer
    //
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    @Bean("CustomKafkaConsumerFactory")
    public ConsumerFactory<String, Object> customConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(this.defaultConsumerConfig(kafkaProperties));
    }

    @Bean("CustomKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> customKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(customConsumerFactory());
        factory.setConcurrency(1);
        return factory;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    //
    // Producer
    //
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    @Bean("CustomKafkaProducerFactory")
    public ProducerFactory<String, Object> customProducerFactory() {
        return new DefaultKafkaProducerFactory<>(this.defaultProducerConfig(kafkaProperties));
    }

    @Bean("CustomKafkaTemplate")
    public KafkaTemplate<String, Object> customKafkaTemplate() {
        return new KafkaTemplate<>(customProducerFactory());
    }
}
