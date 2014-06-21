package cz.hatoff.geofort.feeder.querychecker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

import javax.mail.*;


@Component
public class QueryCheckingGmailService implements QueryCheckingService {

    private static final Logger logger = Logger.getLogger(QueryCheckingGmailService.class);

    public static final Pattern SUBJECT_PATTERN = Pattern.compile("Geocaching: Your Pocket Query, (.*), is now available.");
    public static final Pattern DOWNLOAD_LINK_PATTERN = Pattern.compile("<a .*?href=['\"\"](.+?)['\"\"].*?>Download now</a>");

    @Autowired
    private PocketQueryEmailDownloader pocketQueryEmailDownloader;

    @Autowired
    private PocketQueryEmailParser pocketQueryEmailParser;

    @Resource(name = "pocketQueryQueue")
    private BlockingQueue<PocketQuery> pocketQueryQueue;

    @Override
    public void checkForNewLinks() {
        logger.info("Checking for new links.");
        Set<Message> messages = pocketQueryEmailDownloader.downloadPocketQueryEmails();
        Set<PocketQuery> pocketQueries = pocketQueryEmailParser.parseMessagesToPocketQueries(messages);
        pocketQueryQueue.addAll(pocketQueries);
    }


}
