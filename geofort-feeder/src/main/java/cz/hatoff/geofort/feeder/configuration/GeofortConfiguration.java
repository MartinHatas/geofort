package cz.hatoff.geofort.feeder.configuration;

import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class GeofortConfiguration {

    private static final Logger logger = Logger.getLogger(GeofortConfiguration.class);

    @Bean(name = "checkedQueryQueue")
    public BlockingQueue<CheckedPocketQuery> createCheckedPocketQueryQueue() {
        return new LinkedBlockingQueue<CheckedPocketQuery>();
    }

    @Bean(name = "downloadedQueryQueue")
    public BlockingQueue<DownloadedPocketQuery> createDownloadedPocketQueryQueue() {
        return new LinkedBlockingQueue<DownloadedPocketQuery>();
    }

    @Bean
    public PropertiesConfiguration createConfiguration() throws ConfigurationException, FileNotFoundException {
        File configurationFile = new File("application.properties");
        if (!configurationFile.exists()) {
            throw new FileNotFoundException(String.format("Unable to load configuration from file '%s'", configurationFile.getAbsolutePath()));
        }
        logger.info(String.format("Loading application configuration from file '%s'", configurationFile.getAbsolutePath()));
        PropertiesConfiguration config = new PropertiesConfiguration("application.properties");
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }

}
