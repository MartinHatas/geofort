package cz.hatoff.geofort.store.crawlers.elasticsearch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Component
public class ElasticsearchCrawler {

    private static final Logger logger = Logger.getLogger(ElasticsearchCrawler.class);

    @Resource(name = "esCrawlerQueue")
    public BlockingQueue<List<ElasticsearchCacheDocument>> esCrawlerQueue;

    private ExecutorService threadPool;

    private Client client;

    @Value("${crawler.elasticsearch.thread.pool.size}")
    private int threadCount;

    @Value("${crawler.elasticsearch.cluster.name}")
    private String clusterName;

    @Value("${crawler.elasticsearch.index.name}")
    private String indexName;

    @Value("${crawler.elasticsearch.network.host}")
    private String clusterHost;

    @Value("${crawler.elasticsearch.network.port}")
    private int clusterPort;

    @PostConstruct
    private void init(){
        initElasticSearchNode();
        initThreadPool();
        initProcessThread();
    }

    private void initElasticSearchNode() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", clusterName).build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(clusterHost, clusterPort));

    }

    private void initThreadPool() {
        logger.info(String.format("Initializing elastic-search crawler tread pool with '%d' threads.", threadCount));
        threadPool = Executors.newFixedThreadPool(threadCount);
    }


    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        List<ElasticsearchCacheDocument> caches = esCrawlerQueue.take();
                        threadPool.submit(new ElasticsearchUpdateTask(caches));
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        };
        new Thread(threadProcessor).start();
    }

    @PreDestroy
    private void onDestroy() throws InterruptedException {
        logger.info("Shutting down elastic-search crawler service. Waiting for running downloads with 30 second timeout.");
        if (client != null) {
            client.close();
        }
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    private class ElasticsearchUpdateTask implements Runnable {

        private List<ElasticsearchCacheDocument> caches;

        public ElasticsearchUpdateTask(List<ElasticsearchCacheDocument> caches) {
            this.caches = caches;
        }

        @Override
        public void run() {
            String cachesString = StringUtils.join(caches, ", ");
            logger.info(String.format("Going to update index by '%s'", cachesString));
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (ElasticsearchCacheDocument cache : caches) {
                bulkRequest.add(client.prepareIndex(indexName, cache.getType(), cache.getCode()).setSource(cache.getJson()));
            }
            try {
                bulkRequest.execute().get();
                logger.info("Update of index OK!");
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
