package cz.hatoff.geofort.sequencer;


import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SequencerApplication {

    private static final Logger logger = Logger.getLogger(SequencerApplication.class);

    private ApplicationContext context;

    public static void main(String[] args) {
        new SequencerApplication().start();
    }

    private void start() {
        logger.info("Starting GEOFORT SEQUENCER.");
        logger.info("Loading application context and initializing singletons.");
        context = new ClassPathXmlApplicationContext("spring-config.xml");
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
                logger.debug("Shutdown hook was invoked. Shutting down geofort-sequencer.");
                ((AbstractApplicationContext) context).close();
            }
        });
    }

    public static void start(String[] params) {
        logger.info("Geofort Sequencer starting as windows service.");
        new SequencerApplication().start();
    }

    public static void stop(String[] params) {
        logger.info("Geofort Sequencer closing as windows service.");
        System.exit(0);
    }
}
