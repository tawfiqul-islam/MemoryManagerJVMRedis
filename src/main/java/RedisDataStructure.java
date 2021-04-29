import org.redisson.api.RLiveObjectService;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.liveobject.resolver.UUIDGenerator;
import java.io.Serializable;


@REntity
public abstract class RedisDataStructure implements Serializable {
    @RId(generator = UUIDGenerator.class)
    private String Id;
    private String name;

    public RedisDataStructure() {
    }
    public RedisDataStructure(String Id) {
        super();
        this.Id = Id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return Id;
    }

    //public abstract boolean mergeStateToRedis(RLiveObjectService service);

    public Object mergeStateToRedis(RLiveObjectService service)
    {
        Object attachedObject;
        try{
            attachedObject=service.merge(this);
        }catch(Exception e)
        {
            System.out.println("exception while merging state to redis: "+e);
            return null;
        }

        return attachedObject;
    }
    public Object detachStateFromRedis(RLiveObjectService service, Object o)
    {
        Object detachedObject;
        RedisDataStructure redisObject = (RedisDataStructure) service.get(o.getClass(), this.Id);

        try{
            detachedObject=service.detach(redisObject);
        }catch(Exception e)
        {
            System.out.println("exception while detaching state from redis: "+e);
            return null;
        }
        return detachedObject;
    }

    public static boolean deleteStateFromRedis(RLiveObjectService service, Class cls, String Id)
    {
        RedisDataStructure redisObject = (RedisDataStructure) service.get(cls, Id);
        if(service.isClassRegistered(cls)) {
            System.out.println(cls+" is registered");
        }
        if(service.isLiveObject(redisObject))
        {
            System.out.println("returned object for "+cls+" is a live object in Redis");
        }
        try{
            service.delete(redisObject);
        }catch(Exception e)
        {
            System.out.println("exception while deleting state from redis: "+e);
            return false;
        }
        return true;
    }
    /*
    public boolean deleteStateFromRedis(RLiveObjectService service, Object o)
    {
        RedisDataStructure redisObject = (RedisDataStructure) service.get(o.getClass(), this.Id);
        if(service.isClassRegistered(o.getClass())) {
            System.out.println(o.getClass()+" is registered");
        }
        if(service.isLiveObject(redisObject))
        {
            System.out.println("returned object for "+o.getClass()+" is a live object in Redis");
        }
        try{
            service.delete(redisObject);
        }catch(Exception e)
        {
            System.out.println("exception while deleting state from redis: "+e);
            return false;
        }
        return true;
    }*/
    //public abstract void registerMemoryConsumers();
}

