import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class TestTreeClass extends RedisDataStructure {

    private int value;
    private String data;
    private HashMap<String, TestTreeClass> myMap=new HashMap<>();
    public TestTreeClass() {

    }
    public TestTreeClass(String Id) {
        super(Id);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public HashMap<String, TestTreeClass> getMyMap() {
        return myMap;
    }

    public void setMyMap(HashMap<String, TestTreeClass> myMap) {
        this.myMap = myMap;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
