package cz.hatoff.geofort.store.checker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;


@Component
public class QueryCheckingMailService {

    private static final Logger logger = Logger.getLogger(QueryCheckingMailService.class);

    public static final Pattern SUBJECT_PATTERN = Pattern.compile("Geocaching: Your Pocket Query, (.*), is now available.");
    public static final Pattern CONTENT_PATTERN = Pattern.compile("APPLICATION/ZIP(.*)");

    @Autowired
    private PocketQueryEmailDownloader pocketQueryEmailDownloader;

    public void checkForNewLinks() throws InterruptedException {
        logger.info("Checking for new links.");
        pocketQueryEmailDownloader.downloadPocketQueryEmails();
    }

}
