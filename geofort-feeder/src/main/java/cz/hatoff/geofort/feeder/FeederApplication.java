package cz.hatoff.geofort.feeder;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FeederApplication {

    private static final Logger logger = Logger.getLogger(FeederApplication.class);

    private ApplicationContext context;

    public static void main(String[] args) {
        new FeederApplication().start();
    }

    private void start() {
        logger.info("Starting GEOFORT FEEDER.");
        logger.info("Loading application context and initializing singletons.");
        context = new ClassPathXmlApplicationContext("spring-config.xml");
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                logger.debug("Shutdown hook was invoked. Shutting down geofort-feeder.");
                ((AbstractApplicationContext) context).close();
            }
        });
    }

    public static void start(String[] params) {
        logger.info("Geofort feeder starting as windows service.");
        new FeederApplication().start();
    }

    public static void stop(String[] params) {
        logger.info("Geofort feeder closing as windows service.");
        System.exit(0);
    }
}
