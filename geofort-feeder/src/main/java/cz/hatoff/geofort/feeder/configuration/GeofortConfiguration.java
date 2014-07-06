package cz.hatoff.geofort.feeder.configuration;

import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        File configFile = new File("../config/application.properties");
        if (!configFile.exists()) {
            logger.warn(String.format("Unable to load configuration from file '%s', trying to load developer config.", configFile.getAbsolutePath()));
            configFile = new File("geofort-feeder/src/main/config/application.properties");
        }

        if (!configFile.exists()) {
            throw new FileNotFoundException("No configuration file has been found.");
        }

        logger.info(String.format("Loading application configuration from file '%s'", configFile.getAbsolutePath()));

        PropertiesConfiguration config = new PropertiesConfiguration(configFile);
        config.setReloadingStrategy(new FileChangedReloadingStrategy());

        return config;
    }

}
