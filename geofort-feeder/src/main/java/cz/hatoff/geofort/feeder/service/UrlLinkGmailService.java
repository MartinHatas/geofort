package cz.hatoff.geofort.feeder.service;

import com.sun.mail.imap.IMAPMessage;
import cz.hatoff.geofort.feeder.dto.PocketQuery;
import cz.hatoff.geofort.feeder.service.iface.UrlLinkService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import javax.mail.*;


@Component
public class UrlLinkGmailService implements UrlLinkService {

    private static final Logger logger = Logger.getLogger(UrlLinkGmailService.class);

    @Resource(name = "pocketQueryQueue")
    private BlockingQueue<PocketQuery> pocketQueryQueue;

    @Override
    public void checkForNewLinks() {
        logger.info("Checking for new links.");

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "ja.nejsem.opice@gmail.com", "RUMburak142.X");
            Folder inbox = store.getFolder("GEO");
            inbox.open(Folder.READ_WRITE);
            Message msg = inbox.getMessage(inbox.getMessageCount());
            Address[] in = msg.getFrom();
            for (Address address : in) {
                logger.info("FROM: " + address.toString());
            }

            logger.info("SENT DATE: " + msg.getSentDate());
            logger.info("SUBJECT: " + msg.getSubject());
            logger.info("CONTENT-TYPE:" + msg.getContentType());
            logger.info("CONTENT: " + msg.getContent());
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
