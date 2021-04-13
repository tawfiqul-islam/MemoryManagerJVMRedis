//import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;


public class RedisCacheImpl {
	private RedissonClient redissonClient = null;
	//private PropertiesConfiguration config;
	private Log log = LogFactory.getLog(RedisCacheImpl.class);

	public RedisCacheImpl() {
		//config = Properties.getConfig();
//		String[] redisClusterNodes;

//			redisClusterNodes = config.getStringArray("redis.hostlist");
//			for (int i=0;i<redisClusterNodes.length;i++){
//				redisClusterNodes[i] = "redis://"+redisClusterNodes[i];
//			}
//			redisConf.useClusterServers().addNodeAddress(redisClusterNodes);
			Config redisConf= new Config();
			redisConf.useSingleServer().setAddress("redis://127.0.0.1:6379");
			try {
				redissonClient = Redisson.create(redisConf);
			} catch (Exception ex){
				redissonClient = null;
				log.warn("Could not connect to redis "+ ex.getMessage());
			}

	}

	public boolean redisEnabled(){
		return redissonClient != null;
	}
	public RedissonClient getRedissonClient() {
		return redissonClient;
	}
}
