package cz.hatoff.geofort.store.parser;

import cz.hatoff.geofort.store.entity.Cache;
import cz.hatoff.geofort.store.unzipper.UnzippedPocketQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import net.sf.json.xml.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
public class ParserServiceImpl {

    private static final Logger logger = Logger.getLogger(ParserServiceImpl.class);

    private static final Pattern WPT_FILE_PATTERN = Pattern.compile("(.*)-wpts.gpx");

    private ExecutorService threadPool;

    @Resource(name = "unzippedQueryQueue")
    private BlockingQueue<UnzippedPocketQuery> unzippedPocketQueryQueue;

    @Resource(name = "parsedQueryQueue")
    private BlockingQueue<ParsedPocketQuery> parsedPocketQueryQueue;

    @Resource(name = "dbCrawlerQueue")
    private BlockingQueue<List<Cache>> dbCrawlerQueue;

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
                        UnzippedPocketQuery unzippedPocketQuery = unzippedPocketQueryQueue.take();
                        logger.info(String.format("Taking downloaded pocket query from queue '%s'. Creating new parse task.", unzippedPocketQuery));
                        threadPool.submit(new CacheParseTask(unzippedPocketQuery));
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

    class CacheParseTask implements Runnable {


        private UnzippedPocketQuery unzippedPocketQuery;
        private Transformer transformer;


        CacheParseTask(UnzippedPocketQuery unzippedPocketQuery) {
            this.unzippedPocketQuery = unzippedPocketQuery;
            try {
                this.transformer = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException e) {
                logger.error(e);
            }
        }

        @Override
        public void run() {
            File cacheFile = getCacheFile();
            FileInputStream inputStream = null;
            try {
                inputStream = FileUtils.openInputStream(cacheFile);

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(inputStream);


                ArrayList<Cache> caches = new ArrayList<Cache>(20);

                NodeList wpts = doc.getElementsByTagName("wpt");
                int wptsLength = wpts.getLength();
                for (int i = 0; i < wptsLength; i++) {
                    Node wpt = wpts.item(i);

                    String code = wpt.getChildNodes().item(3).getTextContent();
                    StringWriter writer = new StringWriter();
                    transformer.transform(new DOMSource(wpt), new StreamResult(writer));

                    Cache cache = new Cache();
                    cache.setCode(code);
                    cache.setGpx(writer.toString().getBytes("UTF-8"));
                    cache.setLastUpdate(new Date());

                    caches.add(cache);

                    if (i % 20 == 0) {
                        dbCrawlerQueue.add(new ArrayList<Cache>(caches));
                        caches.clear();
                    }
                }

                if (!caches.isEmpty()) {
                    dbCrawlerQueue.add(new ArrayList<Cache>(caches));
                }


            } catch (Exception e) {
                logger.error(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        private File getCacheFile() {
            for (File file: unzippedPocketQuery.getExtractedFiles()) {
                if (!WPT_FILE_PATTERN.matcher(file.getName()).matches()) {
                    return file;
                }
            }
            return null;
        }
    }


}
