package cz.hatoff.geofort.feeder;

import cz.hatoff.geofort.feeder.configuration.QueueConfiguration;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainApplication {

    private static final Logger logger = Logger.getLogger(MainApplication.class);

    private ApplicationContext context;



    public static void main(String[] args) {
        new MainApplication().start();
    }

    private void start() {
        logger.info("Starting geofort-feeder.");
        logger.info("Loading application context and initializing singletons.");
        context = new ClassPathXmlApplicationContext("spring-config.xml");
        ((AbstractApplicationContext) context).registerShutdownHook();

    }
}
