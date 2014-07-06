package cz.hatoff.geofort.store;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StoreApplication {

    private static final Logger logger = Logger.getLogger(StoreApplication.class);

    private ApplicationContext context;

    public static void main(String[] args) {
        new StoreApplication().start();
    }

    private void start() {
        logger.info("Starting GEOFORT STORE.");
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
        logger.info("Geofort store starting as windows service.");
        new StoreApplication().start();
    }

    public static void stop(String[] params) {
        logger.info("Geofort store closing as windows service.");
        System.exit(0);
    }

}
