package cz.hatoff.geofort.store.configuration;


import cz.hatoff.geofort.store.checker.Email;
import cz.hatoff.geofort.store.entity.Cache;
import cz.hatoff.geofort.store.parser.ParsedPocketQuery;
import cz.hatoff.geofort.store.unzipper.UnzippedPocketQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@PropertySource(value = "classpath:application.properties")
public class QueueConfiguration {

    @Bean(name = "parsedQueryQueue")
    public BlockingQueue<ParsedPocketQuery> createParsedPocketQueryQueue() {
        return new LinkedBlockingQueue<ParsedPocketQuery>();
    }

    @Bean(name = "unzippedQueryQueue")
    public BlockingQueue<UnzippedPocketQuery> createUnzippedPocketQueryQueue() {
        return new LinkedBlockingQueue<UnzippedPocketQuery>();
    }

    @Bean(name = "emailQueue")
    public BlockingQueue<Email> createEmailQueue() {
        return new LinkedBlockingQueue<Email>();
    }

    @Bean(name = "dbCrawlerQueue")
    public BlockingQueue<List<Cache>> createDbCrawlerQueue() {
        return new LinkedBlockingQueue<List<Cache>>();
    }
}
