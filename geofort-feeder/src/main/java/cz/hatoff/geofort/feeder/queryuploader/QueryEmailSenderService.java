package cz.hatoff.geofort.feeder.queryuploader;

import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class QueryEmailSenderService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddhhmmssSS");
    private static final String RECEIVER_EMAIL_ADDRESS = "browsil.pillow@gmail.com";


    private static final Logger logger = Logger.getLogger(QueryEmailSenderService.class);

    private ExecutorService threadPool;

    @Resource(name = "downloadedQueryQueue")
    private BlockingQueue<DownloadedPocketQuery> downloadedPocketQueryQueue;

    @Autowired
    private PropertiesConfiguration configuration;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        int threadCount = configuration.getInt("uploader.thread.pool.size");
        logger.info(String.format("Initializing uploader tread pool with '%d' threads.", threadCount));
        threadPool = Executors.newFixedThreadPool(threadCount);
    }

    private void initProcessThread() {
        Runnable threadProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        DownloadedPocketQuery downloadedPocketQuery = downloadedPocketQueryQueue.take();
                        logger.info(String.format("Taking downloaded pocket query from queue '%s'. Creating new upload task.", downloadedPocketQuery.getQueryName()));
                        UploadPocketQueryTask uploadPocketQueryTask = new UploadPocketQueryTask(downloadedPocketQuery);
                        threadPool.submit(uploadPocketQueryTask);
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        };
        new Thread(threadProcessor).start();
    }

    @PreDestroy
    private void closeDownloader() throws InterruptedException {
        logger.info("Shutting down pocket query upload service. Waiting for running downloads with 30 second timeout.");
        threadPool.shutdown();
        threadPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    private class UploadPocketQueryTask implements Runnable {

        private DownloadedPocketQuery downloadedPocketQuery;

        public UploadPocketQueryTask(DownloadedPocketQuery downloadedPocketQuery) {
            this.downloadedPocketQuery = downloadedPocketQuery;
        }

        @Override
        public void run() {

            Properties props = new Properties();
            props.put("mail.smtp.host", configuration.getString("uploader.email.server"));
            props.put("mail.smtp.socketFactory.port",configuration.getString("uploader.email.port"));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", configuration.getString("uploader.email.port"));

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(configuration.getString("uploader.email.login"), configuration.getString("uploader.email.password"));
                        }
                    });

            try {
                logger.info("Going to send pocket query into store.");
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(configuration.getString("uploader.email.login")));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECEIVER_EMAIL_ADDRESS));
                message.setSubject(downloadedPocketQuery.getEmailSubject());

                // create the message part
                MimeBodyPart messageBodyPart = new MimeBodyPart();

                //fill message
                messageBodyPart.setText(downloadedPocketQuery.toString());

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(downloadedPocketQuery.getDownloadedQuery(), "application/zip");
                messageBodyPart.setDataHandler(new DataHandler(source));

                String fileName = String.format("%s-%s.zip", "PQ", DATE_FORMAT.format(downloadedPocketQuery.getUpdateDate()));
                messageBodyPart.setFileName(fileName);
                multipart.addBodyPart(messageBodyPart);

                // Put parts in message
                message.setContent(multipart);

                Transport.send(message);

                logger.info("Pocket query has been successfully sent into store.");

            } catch (MessagingException e) {
                logger.error(e);
            }


        }
    }
}
