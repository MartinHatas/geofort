package cz.hatoff.geofort.feeder.queryuploader;

import cz.hatoff.geofort.feeder.querydownloader.DownloadedPocketQuery;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class QueryGoogleDriveUploadService {

    private static final Logger logger = Logger.getLogger(QueryGoogleDriveUploadService.class);

    private ExecutorService threadPool;

    @Resource(name = "downloadedQueryQueue")
    private BlockingQueue<DownloadedPocketQuery> downloadedPocketQueryQueue;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void initDownloader() {
        initThreadPool();
        initProcessThread();
    }

    private void initThreadPool() {
        String threadCountString = environment.getProperty("uploader.thread.pool.size");
        logger.info(String.format("Initializing uploader tread pool with '%s' threads.", threadCountString));
        threadPool = Executors.newFixedThreadPool(Integer.valueOf(threadCountString));
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

    private class UploadPocketQueryTask implements Runnable {

        private DownloadedPocketQuery downloadedPocketQuery;

        public UploadPocketQueryTask(DownloadedPocketQuery downloadedPocketQuery) {
            this.downloadedPocketQuery = downloadedPocketQuery;
        }

        @Override
        public void run() {

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication("martin.hatas","slackLine87");
                        }
                    });

            try {
                logger.info("Going to send pocket query into store.");
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("martin.hatas@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ja.nejsem.opice@gmail.com"));
                message.setSubject("PocketQuery");
                message.setText("Hello, this is new PQ mail :]");

                // create the message part
                MimeBodyPart messageBodyPart = new MimeBodyPart();

                //fill message
                messageBodyPart.setText("Hi");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                DataSource source =  new ByteArrayDataSource(downloadedPocketQuery.getDownloadedQuery(), "application/zip");
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(downloadedPocketQuery.getQueryName() + ".zip");
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
