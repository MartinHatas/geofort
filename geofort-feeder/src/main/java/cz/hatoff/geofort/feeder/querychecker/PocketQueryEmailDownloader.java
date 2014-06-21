package cz.hatoff.geofort.feeder.querychecker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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

    private static final String IMAP_SERVER = "imap.gmail.com";
    private static final String USER_NAME = "ja.nejsem.opice@gmail.com";
    private static final String PASSWORD = "RUMburak142.X";
    private static final String PQ_FOLDER = "GEO";
    private static final String PQ_FOLDER_DONE = "GEO-DONE";

    private final Properties properties = new Properties();

    private Set<Message> pocketQueryMessages = new HashSet<Message>();

    private Folder pqInbox;
    private Folder pqDoneInbox;
    private Store store;


    public PocketQueryEmailDownloader() {
        properties.setProperty("mail.store.protocol", "imaps");
    }

    public Set<Message> downloadPocketQueryEmails() {
        logger.info(String.format("Going to check for new incoming pocket queries at '%s' in the '%s' folder.", USER_NAME, PQ_FOLDER));
        try {
            connectToStore();
            openFolders();
            lookForNewEmails();
        } catch (Exception e) {
            logger.error(e);
        }
        return pocketQueryMessages;
    }

    private void lookForNewEmails() throws MessagingException {
        Message[] allEmailMessages = pqInbox.getMessages();
        logger.info(String.format("Found '%d' new messages inside '%s' folder." , allEmailMessages.length, PQ_FOLDER));
        for (Message message : allEmailMessages) {
            resolveIfPocketQuery(message);
        }
        copyProcessedMessagesToAnotherFolder();
    }

    private void copyProcessedMessagesToAnotherFolder() throws MessagingException {
        logger.info(String.format("Copying '%d' messages into '%s' folder.", pocketQueryMessages.size(), PQ_FOLDER_DONE));
        pqInbox.copyMessages(pocketQueryMessages.toArray(new Message[pocketQueryMessages.size()]), pqDoneInbox);
    }

    private void resolveIfPocketQuery(Message message) throws MessagingException {
        logger.info(String.format("Going to check message from '%s' with subject '%s'.", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
        Matcher matcher = QueryCheckingGmailService.SUBJECT_PATTERN.matcher(message.getSubject());
        if (matcher.find()){
            logger.info(String.format("Email from '%s' with subject '%s' is OK!", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
            pocketQueryMessages.add(message);
        }
    }

    private void openFolders() throws MessagingException {
        pqInbox = store.getFolder(PQ_FOLDER);
        pqDoneInbox = store.getFolder(PQ_FOLDER_DONE);
        pqInbox.open(Folder.READ_WRITE);
    }

    private void connectToStore() throws MessagingException {
        Session session = Session.getInstance(properties, null);
        store = session.getStore();
        store.connect(IMAP_SERVER, USER_NAME, PASSWORD);
    }
}
