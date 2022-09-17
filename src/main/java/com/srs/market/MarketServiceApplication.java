package com.srs.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class MarketServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(MarketServiceApplication.class);

    public static void main(String[] args) throws UnknownHostException {
        Environment env = SpringApplication.run(MarketServiceApplication.class, args).getEnvironment();

        final String application = env.getRequiredProperty("spring.application.name");
        final String port = env.getRequiredProperty("server.port");
        final String dbConn = env.getRequiredProperty("spring.datasource.url");

        logger.info("\n----------------------------------------------------------\n" +
                        "\tApplication '{}' is running! Access URLs:\n" +
                        "\tLocal address: \t\tlocalhost:{}\n" +
                        "\tExternal address: \t{}:{}\n" +
                        "\tDB Connection: \t\t{}\n" +
                        "----------------------------------------------------------\n",
                application, port, InetAddress.getLocalHost().getHostAddress(), port, dbConn);
    }
}
