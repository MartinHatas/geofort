package cz.hatoff.geofort.store.configuration;


import cz.hatoff.geofort.store.parser.ParsedPocketQuery;
import cz.hatoff.geofort.store.unzipper.UnzippedPocketQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@PropertySource(value = "classpath:application.properties")
public class QueueConfiguration {

    @Bean(name = "unzippedQueryQueue")
    public BlockingQueue<UnzippedPocketQuery> createUnzippedPocketQueryQueue() {
        return new LinkedBlockingQueue<UnzippedPocketQuery>();
    }

    @Bean(name = "parsedQueryQueue")
    public BlockingQueue<ParsedPocketQuery> createParsedPocketQueryQueue() {
        return new LinkedBlockingQueue<ParsedPocketQuery>();
    }
}
