import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MemoryMonitor extends Observable implements Runnable {
    private Log log = LogFactory.getLog(MemoryMonitor.class);
    private static MemoryMonitor instance;
    private static double currentToMaxThreshold = 0.9;
    private static double freeMemThreshold = 0.2;
    private HashSet<StatefulProcessor> statefulProcessors  = new HashSet<>();

    private MemoryMonitor(){}

    public static synchronized MemoryMonitor getInstance(){
        if(instance == null){
            instance = new MemoryMonitor();
            new Thread(instance).start();
        }
        return instance;
    }

    public void register(StatefulProcessor obs) {
        instance.addObserver(obs);
        instance.statefulProcessors.add(obs);
    }

    public void deregister(StatefulProcessor obs) {
        instance.deleteObserver(obs);
        instance.statefulProcessors.remove(obs);
    }


    @Override
    public void run() {
        long sleepSeconds = 5;
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        log.info("TAW_MEM: starting memory monitor");
        while (true) {
            while (instance.countObservers()==0){
                try {
                    Thread.sleep(sleepSeconds*1000);
                } catch (InterruptedException e) {
                    log.error("Memory monitor Thread sleeping failed");
                }
            }
            long total = rt.totalMemory();
            long free = rt.freeMemory();
            double freePercentage  = (double) free/(double) total;
            double totalPercentage  = (double) total/(double) max;
            //TODO test tracking the max memory consumer without any threshold exceeding (for test purpose)
            //getMaxMemoryConsumer();
            log.info("TAW_MEM test sj yyy tot: " + total + ", free: "+ free  + " , max: "+ max +", free:total"+ freePercentage +" ; "+ totalPercentage);
            if(totalPercentage >= currentToMaxThreshold && freePercentage < freeMemThreshold){
                log.info("TAW_MEM: Threshold exceeded" );
                Observer observer = getMaxMemoryConsumer();
                if(observer !=null){
                    log.info("TAW_MEM: Sending state transfer notification to " + observer.getClass().getSimpleName());
                    setChanged();
                    notifyObservers(observer);
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Memory monitor Thread sleeping failed");
            }
        }
    }

    private Observer getMaxMemoryConsumer(){
        long mem = 0;
        Observer maxObserver = null;
        for(StatefulProcessor observer :statefulProcessors){
            long currentMemory = observer.getMemoryConsumption();
            log.info("TAW_MEM: Memory consumed by a stateful processor: " + observer.getClass().getSimpleName() + ","+mem+"bytes" );
            if(mem < currentMemory ){
                 mem = currentMemory;
                 maxObserver = observer;
            }
        }
        if(mem>0){
            log.info("TAW_MEM: Most memory consuming stateful processor: " + maxObserver.getClass().getSimpleName() + ","+mem+"bytes" );
        }
        return maxObserver;
    }
}
