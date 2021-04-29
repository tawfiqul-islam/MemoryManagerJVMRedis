import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.liveobject.resolver.LongGenerator;
import org.redisson.liveobject.resolver.UUIDGenerator;

@REntity
public class TestClassLive2 {
    @RId(generator = UUIDGenerator.class)
    private String Id;
    private int value;
    private String data;

    protected TestClassLive2() {
    }

    public TestClassLive2(String Id) {
        super();
        this.Id=Id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TestClassLive2{" +
                "value=" + value +
                ", data='" + data + '\'' +
                '}';
    }
}
