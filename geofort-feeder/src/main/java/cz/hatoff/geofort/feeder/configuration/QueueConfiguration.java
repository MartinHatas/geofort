package cz.hatoff.geofort.feeder.configuration;

import cz.hatoff.geofort.feeder.querychecker.PocketQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class QueueConfiguration {

    @Bean(name = "pocketQueryQueue")
    public BlockingQueue<PocketQuery> createPocketQueryQueue() {
        return new LinkedBlockingQueue<PocketQuery>();
    }
}
