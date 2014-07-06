package cz.hatoff.geofort.store.parser;

import cz.hatoff.geofort.entity.model.Cache;
import cz.hatoff.geofort.store.crawlers.elasticsearch.ElasticsearchCacheDocument;
import cz.hatoff.geofort.store.unzipper.UnzippedPocketQuery;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import net.sf.json.xml.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    @Resource(name = "dbCrawlerQueue")
    private BlockingQueue<List<Cache>> dbCrawlerQueue;

    @Resource(name = "esCrawlerQueue")
    public BlockingQueue<List<ElasticsearchCacheDocument>> esCrawlerQueue;

    @Value("${parser.thread.pool.size}")
    private int threadCount;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        logger.info(String.format("Initializing parser tread pool with '%d' threads.", threadCount));
        threadPool = Executors.newFixedThreadPool(threadCount);
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
            logger.info(String.format("Going to parse PQ file '%s'", cacheFile.getAbsolutePath()));
            FileInputStream inputStream = null;
            try {
                inputStream = FileUtils.openInputStream(cacheFile);

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(inputStream);


                ArrayList<Cache> caches = new ArrayList<Cache>(20);
                ArrayList<ElasticsearchCacheDocument> cacheDocuments = new ArrayList<ElasticsearchCacheDocument>(20);

                NodeList wpts = doc.getElementsByTagName("wpt");
                int wptsLength = wpts.getLength();
                for (int i = 0; i < wptsLength; i++) {
                    try {
                        Node wpt = wpts.item(i);

                        String code = wpt.getChildNodes().item(3).getTextContent();
                        StringWriter writer = new StringWriter();
                        transformer.transform(new DOMSource(wpt), new StreamResult(writer));
                        byte[] cacheXMLbytes = writer.toString().getBytes("UTF-8");

                        Cache cache = new Cache();
                        cache.setCode(code);
                        cache.setGpx(cacheXMLbytes);
                        cache.setLastUpdate(new Date());


                        XMLSerializer xmlSerializer = new XMLSerializer();
                        JSONObject json = (JSONObject) xmlSerializer.readFromStream(new ByteArrayInputStream(cacheXMLbytes));
                        String latitude = json.getString("@lat");
                        String longitude = json.getString("@lon");
                        JSONArray coordinates = new JSONArray();
                        coordinates.add(longitude);
                        coordinates.add(latitude);
                        json.put("geofort:coordinates", coordinates);
                        ElasticsearchCacheDocument cacheDocument = new ElasticsearchCacheDocument();
                        cacheDocument.setCode(code);
                        cacheDocument.setJson(json.toString());

                        caches.add(cache);
                        cacheDocuments.add(cacheDocument);

                    } catch (Exception e) {
                        logger.error(e);
                    }

                    if (i % 20 == 0) {
                        dbCrawlerQueue.add(new ArrayList<Cache>(caches));
                        esCrawlerQueue.add(new ArrayList<ElasticsearchCacheDocument>(cacheDocuments));
                        caches.clear();
                        cacheDocuments.clear();
                    }
                }

                if (!caches.isEmpty()) {
                    dbCrawlerQueue.add(new ArrayList<Cache>(caches));
                    esCrawlerQueue.add(new ArrayList<ElasticsearchCacheDocument>(cacheDocuments));
                }


            } catch (Exception e) {
                logger.error(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
                FileUtils.deleteQuietly(unzippedPocketQuery.getPqDirectory());
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
