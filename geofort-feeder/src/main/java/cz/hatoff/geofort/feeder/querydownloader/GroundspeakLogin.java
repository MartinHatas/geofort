package cz.hatoff.geofort.feeder.querydownloader;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroundspeakLogin {

    private static final String LOGIN_URI = "https://www.geocaching.com/login/default.aspx";

    private static final String EVENT_TARGET_KEY = "__EVENTTARGET";
    private static final String EVENT_ARGUMENT_KEY = "__EVENTARGUMENT";
    private static final String USERNAME_KEY = "ctl00$ContentBody$tbUsername";
    private static final String PASSWORD_KEY = "ctl00$ContentBody$tbPassword";
    private static final String REMEMBER_ME_KEY = "ctl00$ContentBody$cbRememberMe";
    private static final String SIGN_IN_KEY = "ctl00$ContentBody$btnSignIn";

    @Autowired
    private Environment environment;

    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private CookieStore cookieStore = new BasicCookieStore();

    public void login() {

        HttpPost httpPost = new HttpPost(LOGIN_URI);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(EVENT_TARGET_KEY, ""));
        parameters.add(new BasicNameValuePair(EVENT_ARGUMENT_KEY, ""));
        parameters.add(new BasicNameValuePair(USERNAME_KEY, "my.nejsme.opice"));
        parameters.add(new BasicNameValuePair(PASSWORD_KEY, "slackLine87"));
        parameters.add(new BasicNameValuePair(REMEMBER_ME_KEY, "on"));
        parameters.add(new BasicNameValuePair(SIGN_IN_KEY, "Login"));

        CloseableHttpResponse loginResponse = null;

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            loginResponse = httpClient.execute(httpPost);
        } catch (Exception e) {
            if (loginResponse != null) {
                loginResponse.close();
            }
        }
        int i = 0;

    }
}
