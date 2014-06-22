package cz.hatoff.geofort.feeder.querychecker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;


@Component
public class QueryCheckingGmailService implements QueryCheckingService {

    private static final Logger logger = Logger.getLogger(QueryCheckingGmailService.class);

    public static final Pattern SUBJECT_PATTERN = Pattern.compile("Geocaching: Your Pocket Query, (.*), is now available.");
    public static final Pattern DOWNLOAD_LINK_PATTERN = Pattern.compile("<a .*?href=['\"\"](.+?)['\"\"].*?>Download now</a>");

    @Autowired
    private PocketQueryEmailDownloader pocketQueryEmailDownloader;

    @Autowired
    private PocketQueryEmailParser pocketQueryEmailParser;

    @Resource(name = "checkedQueryQueue")
    private Queue<CheckedPocketQuery> checkedPocketQueryQueue;

    @Override
    public void checkForNewLinks() {
        logger.info("Checking for new links.");
        Set<Email> emails = pocketQueryEmailDownloader.downloadPocketQueryEmails();
        Set<CheckedPocketQuery> pocketQueries = pocketQueryEmailParser.parseMessagesToPocketQueries(emails);
        checkedPocketQueryQueue.addAll(pocketQueries);
    }

}
