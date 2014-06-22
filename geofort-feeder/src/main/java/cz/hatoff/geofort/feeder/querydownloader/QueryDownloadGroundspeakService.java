package cz.hatoff.geofort.feeder.querydownloader;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
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
            InputStream inputStream = null;
            try {


                String authString = "my.nejsme.opice:slackLine87";
                System.out.println("auth string: " + authString);
                String authStringEnc = Base64.encode(authString.getBytes());

                URLConnection urlConnection = checkedPocketQuery.getDownloadUrl().openConnection();
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
                InputStream is = urlConnection.getInputStream();
                byte[] bytes = IOUtils.toByteArray(is);

                int i = 0;
            } catch (IOException e) {
                logger.error(e);

            } finally {
                IOUtils.closeQuietly(inputStream);
            }


        }
    }
}
