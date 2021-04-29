import org.redisson.api.*;
import org.redisson.api.RLiveObjectService;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.FstCodec;

public class RedissonTester {

    private static Log log = LogFactory.getLog(RedissonTester.class);
    RedissonClient redisson = null;
    RedisCacheImpl redisCache = null;
    RLiveObjectService service;
    boolean redisEnabled=false;
    private Map<String, Circle> mapObj = new HashMap<>();

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

    public long getRandomLong() {
        return new Random().nextLong();
    }

    public void mapTester(){

        System.out.println("starting JVM Map and Redis Key-Value Map Testing");
        //Initiating data
        Circle circleObj = new Circle();
        Point p = new Point(1,1);
        circleObj.setId("myID");
        circleObj.setRadius(1);
        circleObj.setCentre(p);
        mapObj.put("myID",circleObj);

        //Observing JVM behaviour
        System.out.println("*Observing JVM behaviour:");
        circleObj= mapObj.get("myID");
        System.out.println("circleClass object's elements "+circleObj);
        circleObj.setRadius(2);
        circleObj.getCentre().setX(2);
        circleObj.getCentre().setY(2);
        System.out.println("circleClass object's elements after local updates: "+mapObj.get("myID"));

        //Observing Redis Map behaviour (key value store)
        System.out.println("*Observing Redis Map behaviour:");

        transferStateToRedis();
        //mapObj is a Redis Map now instead of JVM Map
        circleObj= mapObj.get("myID");
        System.out.println("Transferred circleClass object's elements in Redis: "+mapObj.get("myID"));
        circleObj.setRadius(3);
        circleObj.getCentre().setX(3);
        circleObj.getCentre().setY(3);
        System.out.println("***circleClass object's elements in RedisMap after local updates: "+mapObj.get("myID"));
        System.out.println("***If local changes are not updated in Redis, then all the above values should be 2s instead of 3s");
        //Manually overwriting the whole object in Redis
        System.out.println("Manually overwriting the map entry in Redis Now... ");
        mapObj.put("myID",circleObj);
        System.out.println("Value after overwriting whole object in RedisMap: "+mapObj.get("myID"));

    }

    public void liveObjectTester1() {

        System.out.println("*Starting Live Object Testing:");
        TestClassLive testObj = new TestClassLive("myLiveObjectID");

        testObj.setName("Tawfiq");
        testObj.setValue(111);
        System.out.println("TestClass Object elements before merging state to Redis: "+testObj);

        ///testObj becomes live object and the current state of myObject is now merged to Redis
        testObj = service.merge(testObj);

        System.out.println("TestClass became LiveObject, merged state to Redis");

        //Dynamically adding a custom class object as a nested field to the liveobject

        TestClassLive2 nestedObj = new TestClassLive2("myNestedObjectID");
        nestedObj.setValue(222);
        nestedObj.setData("ABC");
        nestedObj=service.merge(nestedObj);
        testObj.setNestedClass(nestedObj);


        System.out.println("Making Local Changes to LiveObject elements including the nested object: ");

        testObj.setName("Shanika");
        testObj.setValue(333);
        nestedObj=testObj.getNestedClass();
        nestedObj.setValue(444);
        nestedObj.setData("EDF");
        System.out.println("LiveObject elements after dynamic updates: ");
        System.out.println("TestClass Name: "+service.get(TestClassLive.class,"myLiveObjectID").getName());
        System.out.println("TestClass Value: "+service.get(TestClassLive.class,"myLiveObjectID").getValue());
        System.out.println("NestedClass Value: "+service.get(TestClassLive.class,"myLiveObjectID").getNestedClass().getValue());
        System.out.println("NestedClass Data: "+service.get(TestClassLive.class,"myLiveObjectID").getNestedClass().getData());
    }

    public boolean transferStateToRedis() {
        log.info("TAW_Redis_Test: redisEnabled value: "+redisEnabled);
        log.info("TAW_Redis_Test: Transferring objects to Redis");
        if(!redisEnabled)
            return false;
        RMap<String, Circle> redisTestMap = redisson.getMap("circleMap" + this.toString(), new CompositeCodec(new StringCodec(), new FstCodec()), LocalCachedMapOptions.defaults());

        Iterator<Map.Entry<String, Circle>> iteratorLocEvents = mapObj.entrySet().iterator();
        while (iteratorLocEvents.hasNext()) {
            Map.Entry<String, Circle> entry = iteratorLocEvents.next();
            redisTestMap.fastPut(entry.getKey(),entry.getValue());
            iteratorLocEvents.remove();
        }
        mapObj = redisTestMap;

        System.out.println("Redis transfer success");
        return true;
    }

    public void liveObjectTester2() {
        long startTime=System.currentTimeMillis();
        List<TestClassLive> testClassList= createDataSet(1);
        long endTime=System.currentTimeMillis();
        log.debug("Total DataSet Creation Time in Redis: "+(endTime-startTime)/1000+" seconds");
        printDataSet(testClassList,1);
    }

    public void windowBasedAlgorithm(boolean useRedis)
    {
        System.out.println("Initial Total JVM memory: " + Runtime.getRuntime().totalMemory()/(1024*1024)+"MB");
        System.out.println("Initial Free JVM memory: " + Runtime.getRuntime().freeMemory()/(1024*1024)+"MB");
        System.out.println("Initial Used JVM memory: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024)+"MB");
        System.out.println("***********");
        HashMap<String, TestTreeClass> treeMap = new HashMap<>();
        ArrayList<String> redisLiveObjectKeys= new ArrayList<>();
        for(int i=0;i<10;i++) {
            System.out.println("Iteration: "+(i+1));
            String myObjectID="myLiveObject-"+i;
            redisLiveObjectKeys.add(myObjectID);
            TestTreeClass treeClassObj = new TestTreeClass(myObjectID);
            treeMap.put(myObjectID,treeClassObj);
            treeClassObj.setName("A"+i);
            treeClassObj.setValue(i+1);
            try {
                String data = new String(Files.readAllBytes(Paths.get("Data_large.txt")));
                System.out.println("data length: "+data.length());
                treeClassObj.setData(data);
                data=null;
                //System.gc();
                //System.runFinalization();
                System.out.println("Local Object size after setting data: "+ ObjectSizeCalculator.getObjectSize(treeClassObj)/(1024*1024)+"MB");
                System.out.println("Free JVM memory: " + Runtime.getRuntime().freeMemory()/(1024*1024)+"MB");
                System.out.println("Used JVM memory: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024)+"MB");
                System.out.println("");
            }catch(Exception ex) {
                System.out.println("exception while reading file"+ex);
            }
            if(useRedis) {
                // returned attached proxy object must be captured to a variable to use it for future.
                //the old class object doesn't work and can't modify the redis state
                treeClassObj.mergeStateToRedis(service);
                treeClassObj = null;
                treeMap.remove(myObjectID);
                //treeClassObj=(TestTreeClass) treeClassObj.mergeStateToRedis(service);
                //System.gc();
                //System.runFinalization();
                System.out.println("Merging Object State to Redis...");
                System.out.println("Local Object Size After Redis Transfer: " + ObjectSizeCalculator.getObjectSize(treeClassObj) / (1024 * 1024) + "MB");
                System.out.println("Total JVM memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
                System.out.println("Free JVM memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
                System.out.println("Used JVM memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
                System.out.println("");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Program Completed Successfully");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(useRedis) {
            System.out.println("Clearing Redis Cache... ");
            for(int j=0;j<redisLiveObjectKeys.size();j++) {
                RedisDataStructure.deleteStateFromRedis(service,TestTreeClass.class,redisLiveObjectKeys.get(j));
            }
            System.out.println("Redis Cache cleared Successfully... ");
        }
    }

    public void redisDataStructuctureTester()
    {
        System.out.println("Total JVM memory: " + Runtime.getRuntime().totalMemory()/(1024*1024)+"MB");
        System.out.println("Free JVM memory: " + Runtime.getRuntime().freeMemory()/(1024*1024)+"MB");
        System.out.println("Used JVM memory: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024)+"MB");
        TestTreeClass treeClassObj = new TestTreeClass("myLiveObject-"+1);
        treeClassObj.setName("A");
        treeClassObj.setValue(1);
        System.out.println("object size1: "+ ObjectSizeCalculator.getObjectSize(treeClassObj)/(1024*1024)+"MB");
        System.out.println("Free JVM memory: " + Runtime.getRuntime().freeMemory()/(1024*1024)+"MB");
        System.out.println("Used JVM memory: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024)+"MB");
        // returned attached proxy object must be captured to a variable to use it for future.
        //the old class object doesn't work and can't modify the redis state
        try {
            String data = new String(Files.readAllBytes(Paths.get("Data_large.txt")));
            System.out.println("data length: "+data.length());
            treeClassObj.setData(data);
            data=null;

        }catch(Exception ex) {
            System.out.println("exception while reading file"+ex);
        }
        treeClassObj.mergeStateToRedis(service);
        treeClassObj=null;

        System.gc();
        System.runFinalization ();
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("object size2: " + ObjectSizeCalculator.getObjectSize(treeClassObj) / (1024 * 1024) + "MB");
            System.out.println("Total JVM memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
            System.out.println("Free JVM memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
            System.out.println("Used JVM memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
        }
        //treeClassObj.deleteStateFromRedis(service,treeClassObj);

        /*HashMap<String, TestTreeClass> treeMap=new HashMap<>();
        TestTreeClass treeClassObj2 = new TestTreeClass("myLiveObject-nested-1");
        treeClassObj2.setName("B");
        treeClassObj2.setRadius(2);
        treeMap.put("key",treeClassObj2);

        treeClassObj.setMyMap(treeMap);
        treeClassObj2.mergeStateToRedis(service);*/



    }

    public List<TestClassLive> createDataSet(int instance) {

        List<TestClassLive> testClassList=new ArrayList<>();

        for(int i=0;i<instance;i++) {
            TestClassLive testObj = new TestClassLive("myliveObject"+i);
            testObj.setName("TAW"+i);
            testObj.setValue(i);
            testObj = service.merge(testObj);
            testClassList.add(testObj);

            TestClassLive2 testObj2 = new TestClassLive2(Long.toString(getRandomLong()));
            testObj2 = service.merge(testObj2);
            testObj2.setData("Data-"+i);
            testObj.setNestedClass(testObj2);
            for(int j=0;j<2;j++) {
                TestClassLive3 testObj3 = new TestClassLive3(getRandomLong(),i+j);
                testObj3 = service.merge(testObj3);
                testObj3.setData("ABCDEFGJIJKLMNOPQRSTUVWXYZABCDEFGJIJKLMNOPQRSTUVWXYZABCDEFGJIJKLMNOPQRSTUVWXYZ"+i+j);
                //testObj.getNestedList().add(testObj3);
            }
        }
        return testClassList;
    }

    public void printDataSet(List<TestClassLive> testClassList,int instance)
    {
        System.out.println("*******Starting to print Dataset values:********");
        for(int i=0;i<instance;i++) {

            System.out.println(testClassList.get(i));
        }
    }

    public RLiveObjectService getService() {
        return service;
    }
}
