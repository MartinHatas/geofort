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
        logger.info("Starting geofort-feeder.");
        logger.info("Loading application context and initializing singletons.");
        context = new ClassPathXmlApplicationContext("spring-config.xml");
        ((AbstractApplicationContext) context).registerShutdownHook();

    }
}
