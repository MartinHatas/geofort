package cz.hatoff.geofort.sequencer.generator;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.BlockingQueue;

@Component
public class SequenceGenerator {

    private static final Logger logger = Logger.getLogger(SequenceGenerator.class);

    @Resource(name = "generatedCodeQueue")
    public BlockingQueue<String> generatedCodeQueue;

    private Thread generatingThread;

    private static final String CODE_PREFIX = "GC";

    @PostConstruct
    private void startGenerating() throws InterruptedException {
        logger.info("Creating geocode generator thread.");
        generatingThread = new Thread(new RunnableGenerator());
        generatingThread.start();
    }

    class RunnableGenerator implements Runnable {
        @Override
        public void run() {
            int number = 0;
            while (number < Integer.MAX_VALUE) {
                String fromDecimal = Convertor.convertFromDecimal(number);
                try {
                    generatedCodeQueue.put(CODE_PREFIX + fromDecimal);
                } catch (InterruptedException e) {
                    logger.error(e );
                }
                number++;
            }

        }
    }
}
