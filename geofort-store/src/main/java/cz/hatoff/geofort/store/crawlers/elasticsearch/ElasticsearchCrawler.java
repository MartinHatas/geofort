package cz.hatoff.geofort.store.crawlers.elasticsearch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class ElasticsearchCrawler {

    private static final Logger logger = Logger.getLogger(ElasticsearchCrawler.class);

    @Resource(name = "esCrawlerQueue")
    public BlockingQueue<List<ElasticsearchCacheDocument>> esCrawlerQueue;

    private ExecutorService threadPool;

    @Autowired
    private Environment environment;

    private Client client;

    @PostConstruct
    private void init(){
        initElasticSearchNode();
        initThreadPool();
        initProcessThread();
    }

    private void initElasticSearchNode() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", environment.getProperty("crawler.elasticsearch.cluster.name")).build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(environment.getProperty("crawler.elasticsearch.network.host"), Integer.parseInt(environment.getProperty("crawler.elasticsearch.network.port"))));

    }

    private void initThreadPool() {
        String threadCountString = environment.getProperty("crawler.elasticsearch.thread.pool.size");
        logger.info(String.format("Initializing elasticsearch crawler tread pool with '%s' threads.", threadCountString));
        threadPool = Executors.newFixedThreadPool(Integer.valueOf(threadCountString));
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
    private void onDestroy(){
        if (client != null) {
            client.close();
        }
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
                bulkRequest.add(client.prepareIndex(environment.getProperty("crawler.elasticsearch.index.name"), cache.getType(), cache.getCode()).setSource(cache.getJson()));
            }
            try {
                bulkRequest.execute().get();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
