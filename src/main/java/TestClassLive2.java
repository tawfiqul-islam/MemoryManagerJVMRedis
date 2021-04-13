import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.liveobject.resolver.LongGenerator;

@REntity
public class TestClassLive2 {
    @RId
    private long id;
    private int value2;
    private String address;
    protected TestClassLive2() {
    }

    public TestClassLive2(long id, int value) {
        super();
        this.id=id;
        this.value2 = value;
    }

    public int getValue2() {
        return value2;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
