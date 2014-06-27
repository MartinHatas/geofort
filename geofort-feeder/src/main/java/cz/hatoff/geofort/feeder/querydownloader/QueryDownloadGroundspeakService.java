package cz.hatoff.geofort.feeder.querydownloader;

import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Component
public class QueryDownloadGroundspeakService {

    private static final Logger logger = Logger.getLogger(QueryDownloadGroundspeakService.class);

    private ExecutorService threadPool;

    @Resource(name = "checkedQueryQueue")
    private BlockingQueue<CheckedPocketQuery> checkedPocketQueryQueue;

    @Resource(name = "downloadedQueryQueue")
    private BlockingQueue<DownloadedPocketQuery> downloadedPocketQueryQueue;

    @Autowired
    private GroundspeakLogin groundspeakLogin;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        CheckedPocketQuery checkedPocketQuery = checkedPocketQueryQueue.take();
                        logger.info(String.format("Taking checked pocket query from queue '%s'. Creating new download task.", checkedPocketQuery.getQueryName()));
                        DownloadPocketQueryTask downloadPocketQueryTask = new DownloadPocketQueryTask(checkedPocketQuery);
                        threadPool.submit(downloadPocketQueryTask);
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        };
        new Thread(threadProcessor).start();
    }

    private void initThreadPool() {
        String threadCountString = environment.getProperty("downloader.thread.pool.size");
        logger.info(String.format("Initializing downloader tread pool with '%s' threads.", threadCountString));
        threadPool = Executors.newFixedThreadPool(Integer.valueOf(threadCountString));
    }


    @PreDestroy
    private void closeDownloader() throws InterruptedException {
        logger.info("Shutting down pocket query download service. Waiting for running downloads with 30 second timeout.");
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    class DownloadPocketQueryTask implements Runnable {

        private CheckedPocketQuery checkedPocketQuery;

        public DownloadPocketQueryTask(CheckedPocketQuery checkedPocketQuery) {
            this.checkedPocketQuery = checkedPocketQuery;
        }

        @Override
        public void run() {
            logger.info(String.format("Going to download pocket query archive '%s'.", checkedPocketQuery));
            DownloadedPocketQuery downloadedPocketQuery = downloadPocketQueryArchive();
            downloadedPocketQueryQueue.add(downloadedPocketQuery);
        }

        private DownloadedPocketQuery downloadPocketQueryArchive() {
            CloseableHttpClient httpClient = null;
            byte[] pocketQueryBytes = new byte[0];
            try {
                CookieStore cookieStore = groundspeakLogin.login();
                httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

                HttpUriRequest downloadRequest = RequestBuilder.get()
                        .setUri(checkedPocketQuery.getDownloadUrl().toURI())
                        .build();

                CloseableHttpResponse downloadResponse = httpClient.execute(downloadRequest);
                InputStream pocketQueryStream = downloadResponse.getEntity().getContent();

                pocketQueryBytes = IOUtils.toByteArray(pocketQueryStream);

            } catch (Exception e) {
                logger.error(e);
            } finally {
                IOUtils.closeQuietly(httpClient);
            }
            logger.info(String.format("Download of pocket query '%s' completed OK!", checkedPocketQuery.getQueryName()));
            return new DownloadedPocketQuery(checkedPocketQuery, pocketQueryBytes);
        }
    }
}
