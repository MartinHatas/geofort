package cz.hatoff.geofort.feeder.configuration;

import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@PropertySource(value = "classpath:application.properties")
public class QueueConfiguration {

    @Bean(name = "checkedQueryQueue")
    public BlockingQueue<CheckedPocketQuery> createCheckedPocketQueryQueue() {
        return new LinkedBlockingQueue<CheckedPocketQuery>();
    }

    @Bean(name = "downloadedQueryQueue")
    public BlockingQueue<DownloadedPocketQuery> createDownloadedPocketQueryQueue() {
        return new LinkedBlockingQueue<DownloadedPocketQuery>();
    }

}
