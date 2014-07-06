package cz.hatoff.geofort.sequencer.configuration;

import cz.hatoff.geofort.sequencer.downloader.GpxFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class SequencerConfiguration {

    @Bean(name = "generatedCodeQueue")
    public BlockingQueue<String> createGeneratedCodeQueue() {
        return new LinkedBlockingQueue<String>(1000);
    }

    @Bean(name = "gpxDownloadedQueue")
    public BlockingQueue<GpxFile> createGpxDownloadedQueue() {
        return new LinkedBlockingQueue<GpxFile>(1000);
    }
}
