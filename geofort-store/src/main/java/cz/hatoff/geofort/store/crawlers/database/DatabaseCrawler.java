package cz.hatoff.geofort.store.crawlers.database;

import cz.hatoff.geofort.store.entity.Cache;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

@Component
public class DatabaseCrawler {

    private static final Logger logger = Logger.getLogger(DatabaseCrawler.class);

    @Resource(name = "dbCrawlerQueue")
    private BlockingQueue<List<Cache>> dbCrawlerQueue;

    private ExecutorService threadPool;

    @Autowired
    private Environment environment;

    @Autowired
    private SessionFactory sessionFactory;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        String threadCountString = environment.getProperty("crawler.database.thread.pool.size");
        logger.info(String.format("Initializing unzipper tread pool with '%s' threads.", threadCountString));
        threadPool = Executors.newFixedThreadPool(Integer.valueOf(threadCountString));
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        List<Cache> caches = dbCrawlerQueue.take();
                       // logger.info(String.format("Taking parsed cache from queue '%s'. Creating new insert task.", cache.toString()));
                        threadPool.submit(new DatabaseInsertTask(caches));
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        };
        new Thread(threadProcessor).start();
    }



    @PreDestroy
    private void closeUnzipper() throws InterruptedException {
        logger.info("Shutting down pocket query unzip service. Waiting for running downloads with 30 second timeout.");
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    private class DatabaseInsertTask implements  Runnable {

        private List<Cache> caches;

        private DatabaseInsertTask(List<Cache> caches) {
            this.caches = caches;
        }

        @Override
        public void run() {
            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();
                for (Cache cache : caches) {
                    session.saveOrUpdate(cache);
                }
                transaction.commit();

            } catch (RuntimeException e) {
                logger.error(e);
                if (transaction != null) {
                    transaction.rollback();
                }
            } finally {
                if (session != null) {
                    session.close();
                }

            }



        }
    }
}
