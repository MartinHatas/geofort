package cz.hatoff.geofort.feeder.querychecker;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.mail.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;


@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PocketQueryEmailDownloader {

    private static final Logger logger = Logger.getLogger(PocketQueryEmailDownloader.class);

    private final Properties properties = new Properties();

    private Set<Email> pocketQueryMessages = new HashSet<Email>();

    private Folder pqInbox;
    private Folder pqDoneInbox;
    private Store store;

    @Autowired
    private PropertiesConfiguration configuration;


    public PocketQueryEmailDownloader() {
        properties.setProperty("mail.store.protocol", "imaps");
    }

    public Set<Email> downloadPocketQueryEmails() {
        logger.info(String.format("Going to check for new incoming pocket queries at '%s' in the '%s' folder.", configuration.getString("checker.email.login"), configuration.getString("checker.email.pq.folder")));
        try {
            connectToStore();
            openFolders();
            lookForNewEmails();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            closeStore();
        }
        return pocketQueryMessages;
    }

    private void lookForNewEmails() throws Exception {
        if (pqInbox.getMessageCount() > 0) {
            resolvePQEmails();
        } else {
            logger.info(String.format("No new PQ emails detected in folder '%s'.", configuration.getString("checker.email.pq.folder")));
        }
    }

    private void resolvePQEmails() throws Exception {
        Message[] allEmailMessages = pqInbox.getMessages();
        logger.info(String.format("Found '%d' new messages inside '%s' folder." , allEmailMessages.length, configuration.getString("checker.email.pq.folder")));
        for (Message message : allEmailMessages) {
            resolveIfPocketQuery(message);
        }
        copyProcessedMessagesToAnotherFolder(allEmailMessages);
    }

    private void copyProcessedMessagesToAnotherFolder(Message[] allEmailMessages) throws MessagingException {
        logger.info(String.format("Copying '%d' messages into '%s' folder.", pocketQueryMessages.size(), configuration.getString("checker.email.pq.folder.done")));
        pqInbox.copyMessages(allEmailMessages, pqDoneInbox);
    }

    private void resolveIfPocketQuery(Message message) throws Exception {
        logger.info(String.format("Going to check message from '%s' with subject '%s'.", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
        Matcher matcher = QueryCheckingMailService.SUBJECT_PATTERN.matcher(message.getSubject());
        if (matcher.find()){
            logger.info(String.format("Email from '%s' with subject '%s' is OK!", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
            pocketQueryMessages.add(new Email(StringUtils.join(message.getFrom(), ", "), message.getSubject(), (String) message.getContent(), message.getSentDate()));
            message.setFlag(Flags.Flag.DELETED, true);
        }
    }

    private void openFolders() throws MessagingException {
        pqInbox = store.getFolder(configuration.getString("checker.email.pq.folder"));
        pqDoneInbox = store.getFolder(configuration.getString("checker.email.pq.folder.done"));
        if (!pqInbox.exists()) {
            logger.info(String.format("Folder where are expected new PQs '%s' does not exist. Making new direcory '%s'.", configuration.getString("checker.email.pq.folder"), configuration.getString("checker.email.pq.folder")));
            pqInbox.create(Folder.HOLDS_MESSAGES);
        }
        if (!pqDoneInbox.exists()) {
            logger.info(String.format("Folder where are expected new PQs '%s' does not exist. Making new direcory '%s'.", configuration.getString("checker.email.pq.folder.done"), configuration.getString("checker.email.pq.folder.done")));
            pqDoneInbox.create(Folder.HOLDS_MESSAGES);
        }
        pqInbox.open(Folder.READ_WRITE);
    }

    private void connectToStore() throws MessagingException {
        logger.info(String.format("Connecting to store '%s' ... ", configuration.getString("checker.email.server")));
        Session session = Session.getInstance(properties, null);
        store = session.getStore();
        store.connect(configuration.getString("checker.email.server"), configuration.getString("checker.email.login"), configuration.getString("checker.email.password"));
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
