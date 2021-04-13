import org.apache.log4j.Logger;
import org.redisson.api.RLiveObjectService;
import org.redisson.api.RedissonClient;
//import storm.sharon.util.RedisCacheImpl;

public class RedissonLiveObjectService {

    private static final Logger log = Logger.getLogger(RedissonLiveObjectService.class);
    private static RedissonLiveObjectService instance;
    private static RedissonClient redisson = null;
    private static RedisCacheImpl redisCache = null;
    private static RLiveObjectService service = null;

    public static synchronized RedissonLiveObjectService getInstance(){
        if(instance == null){
            instance = new RedissonLiveObjectService();
        }
        return instance;
    }

    private RedissonLiveObjectService() {
        boolean redisEnabled;
        redisCache = new RedisCacheImpl();
        redisEnabled = redisCache.redisEnabled();
        if(redisEnabled) {
            redisson = redisCache.getRedissonClient();
            service = redisson.getLiveObjectService();
            log.debug("CONNECTION TO REDIS SUCCESSFUL");
        }
        else{
            log.debug("FAILED TO CONNECT TO REDIS");
        }
    }

    public static RLiveObjectService getService() {
        if(instance==null)
            getInstance();
        return service;
    }
}
