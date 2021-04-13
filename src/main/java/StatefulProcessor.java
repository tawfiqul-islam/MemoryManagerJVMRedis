import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.redisson.api.RedissonClient;
import java.util.ArrayList;
import java.util.Observer;
import org.apache.log4j.Logger;

public abstract class StatefulProcessor implements Observer {

    private static final Logger log = Logger.getLogger(StatefulProcessor.class);
    private transient MemoryMonitor memoryMonitor;
    protected transient RedissonClient redisson = null;
    protected boolean redisEnabled;
    private transient ArrayList<Object> objectsOfInterest = new ArrayList<>();
    private boolean objectsRegistered = false;

    public StatefulProcessor() {
        memoryMonitor = MemoryMonitor.getInstance();
        register();
        RedisCacheImpl redisCache = new RedisCacheImpl();
        redisEnabled = redisCache.redisEnabled();
        if(redisEnabled) {
            redisson = redisCache.getRedissonClient();
        }
    }

    public long getMemoryConsumption() {
        if(!objectsRegistered){
            objectsRegistered = true;
            log.info("TAW_MEM: registering memory consumers in statefulprocessor");
            registerMemoryConsumers();
        }
        else{
            log.info("TAW_MEM: object is registered already!");
        }
    	long size = 0;

        //TODO how do you get memory size from here?
    	for(Object o: objectsOfInterest) {
    		size = size + ObjectSizeCalculator.getObjectSize(o);
    	}
   		return size;
    }

    public void register(){
        memoryMonitor.register(this);
    }

    public void deRegister(){
        memoryMonitor.deregister(this);
    }


    public void addMemoryConsumers(Object...objects) {
    	for (Object obj: objects) {
    		objectsOfInterest.add(obj);
    	}
    }
    public abstract boolean transferStateToRedis();
    public abstract void registerMemoryConsumers();
}
