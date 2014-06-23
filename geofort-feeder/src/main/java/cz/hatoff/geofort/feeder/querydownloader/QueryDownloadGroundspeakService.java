package cz.hatoff.geofort.feeder.querydownloader;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
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

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Queue;


@Component
public class QueryDownloadGroundspeakService implements QueryDownloadService {

    private static final Logger logger = Logger.getLogger(QueryDownloadGroundspeakService.class);

    @Resource(name = "checkedQueryQueue")
    private Queue<CheckedPocketQuery> checkedPocketQueryQueue;

    @Resource(name = "downloadedQueryQueue")
    private Queue<DownloadedPocketQuery> downloadedPocketQueryQueue;

    @Autowired
    private GroundspeakLogin groundspeakLogin;

    @Autowired
    private Environment environment;

    public synchronized void checkForIncomingQueries() {
        if (!checkedPocketQueryQueue.isEmpty()) {
            downloadIncomingPocketQueries();
        }
    }

    private void downloadIncomingPocketQueries() {
        logger.info(String.format("Found '%d' incoming pocket queries. Going to start downloading.", checkedPocketQueryQueue.size()));
        Iterator<CheckedPocketQuery> pocketQueryIterator = checkedPocketQueryQueue.iterator();
        while (pocketQueryIterator.hasNext()) {
            CheckedPocketQuery checkedPocketQuery = pocketQueryIterator.next();
            Thread downloadThread = new Thread(new DownloadPocketQueryTask(checkedPocketQuery));
            downloadThread.start();
            pocketQueryIterator.remove();
        }
    }

    class DownloadPocketQueryTask implements Runnable {

        private CheckedPocketQuery checkedPocketQuery;

        public DownloadPocketQueryTask(CheckedPocketQuery checkedPocketQuery) {
            this.checkedPocketQuery = checkedPocketQuery;
        }

        @Override
        public void run() {
            CloseableHttpClient httpClient = null;
            try {
                CookieStore cookieStore = groundspeakLogin.login();
                httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

                HttpUriRequest downloadRequest = RequestBuilder.get()
                        .setUri(checkedPocketQuery.getDownloadUrl().toURI())
                        .build();

                CloseableHttpResponse downloadResponse = httpClient.execute(downloadRequest);
                InputStream pocketQueryStream = downloadResponse.getEntity().getContent();

                File pocketQueryFile = new File(environment.getProperty("application.directory.pq"), checkedPocketQuery.getQueryName());
                FileUtils.copyInputStreamToFile(pocketQueryStream, pocketQueryFile);

            } catch (Exception e) {
                logger.error(e);
            } finally {
                IOUtils.closeQuietly(httpClient);
            }

        }
    }
}
