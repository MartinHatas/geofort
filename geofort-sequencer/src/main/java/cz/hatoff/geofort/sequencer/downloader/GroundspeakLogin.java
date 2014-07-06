package cz.hatoff.geofort.sequencer.downloader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GroundspeakLogin {

    private static final Logger logger = Logger.getLogger(GroundspeakLogin.class);

    private static final String LOGIN_URI = "https://www.geocaching.com/login/default.aspx";

    private static final String EVENT_TARGET_KEY = "__EVENTTARGET";
    private static final String EVENT_ARGUMENT_KEY = "__EVENTARGUMENT";
    private static final String USERNAME_KEY = "ctl00$ContentBody$tbUsername";
    private static final String PASSWORD_KEY = "ctl00$ContentBody$tbPassword";
    private static final String REMEMBER_ME_KEY = "ctl00$ContentBody$cbRememberMe";
    private static final String SIGN_IN_KEY = "ctl00$ContentBody$btnSignIn";

    @Value("${downloader.groundspeak.login}")
    private String login;

    @Value("${downloader.groundspeak.password}")
    private String password;

    private CookieStore cookieStore;

    public CookieStore login() {
        logger.info(String.format("Logging to geocatching.com as user '%s'.", login));
        cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpUriRequest loginRequest = buildLoginRequest();
        CloseableHttpResponse loginResponse = null;
        try {
            loginResponse = login(httpClient, loginRequest);
            if (!(loginResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK ^ loginResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)) {
                String message = String.format("Cannot login into geocatching.com account with given credentials. Response HTTP code is '%d'", loginResponse.getStatusLine().getStatusCode());
                logger.error(message);
                throw new Exception(message);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            IOUtils.closeQuietly(loginResponse);
            IOUtils.closeQuietly(httpClient);
        }
        return cookieStore;
    }

    private CloseableHttpResponse login(CloseableHttpClient httpClient, HttpUriRequest loginRequest) throws IOException {
        CloseableHttpResponse loginResponse = httpClient.execute(loginRequest);
        HttpEntity loginEntity = loginResponse.getEntity();
        EntityUtils.consume(loginEntity);
        return loginResponse;
    }

    private HttpUriRequest buildLoginRequest() {
        return RequestBuilder.post()
                    .setUri(LOGIN_URI)
                    .addParameter(new BasicNameValuePair(EVENT_TARGET_KEY, ""))
                    .addParameter(new BasicNameValuePair(EVENT_ARGUMENT_KEY, ""))
                    .addParameter(new BasicNameValuePair(USERNAME_KEY, login))
                    .addParameter(new BasicNameValuePair(PASSWORD_KEY, password))
                    .addParameter(new BasicNameValuePair(REMEMBER_ME_KEY, "on"))
                    .addParameter(new BasicNameValuePair(SIGN_IN_KEY, "Login"))
                    .build();
    }

    public CookieStore getCookieStore() {
        if (cookieStore == null) {
            return login();
        }
        return cookieStore;
    }
}
