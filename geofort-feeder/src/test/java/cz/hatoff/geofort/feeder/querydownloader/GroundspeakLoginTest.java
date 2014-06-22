package cz.hatoff.geofort.feeder.querydownloader;

import junit.framework.TestCase;


public class GroundspeakLoginTest extends TestCase {


    GroundspeakLogin groundspeakLogin = new GroundspeakLogin();

    public void testLogin() throws Exception {
        groundspeakLogin.login();
    }
}
