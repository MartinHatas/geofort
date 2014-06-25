package cz.hatoff.geofort.feeder.parser;

import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class ParserServiceImpl implements ParserService {

    private static final Logger logger = Logger.getLogger(ParserServiceImpl.class);

    private ExecutorService threadPool;

    @Resource(name = "downloadedQueryQueue")
    private BlockingQueue<DownloadedPocketQuery> downloadedPocketQueryQueue;

    @Resource(name = "parsedQueryQueue")
    private BlockingQueue<ParsedPocketQuery> parsedPocketQueryQueue;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        String threadCountString = environment.getProperty("parser.thread.pool.size");
        logger.info(String.format("Initializing parser tread pool with '%s' threads.", threadCountString));
        threadPool = Executors.newFixedThreadPool(Integer.valueOf(threadCountString));
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        DownloadedPocketQuery downloadedPocketQuery = downloadedPocketQueryQueue.take();
                        logger.info(String.format("Taking downloaded pocket query from queue '%s'. Creating new parse task.", downloadedPocketQuery.getQueryName()));
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        };
        new Thread(threadProcessor).start();
    }

    @PreDestroy
    private void closeDownloader() throws InterruptedException {
        logger.info("Shutting down pocket query parsing service. Waiting for running downloads with 30 second timeout.");
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }


}
