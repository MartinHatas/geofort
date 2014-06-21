package cz.hatoff.geofort.feeder.querychecker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

@Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PocketQueryEmailParser {

    private static final Logger logger = Logger.getLogger(PocketQueryEmailParser.class);

    private Set<PocketQuery> pocketQueries = new HashSet<PocketQuery>();

    public Set<PocketQuery> parseMessagesToPocketQueries(Set<Message> messages) {
        logger.info(String.format("Going to parse '%d' pocket query emails.", messages.size()));

        for (Message message : messages) {
            try {
                String queryName = parseQueryName(message);
                URL downloadUrl = parseQueryDownloadUrl(message);
                if (downloadUrl == null) continue;
                createPocketQuery(queryName, downloadUrl);
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return pocketQueries;
    }

    private void createPocketQuery(String queryName, URL downloadUrl) {
        PocketQuery pocketQuery = new PocketQuery(queryName, downloadUrl);
        pocketQueries.add(pocketQuery);
    }

    private URL parseQueryDownloadUrl(Message message) throws IOException, MessagingException {
        String messageContent = (String) message.getContent();
        Matcher downloadLinkMatcher = QueryCheckingGmailService.DOWNLOAD_LINK_PATTERN.matcher(messageContent);
        if (!downloadLinkMatcher.find()) {
            logger.warn(String.format("Pocket query download link was not found in email from '%s' with subject '%s'. This message will be skipped.", StringUtils.join(message.getFrom(), ", "), message.getSubject()));
            return null;
        }
        String downloadLink = downloadLinkMatcher.group(1);
        logger.info(String.format("Successfully found download link '%s' for email from '%s' with subject '%s'", downloadLink, StringUtils.join(message.getFrom(), ", "), message.getSubject()));
        return new URL(downloadLink);
    }

    private String parseQueryName(Message message) throws MessagingException {
        String subject = message.getSubject();
        Matcher queryNameMatcher = QueryCheckingGmailService.SUBJECT_PATTERN.matcher(subject);
        queryNameMatcher.find();
        return queryNameMatcher.group(1);
    }
}
