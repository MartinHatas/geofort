package cz.hatoff.geofort.sequencer.downloader;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GpxDownloader {

    private static final Logger logger = Logger.getLogger(GpxDownloader.class);

    @Value("${downloader.thread.pool.size}")
    private int threadCount;

    @Resource(name = "generatedCodeQueue")
    public BlockingQueue<String> generatedCodeQueue;

    @Resource(name = "gpxDownloadedQueue")
    public BlockingQueue<GpxFile> gpxDownloadedQueue;

    @Autowired
    private GroundspeakLogin groundspeakLogin;

    private ExecutorService threadPool;

    private static final Pattern URL_PATTERN = Pattern.compile("<a .*?href=['\"\"](.+?)['\"\"].*?>");

    @PostConstruct
    public void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        logger.info(String.format("Initializing downloader tread pool with '%d' threads.", threadCount));
        threadPool = Executors.newFixedThreadPool(threadCount);
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String generatedCode = generatedCodeQueue.take();
                        threadPool.submit(new GpxDownloadTask(generatedCode));
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        };
        new Thread(threadProcessor).start();
    }

    @PreDestroy
    private void closeParser() throws InterruptedException {
        logger.info("Shutting down pocket query parsing service. Waiting for running downloads with 30 second timeout.");
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    private class GpxDownloadTask implements Runnable {

        private String generatedCode;

        private static final String DOWNLOAD_URL_STRING = "http://www.geocaching.com/geocache/";
        private static final String GPX_PARAM_NAME = "ctl00$ContentBody$btnGPXDL";
        private static final String GPX_PARAM_VALUE = "GPX file";
        private static final String EVENTTARGET = "__EVENTTARGET";
        private static final String EVENTARGUMENT = "__EVENTARGUMENT";
        private static final String VIEWSTATEFIELDCOUNT = "__VIEWSTATEFIELDCOUNT";


        private GpxDownloadTask(String generatedCode) {
            this.generatedCode = generatedCode;
        }

        @Override
        public void run() {
            CloseableHttpClient httpClient = null;
            byte[] gpxBytes = new byte[0];
            InputStream gpxStream = null;
            InputStream redirectPageStream = null;
            CloseableHttpResponse gpxResponse = null;
            CloseableHttpResponse downloadResponse = null;

            try {
                org.apache.http.client.CookieStore cookieStore = groundspeakLogin.getCookieStore();
                httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
                String uri = DOWNLOAD_URL_STRING + generatedCode;
                HttpPost postRequest = new HttpPost(uri);
                downloadResponse = httpClient.execute(postRequest);
                int statusCode = downloadResponse.getStatusLine().getStatusCode();
                if (statusCode == 401) {
                    groundspeakLogin.login();
                    run();
                }
                if (statusCode == 404) {
                    return;
                }

                redirectPageStream = downloadResponse.getEntity().getContent();
                String htmlRedirect = IOUtils.toString(redirectPageStream);

                String url;
                Matcher matcher = URL_PATTERN.matcher(htmlRedirect);
                if (matcher.find()){
                    url = matcher.group(1);
                } else {
                    logger.warn(String.format("URL link was not found for geocache '%s'", generatedCode));
                    return;
                }

                postRequest.setURI(new URI(url));
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                formParams.add(new BasicNameValuePair(EVENTTARGET, ""));
                formParams.add(new BasicNameValuePair(EVENTARGUMENT, ""));
                formParams.add(new BasicNameValuePair(VIEWSTATEFIELDCOUNT, "0"));
                formParams.add(new BasicNameValuePair(GPX_PARAM_NAME, GPX_PARAM_VALUE));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                postRequest.setEntity(entity);

                gpxResponse = httpClient.execute(postRequest);
                if (gpxResponse.getStatusLine().getStatusCode() == 200) {
                    gpxStream = gpxResponse.getEntity().getContent();
                    gpxBytes = IOUtils.toByteArray(gpxStream);
                } else {
                    logger.warn(String.format("GPX file was not found for geocache '%s'", generatedCode));
                    return;
                }

            } catch (Exception e) {
                logger.error(e);
            } finally {
                IOUtils.closeQuietly(redirectPageStream);
                IOUtils.closeQuietly(gpxStream);
                IOUtils.closeQuietly(downloadResponse);
                IOUtils.closeQuietly(gpxResponse);
                IOUtils.closeQuietly(httpClient);
            }
            logger.debug(String.format("Download of GPX file for cache '%s' completed OK!", generatedCode));
            try {
                if (gpxBytes.length > 0)
                gpxDownloadedQueue.put(new GpxFile(generatedCode, gpxBytes));
            } catch (InterruptedException e) {
                logger.error(e);
            }

        }
    }
}
