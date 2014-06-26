package cz.hatoff.geofort.store.unzipper;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class PocketQueryUnzipService {

    private static final Logger logger = Logger.getLogger(PocketQueryUnzipService.class);

    private ExecutorService threadPool;

    @Resource(name = "unzippedQueryQueue")
    private BlockingQueue<UnzippedPocketQuery> unzippedPocketQueryQueue;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        String threadCountString = environment.getProperty("unzipper.thread.pool.size");
        logger.info(String.format("Initializing unzipper tread pool with '%s' threads.", threadCountString));
        threadPool = Executors.newFixedThreadPool(Integer.valueOf(threadCountString));
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
//                while (true) {
//                    try {
//                        DownloadedPocketQuery downloadedPocketQuery = downloadedPocketQueryQueue.take();
//                        logger.info(String.format("Taking downloaded pocket query from queue '%s'. Creating new unzip task.", downloadedPocketQuery.getQueryName()));
//                    } catch (InterruptedException e) {
//                        logger.error(e);
//                    }
//                }
            }
        };
        new Thread(threadProcessor).start();
    }

    @PreDestroy
    private void closeUnzipper() throws InterruptedException {
        logger.info("Shutting down pocket query unzip service. Waiting for running downloads with 30 second timeout.");
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }

}
