package cz.hatoff.geofort.feeder.querychecker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

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

    private Set<CheckedPocketQuery> pocketQueries = new HashSet<CheckedPocketQuery>();

    public Set<CheckedPocketQuery> parseMessagesToPocketQueries(Set<Email> emails) {
        logger.info(String.format("Going to parse '%d' pocket query emails.", emails.size()));

        for (Email email : emails) {
            try {
                String queryName = parseQueryName(email);
                URL downloadUrl = parseQueryDownloadUrl(email);
                if (downloadUrl == null) continue;
                createPocketQuery(queryName, downloadUrl, email);
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return pocketQueries;
    }


    private void createPocketQuery(String queryName, URL downloadUrl, Email email) {
        CheckedPocketQuery checkedPocketQuery = new CheckedPocketQuery(queryName, downloadUrl, email.getUpdateDate(), email.getSubject());
        pocketQueries.add(checkedPocketQuery);
    }

    private URL parseQueryDownloadUrl(Email email) throws IOException, MessagingException {
        String messageContent = email.getContent();
        Matcher downloadLinkMatcher = QueryCheckingMailService.DOWNLOAD_LINK_PATTERN.matcher(messageContent);
        if (!downloadLinkMatcher.find()) {
            logger.warn(String.format("Pocket query download link was not found in email from '%s' with subject '%s'. This email will be skipped.", email.getFrom(), email.getSubject()));
            return null;
        }
        String downloadLink = downloadLinkMatcher.group(1);
        logger.info(String.format("Successfully found download link '%s' for email from '%s' with subject '%s'", downloadLink, email.getFrom(), email.getSubject()));
        return new URL(downloadLink);
    }

    private String parseQueryName(Email email) throws MessagingException {
        String subject = email.getSubject();
        Matcher queryNameMatcher = QueryCheckingMailService.SUBJECT_PATTERN.matcher(subject);
        queryNameMatcher.find();
        return queryNameMatcher.group(1);
    }
}
