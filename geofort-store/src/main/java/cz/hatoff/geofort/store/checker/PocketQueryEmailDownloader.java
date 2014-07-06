package cz.hatoff.geofort.store.checker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;


@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PocketQueryEmailDownloader {

    private static final Logger logger = Logger.getLogger(PocketQueryEmailDownloader.class);
    private static final String FOLDER = "Inbox";

    private final Properties properties = new Properties();

    @Resource(name = "emailQueue")
    private BlockingQueue<Email> emailQueue;

    private Folder pqInbox;
    private Store store;

    @Value("${downloader.email.server}")
    private String downloadEmailServer;

    @Value("${downloader.email.login}")
    private String downloadEmailLogin;

    @Value("${downloader.email.password}")
    private String downloadEmailPassword;

    public PocketQueryEmailDownloader() {
        properties.setProperty("mail.store.protocol", "imaps");
    }

    public void downloadPocketQueryEmails() {
        logger.info(String.format("Going to check for new incoming pocket queries at '%s' in the '%s' folder.", downloadEmailLogin, FOLDER));
        try {
            connectToStore();
            openFolders();
            lookForNewEmails();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            closeStore();
        }
    }

    private void lookForNewEmails() throws Exception {
        if (pqInbox.getMessageCount() > 0) {
            resolvePQEmails();
        } else {
            logger.info(String.format("No new PQ emails detected in folder '%s'.", FOLDER));
        }
    }

    private void resolvePQEmails() throws Exception {
        Message[] allEmailMessages = pqInbox.getMessages();
        logger.info(String.format("Found '%d' new messages inside '%s' folder." , allEmailMessages.length, FOLDER));
        for (Message message : allEmailMessages) {
            resolveIfPocketQuery(message);
        }
    }


    private void resolveIfPocketQuery(Message message) throws Exception {
        logger.info(String.format("Going to check message from '%s' with subject '%s'.", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
        Matcher matcher = QueryCheckingMailService.SUBJECT_PATTERN.matcher(message.getSubject());
        if (matcher.find()){
            logger.info(String.format("Email from '%s' with subject '%s' is OK!", StringUtils.join(message.getFrom(), ", "), message.getSubject()));

            Multipart multipart = (Multipart) message.getContent();
            byte[] zipArchive = getZipArchive(multipart);


            if (zipArchive != null) {
                Email email = new Email(StringUtils.join(message.getFrom(), ", "), message.getSubject(), message.getSentDate(), zipArchive);
                logger.info(String.format("Successfully found email with .zip attachment and going to send it into unzipper '%s'", email));
                emailQueue.put(email);
            } else {
                logger.warn(String.format("Found email '%s' without attachment with subject '%s'.", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
            }
            logger.info(String.format("Deleting email '%s' with subject '%s'.", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
            message.setFlag(Flags.Flag.DELETED, true);
        }
    }

    private byte[] getZipArchive(Multipart multipart) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (QueryCheckingMailService.CONTENT_PATTERN.matcher(bodyPart.getContentType()).matches()) {
                InputStream inputStream = bodyPart.getInputStream();
                return IOUtils.toByteArray(inputStream);
            }
        }
        return null;
    }

    private void openFolders() throws MessagingException {
        pqInbox = store.getFolder(FOLDER);
        if (!pqInbox.exists()) {
            logger.info(String.format("Folder where are expected new PQs '%s' does not exist. Making new direcory '%s'.", FOLDER, FOLDER));
            pqInbox.create(Folder.HOLDS_MESSAGES);
        }
        pqInbox.open(Folder.READ_WRITE);
    }

    private void connectToStore() throws MessagingException {
        logger.info(String.format("Connecting to store '%s' ... ", downloadEmailServer));
        Session session = Session.getInstance(properties, null);
        store = session.getStore();
        store.connect(downloadEmailServer, downloadEmailLogin, downloadEmailPassword);
    }

    private void closeStore() {
        if (store != null && store.isConnected()) {
            try {
                pqInbox.close(false);
                store.close();
                logger.info(String.format("Connection to store '%s' has been successfully closed.", store.getURLName().getHost()));
            } catch (Exception e) {
                logger.error(String.format("Failed to close connection to store '%s'", store.getURLName().getHost()), e);
            }
        }
    }

}
