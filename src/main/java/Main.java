import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        RedissonTester redissonTesterObj = new RedissonTester();

        //The following method showcases the issues with key-value based Redis Maps.
        redissonTesterObj.mapTester();
        redissonTesterObj.liveObjectTester1();

        //Uncomment the following codes to run the sample windowbased algorithm
        // to compare JVM vs Redis Memory manangement for iterative windows

        /*redissonTesterObj.redisDataStructuctureTester();
        Scanner tmp=new Scanner(System.in);
        System.out.println("Enter 1 for normal operation, Enter 2 for Redis-enabled operation...");
        int input=tmp.nextInt();
        if(input==1)
            redissonTesterObj.windowBasedAlgorithm(false);
        else if(input==2)
            redissonTesterObj.windowBasedAlgorithm(true);
        else
            System.out.println("Unknown selection, input 1 or 2!");
        System.exit(0);*/
    }
}
