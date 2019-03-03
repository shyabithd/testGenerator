package generator.utils;

import org.slf4j.Logger;
import utils.Inputs;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AtMostOnceLogger {

    /**
     * Keep tracks of messages that should be log only once.
     *	Note: yes, this is a static field, but has no impact on test generation, so not a big deal
     */
    private static final Map<Logger, Set<String>> atMostOnceLogs = new ConcurrentHashMap();


    private static synchronized void logAtMostOnce(Logger logger, String message, boolean error){
        Inputs.checkNull(logger, message);

        Set<String> previous = atMostOnceLogs.get(logger);
        if(previous == null){
            previous = new LinkedHashSet<>();
            atMostOnceLogs.put(logger, previous);
        }

        if(!previous.contains(message)){
            previous.add(message);

            if(error){
                logger.error(message);
            } else {
                logger.warn(message);
            }
        }
    }

    public static void warn(Logger logger, String message){
        logAtMostOnce(logger,message,false);
    }

    public static void error(Logger logger, String message){
        logAtMostOnce(logger, message, true);
    }
}
