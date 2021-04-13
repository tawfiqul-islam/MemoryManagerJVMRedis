import org.redisson.api.*;
import org.redisson.api.RLiveObjectService;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import org.redisson.client.RedisOutOfMemoryException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.SerializationCodec;
import org.redisson.codec.MarshallingCodec;
import org.redisson.codec.FstCodec;

public class RedissonTester {

    private static Log log = LogFactory.getLog(RedissonTester.class);
    RedissonClient redisson = null;
    RedisCacheImpl redisCache = null;
    RLiveObjectService service;
    boolean redisEnabled=false;
    private Map<String, TestClass> mapObj = new HashMap<>();

    public RedissonTester() {
        redisCache = new RedisCacheImpl();
        redisEnabled = redisCache.redisEnabled();
        if(redisEnabled) {
            redisson = redisCache.getRedissonClient();
            service = redisson.getLiveObjectService();
        }
        else{
            log.debug("FAILED TO CONNECT TO REDIS");
        }
    }

    public void mapTester(){

        for (int i=0;i<3;i++) {
            TestClass testObj = new TestClass();
            testObj.setName("name"+i);
            testObj.setValue(i);
            mapObj.put(Integer.toString(i),testObj);
        }
        transferStateToRedis();

        TestClass testObj= mapObj.get(Integer.toString(0));
        log.debug("testClass object 0's elements-> Name: "+testObj.getName()+" Value: "+testObj.getValue());
        testObj.setName("randomname");
        testObj.setValue(100);
        log.debug("Value after updating testObj: "+mapObj.get(Integer.toString(0)).getValue()+" "+mapObj.get(Integer.toString(0)).getName());
        log.debug("updating map entry in Redis Now... ");

        mapObj.put(Integer.toString(0),testObj);
        log.debug("Value after updating redisMap: "+mapObj.get(Integer.toString(0)).getValue()+" "+mapObj.get(Integer.toString(0)).getName());

    }
    public void liveObjectTester() {
        long startTime=System.currentTimeMillis();
        List<TestClassLive> testClassList= createDataSet(10000);
        long endTime=System.currentTimeMillis();
        log.debug("Total DataSet Creation Time in Redis: "+(endTime-startTime)/1000+" seconds");
        printDataSet(testClassList,3);
        /*TestClassLive testObj = new TestClassLive("myliveObject");
        // current state of myObject is now cleared and attached to Redis

        testObj.setName("SHA");
        testObj.setValue(100);
        log.debug("testObj values: "+testObj.getName()+" "+testObj.getValue());

        testObj = service.merge(testObj);
        log.debug("Testing Live Objects: ");

        testObj.setName("TAW");
        testObj.setValue(321);
        log.debug("testObj values after live update: "+testObj.getName()+" "+testObj.getValue());*/
    }

    public long getRandomLong() {
        return new Random().nextLong();
    }

    public List<TestClassLive> createDataSet(int instance) {

        List<TestClassLive> testClassList=new ArrayList<>();

        for(int i=0;i<instance;i++) {
            TestClassLive testObj = new TestClassLive("myliveObject"+i);
            testObj.setName("TAW"+i);
            testObj.setValue(i);
            testObj = service.merge(testObj);
            testClassList.add(testObj);

            TestClassLive2 testObj2 = new TestClassLive2(getRandomLong(),i);
            testObj2 = service.merge(testObj2);
            testObj2.setAddress("Address-"+i);
            testObj.setNestedClass(testObj2);
            for(int j=0;j<10;j++) {
                TestClassLive3 testObj3 = new TestClassLive3(getRandomLong(),i+j);
                testObj3 = service.merge(testObj3);
                testObj3.setData("ABCDEFGJIJKLMNOPQRSTUVWXYZABCDEFGJIJKLMNOPQRSTUVWXYZABCDEFGJIJKLMNOPQRSTUVWXYZ"+i+j);
                testObj.getNestedList().add(testObj3);
            }
        }
        return testClassList;
    }

    public void printDataSet(List<TestClassLive> testClassList,int instance)
    {
        log.debug("*******Starting to print Dataset values:********");
        for(int i=0;i<instance;i++) {

            TestClassLive testClassObj = testClassList.get(i);
            log.debug("TestClass: "+testClassObj.getId()+" "+testClassObj.getName());
            log.debug("TestClass2: "+testClassObj.getNestedClass().getAddress()+" "+testClassObj.getNestedClass().getValue2());
            List<TestClassLive3> testClass3List = testClassObj.getNestedList();
            for(int j=0;j<10;j++) {
                log.debug("TestClass3: " + testClass3List.get(j).getValue3()+" "+testClass3List.get(j).getData());
            }
        }
    }

    public boolean transferStateToRedis() {
        log.info("TAW_Redis_Test: redisEnabled value: "+redisEnabled);
        log.info("TAW_Redis_Test: Transferring objects to Redis");
        if(!redisEnabled)
            return false;
        RMap<String, TestClass> redisTestMap = redisson.getMap("locEvtMap" + this.toString(), new CompositeCodec(new StringCodec(), new FstCodec()), LocalCachedMapOptions.defaults());

        Iterator<Map.Entry<String, TestClass>> iteratorLocEvents = mapObj.entrySet().iterator();
        while (iteratorLocEvents.hasNext()) {
            Map.Entry<String, TestClass> entry = iteratorLocEvents.next();
            redisTestMap.fastPut(entry.getKey(),entry.getValue());
            iteratorLocEvents.remove();
        }
        mapObj = redisTestMap;

        log.info("TAW_Redis_Test: Redis transfer complete");
        return true;
    }

    public RLiveObjectService getService() {
        return service;
    }
}
