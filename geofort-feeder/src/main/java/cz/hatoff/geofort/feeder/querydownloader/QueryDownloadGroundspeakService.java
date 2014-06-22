package cz.hatoff.geofort.feeder.querydownloader;

import cz.hatoff.geofort.feeder.querychecker.CheckedPocketQuery;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Queue;


@Component
public class QueryDownloadGroundspeakService implements QueryDownloadService {

    private static final Logger logger = Logger.getLogger(QueryDownloadGroundspeakService.class);

    @Resource(name = "checkedQueryQueue")
    private Queue<CheckedPocketQuery> checkedPocketQueryQueue;

    @Resource(name = "downloadedQueryQueue")
    private Queue<DownloadedPocketQuery> downloadedPocketQueryQueue;

    @PostConstruct
    public void init(){

    }

    public void checkForIncomingQueries() {
        logger.info(String.format("Checking for incoming queries, found '%d'", checkedPocketQueryQueue.size()));
    }
}
