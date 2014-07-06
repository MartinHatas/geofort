package cz.hatoff.geofort.store.unzipper;

import cz.hatoff.geofort.store.checker.Email;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class PocketQueryUnzipService {

    private static final Logger logger = Logger.getLogger(PocketQueryUnzipService.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddhhmmssSS");

    private ExecutorService threadPool;

    @Resource(name = "emailQueue")
    private BlockingQueue<Email> emailQueue;

    @Resource(name = "unzippedQueryQueue")
    public BlockingQueue<UnzippedPocketQuery> unzippedPocketQueries;

    @Value("${unzipper.thread.pool.size}")
    private int threadCount;

    @Value("${application.directory.pq}")
    private String pqDirectory;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        logger.info(String.format("Initializing unzipper tread pool with '%d' threads.", threadCount));
        threadPool = Executors.newFixedThreadPool(threadCount);
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Email email = emailQueue.take();
                        logger.info(String.format("Taking downloaded pocket query from queue '%s'. Creating new unzip task.", email.toString()));
                        threadPool.submit(new UnzipTask(email));
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

    class UnzipTask implements Runnable {

        private Email email;
        private byte[] buffer = new byte[1024];

        UnzipTask(Email email) {
            this.email = email;
        }

        @Override
        public void run() {
            List<File> extractedFiles = new ArrayList<File>(2);
            File pqDir = new File(pqDirectory, DATE_FORMAT.format(email.getUpdateDate()));
            pqDir.mkdirs();
            logger.info(String.format("Starting to unzip into directory '%s' - '%s'", pqDir.getAbsolutePath(), email));

            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(email.getZipArchive()));
            try {
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    String fileName = entry.getName();
                    File file = new File(pqDir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    extractedFiles.add(file);
                    logger.info(String.format("Successfully extracted file '%s'", file.getAbsolutePath()));
                    entry = zipInputStream.getNextEntry();
                }
                zipInputStream.closeEntry();
                zipInputStream.close();
                UnzippedPocketQuery unzippedPocketQuery = new UnzippedPocketQuery(pqDir, extractedFiles);
                unzippedPocketQueries.put(unzippedPocketQuery);
            } catch (Exception e) {
                logger.error(e);
                logger.info(String.format("Going to delete directory '%s'", pqDir.getAbsolutePath()));
                FileUtils.deleteQuietly(pqDir);
            }
        }
    }

}
